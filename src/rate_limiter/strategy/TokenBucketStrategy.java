package rate_limiter.strategy;

import rate_limiter.entities.RateLimitDecision;
import rate_limiter.entities.RateLimitPolicy;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// [10] Token bucket: each key has a bucket holding up to maxRequests
//      tokens, refilling continuously at maxRequests/windowDuration tokens
//      per unit time. Each request consumes one token if one is available.
//      Example: 100 requests/minute -> capacity 100, refills ~1.67
//      tokens/sec; a client can burst up to 100 requests instantly, then is
//      throttled to the steady refill rate.
//      + Allows bursts up to capacity without smoothing them out - good
//        when occasional bursts are fine as long as the long-run rate
//        holds.
//      + O(1) state per key (two numbers: tokens, lastRefillTime).
//      + No boundary-doubling issue (see SlidingWindowLogStrategy [11]).
//      - Less "fair" right at refill: a client that saved up tokens can
//        fire a full burst back-to-back, which a sliding window would
//        smooth out instead.
public class TokenBucketStrategy implements RateLimitingStrategy {

    // [7] Only the mutable numbers live here — no policy fields — so the
    //     policy is read fresh from the parameter on every tryAcquire call
    //     (see [3]) instead of being frozen in at the moment a key was
    //     first seen.
    private static class BucketState {
        double tokens;
        Instant lastRefillTime;

        BucketState(double tokens, Instant lastRefillTime) {
            this.tokens = tokens;
            this.lastRefillTime = lastRefillTime;
        }
    }

    private final Map<String, BucketState> buckets = new ConcurrentHashMap<>();

    @Override
    public RateLimitDecision tryAcquire(String key, RateLimitPolicy policy) {
        // [8] A new key starts with a full bucket (maxRequests tokens)
        //     rather than empty, so the first burst up to capacity is
        //     allowed immediately instead of forcing a slow start.
        BucketState state = buckets.computeIfAbsent(key,
                k -> new BucketState(policy.getMaxRequests(), Instant.now()));

        // [9] Locking on the per-key BucketState instance, not the shared
        //     map, means two different keys never block each other — only
        //     concurrent requests for the *same* key contend, which is all
        //     req #6 actually requires.
        synchronized (state) {
            refill(state, policy);

            if (state.tokens >= 1) {
                state.tokens -= 1;
                return RateLimitDecision.allow();
            }
            return RateLimitDecision.deny(timeUntilNextToken(state, policy));
        }
    }

    private void refill(BucketState state, RateLimitPolicy policy) {
        Instant now = Instant.now();
        long elapsedMillis = Duration.between(state.lastRefillTime, now).toMillis();

        double capacity = policy.getMaxRequests();
        double refillRatePerMilli = capacity / policy.getWindowDuration().toMillis();

        state.tokens = Math.min(capacity, state.tokens + elapsedMillis * refillRatePerMilli);
        state.lastRefillTime = now;
    }

    private long timeUntilNextToken(BucketState state, RateLimitPolicy policy) {
        double refillRatePerMilli = policy.getMaxRequests() / (double) policy.getWindowDuration().toMillis();
        double tokensNeeded = 1 - state.tokens;
        return (long) Math.ceil(tokensNeeded / refillRatePerMilli);
    }
}
