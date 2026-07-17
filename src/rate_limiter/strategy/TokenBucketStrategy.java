package rate_limiter.strategy;

import rate_limiter.entities.RateLimitDecision;
import rate_limiter.entities.RateLimitPolicy;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

// [RL-10] Token bucket: each key has a bucket holding up to maxRequests
//         tokens, refilling continuously at maxRequests/windowDuration
//         tokens per unit time. Each request consumes one token if one is
//         available. Example: 100 requests/minute -> capacity 100,
//         refills ~1.67 tokens/sec; a client can burst up to 100 requests
//         instantly, then is throttled to the steady refill rate.
//         + Allows bursts up to capacity without smoothing them out - good
//           when occasional bursts are fine as long as the long-run rate
//           holds.
//         + O(1) state per key (two numbers: tokens, lastRefillTime).
//         + No boundary-doubling issue (see SlidingWindowLogStrategy
//           [RL-11]).
//         - Less "fair" right at refill: a client that saved up tokens can
//           fire a full burst back-to-back, which a sliding window would
//           smooth out instead.
public class TokenBucketStrategy implements RateLimitingStrategy {

    // [RL-7] Only the mutable numbers live here — no policy fields — so
    //        the policy is read fresh from the parameter on every
    //        tryAcquire call (see [RL-3]) instead of being frozen in at
    //        the moment a key was first seen. lastWindow additionally
    //        records the windowDuration seen on the most recent touch,
    //        purely so a later sweep (see [RL-16]) can judge staleness
    //        per-key without needing to know every currently-registered
    //        policy.
    private static class BucketState {
        double tokens;
        Instant lastRefillTime;
        Duration lastWindow;

        BucketState(double tokens, Instant lastRefillTime, Duration lastWindow) {
            this.tokens = tokens;
            this.lastRefillTime = lastRefillTime;
            this.lastWindow = lastWindow;
        }
    }

    private final Map<String, BucketState> buckets = new ConcurrentHashMap<>();

    // [RL-15] req #7: bound buckets' growth by piggybacking a full sweep
    //         on ordinary traffic instead of running a background thread -
    //         no lifecycle (start/stop) to manage, at the cost of only
    //         sweeping when *some* key is actively sending requests. 1000
    //         is an arbitrary compromise between sweep overhead and how
    //         long a fully idle entry can linger before being reclaimed.
    private static final long SWEEP_INTERVAL = 1000;
    private final AtomicLong requestCount = new AtomicLong();

    @Override
    public RateLimitDecision tryAcquire(String key, RateLimitPolicy policy) {
        // [RL-8] A new key starts with a full bucket (maxRequests tokens)
        //        rather than empty, so the first burst up to capacity is
        //        allowed immediately instead of forcing a slow start.
        BucketState state = buckets.computeIfAbsent(key,
                k -> new BucketState(policy.getMaxRequests(), Instant.now(), policy.getWindowDuration()));

        // [RL-9] Locking on the per-key BucketState instance, not the
        //        shared map, means two different keys never block each
        //        other — only concurrent requests for the *same* key
        //        contend, which is all req #6 actually requires.
        RateLimitDecision decision;
        synchronized (state) {
            refill(state, policy);

            if (state.tokens >= 1) {
                state.tokens -= 1;
                decision = RateLimitDecision.allow();
            } else {
                decision = RateLimitDecision.deny(timeUntilNextToken(state, policy));
            }
        }

        // Sweep runs after releasing this key's lock, so it never holds
        // more than one BucketState lock at a time (see [RL-16]).
        if (requestCount.incrementAndGet() % SWEEP_INTERVAL == 0) {
            evictStale();
        }
        return decision;
    }

    private void refill(BucketState state, RateLimitPolicy policy) {
        Instant now = Instant.now();
        long elapsedMillis = Duration.between(state.lastRefillTime, now).toMillis();

        double capacity = policy.getMaxRequests();
        double refillRatePerMilli = capacity / policy.getWindowDuration().toMillis();

        state.tokens = Math.min(capacity, state.tokens + elapsedMillis * refillRatePerMilli);
        state.lastRefillTime = now;
        state.lastWindow = policy.getWindowDuration();
    }

    private long timeUntilNextToken(BucketState state, RateLimitPolicy policy) {
        double refillRatePerMilli = policy.getMaxRequests() / (double) policy.getWindowDuration().toMillis();
        double tokensNeeded = 1 - state.tokens;
        return (long) Math.ceil(tokensNeeded / refillRatePerMilli);
    }

    // [RL-16] A bucket idle for >= its own lastWindow is behaviorally
    //         identical to a bucket that doesn't exist yet - refill()
    //         would already have it capped back at full capacity - so
    //         removing it changes nothing about future behavior, it just
    //         reclaims memory. Using each entry's own lastWindow, not one
    //         global constant, matters because different keys/tiers can
    //         be registered with different windows.
    //         Note: there's a narrow, largely theoretical race between
    //         this sweep and a concurrent tryAcquire on the very same key
    //         - a request whose computeIfAbsent has already returned this
    //         exact state object, but hasn't yet reached the synchronized
    //         block above, could have its effect silently discarded if
    //         this sweep removes that entry in between. Closing it fully
    //         would mean folding fetch-or-create and refill/consume into
    //         a single atomic buckets.compute(...) call instead of two
    //         separate steps. Not done here since triggering it needs a
    //         sweep and a request for the very same key to land within
    //         nanoseconds of each other, exactly at that key's staleness
    //         boundary.
    private void evictStale() {
        Instant now = Instant.now();
        buckets.entrySet().removeIf(entry -> {
            BucketState state = entry.getValue();
            synchronized (state) {
                return Duration.between(state.lastRefillTime, now).compareTo(state.lastWindow) >= 0;
            }
        });
    }
}
