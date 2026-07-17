package rate_limiter.strategy;

import rate_limiter.entities.RateLimitDecision;
import rate_limiter.entities.RateLimitPolicy;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

// [RL-11] Sliding window log: each key keeps a log of the timestamp of
//         every request accepted in the last windowDuration. A request is
//         allowed iff fewer than maxRequests timestamps remain in that
//         trailing window, after evicting any that have expired.
//         Example: 100 requests/minute -> at any instant, count
//         timestamps from (now - 1 minute) to now; allow iff that count
//         < 100.
//         + Exact: no boundary-doubling like fixed windows, and no
//           approximation like a weighted sliding window counter.
//         + retryAfter is exact too - it's just when the oldest
//           timestamp currently in the window will expire.
//         - O(maxRequests) memory per active key (one timestamp stored
//           per accepted request in the window), vs O(1) for token
//           bucket - matters more once req #7 (bounded memory) is
//           addressed, since there's more state to reclaim per idle key
//           here.
//         - Every request evicts expired timestamps from the front of
//           the log - amortized O(1) per request (each timestamp is
//           pushed and popped exactly once) but more work per call than
//           token bucket's O(1) refill arithmetic.
public class SlidingWindowLogStrategy implements RateLimitingStrategy {

    private static class LogState {
        // [RL-12] ArrayDeque, not a concurrent collection - safe because
        //         every access happens inside `synchronized (state)`
        //         below, and the deque is never exposed outside this
        //         class.
        final Deque<Instant> timestamps = new ArrayDeque<>();

        // [RL-17] Tracks the windowDuration seen on the most recent
        //         touch, so a later sweep (see [RL-18]) can judge
        //         staleness per-key without needing to know every
        //         currently-registered policy - same reasoning as
        //         BucketState.lastWindow in TokenBucketStrategy [RL-7].
        Duration lastWindow;
    }

    private final Map<String, LogState> logs = new ConcurrentHashMap<>();

    // [RL-19] Same sweep approach and SWEEP_INTERVAL rationale as
    //         TokenBucketStrategy [RL-15]: piggyback on ordinary traffic
    //         rather than run a background thread.
    private static final long SWEEP_INTERVAL = 1000;
    private final AtomicLong requestCount = new AtomicLong();

    @Override
    public RateLimitDecision tryAcquire(String key, RateLimitPolicy policy) {
        LogState state = logs.computeIfAbsent(key, k -> new LogState());

        // [RL-13] Same per-key locking approach as TokenBucketStrategy's
        //         [RL-9]: lock on the per-key LogState instance, not the
        //         shared map, so unrelated keys never block each other.
        RateLimitDecision decision;
        synchronized (state) {
            Instant now = Instant.now();
            Instant windowStart = now.minus(policy.getWindowDuration());
            state.lastWindow = policy.getWindowDuration();

            while (!state.timestamps.isEmpty() && !state.timestamps.peekFirst().isAfter(windowStart)) {
                state.timestamps.pollFirst();
            }

            if (state.timestamps.size() < policy.getMaxRequests()) {
                state.timestamps.addLast(now);
                decision = RateLimitDecision.allow();
            } else {
                Instant oldest = state.timestamps.peekFirst();
                long retryAfterMillis = Duration.between(windowStart, oldest).toMillis();
                decision = RateLimitDecision.deny(retryAfterMillis);
            }
        }

        // Sweep runs after releasing this key's lock, mirroring
        // TokenBucketStrategy's tryAcquire (see its comment above
        // [RL-16]).
        if (requestCount.incrementAndGet() % SWEEP_INTERVAL == 0) {
            evictStale();
        }
        return decision;
    }

    // [RL-18] Same idea as TokenBucketStrategy's eviction [RL-16]: a log
    //         whose most recent entry is already older than its own
    //         lastWindow has nothing left that could still be inside the
    //         window, so it's behaviorally equivalent to a freshly-
    //         created, empty log - removing it changes nothing about
    //         future behavior. An entry only reaches this state by going
    //         idle: the per-request eviction above only runs when that
    //         specific key is touched, so an idle key's expired
    //         timestamps would otherwise sit untouched forever without
    //         this sweep.
    private void evictStale() {
        Instant now = Instant.now();
        logs.entrySet().removeIf(entry -> {
            LogState state = entry.getValue();
            synchronized (state) {
                if (state.timestamps.isEmpty()) {
                    return true;
                }
                return Duration.between(state.timestamps.peekLast(), now).compareTo(state.lastWindow) >= 0;
            }
        });
    }
}
/*
computeIfAbsent, size, and addLast are O(1).
The cleanup loop can remove multiple expired timestamps, so a single request has a worst-case time complexity of O(maxRequests).
However, each timestamp is inserted once and removed once, so across many requests the cleanup cost is amortized O(1) per request.
Space complexity is O(maxRequests) per active key because we store one timestamp for every accepted request in the current window.
*/
