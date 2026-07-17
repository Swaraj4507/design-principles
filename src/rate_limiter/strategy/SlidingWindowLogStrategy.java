package rate_limiter.strategy;

import rate_limiter.entities.RateLimitDecision;
import rate_limiter.entities.RateLimitPolicy;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// [11] Sliding window log: each key keeps a log of the timestamp of every
//      request accepted in the last windowDuration. A request is allowed
//      iff fewer than maxRequests timestamps remain in that trailing
//      window, after evicting any that have expired.
//      Example: 100 requests/minute -> at any instant, count timestamps
//      from (now - 1 minute) to now; allow iff that count < 100.
//      + Exact: no boundary-doubling like fixed windows, and no
//        approximation like a weighted sliding window counter.
//      + retryAfter is exact too - it's just when the oldest timestamp
//        currently in the window will expire.
//      - O(maxRequests) memory per active key (one timestamp stored per
//        accepted request in the window), vs O(1) for token bucket -
//        matters more once req #7 (bounded memory) is addressed, since
//        there's more state to reclaim per idle key here.
//      - Every request evicts expired timestamps from the front of the
//        log - amortized O(1) per request (each timestamp is pushed and
//        popped exactly once) but more work per call than token bucket's
//        O(1) refill arithmetic.
public class SlidingWindowLogStrategy implements RateLimitingStrategy {

    private static class LogState {
        // [12] ArrayDeque, not a concurrent collection - safe because every
        //      access happens inside `synchronized (state)` below, and the
        //      deque is never exposed outside this class.
        final Deque<Instant> timestamps = new ArrayDeque<>();
    }

    private final Map<String, LogState> logs = new ConcurrentHashMap<>();

    @Override
    public RateLimitDecision tryAcquire(String key, RateLimitPolicy policy) {
        LogState state = logs.computeIfAbsent(key, k -> new LogState());

        // [13] Same per-key locking approach as TokenBucketStrategy's [9]:
        //      lock on the per-key LogState instance, not the shared map,
        //      so unrelated keys never block each other.
        synchronized (state) {
            Instant now = Instant.now();
            Instant windowStart = now.minus(policy.getWindowDuration());

            while (!state.timestamps.isEmpty() && !state.timestamps.peekFirst().isAfter(windowStart)) {
                state.timestamps.pollFirst();
            }

            if (state.timestamps.size() < policy.getMaxRequests()) {
                state.timestamps.addLast(now);
                return RateLimitDecision.allow();
            }

            Instant oldest = state.timestamps.peekFirst();
            long retryAfterMillis = Duration.between(windowStart, oldest).toMillis();
            return RateLimitDecision.deny(retryAfterMillis);
        }
    }
}
/*
computeIfAbsent, size, and addLast are O(1). 
The cleanup loop can remove multiple expired timestamps, so a single request has a worst-case time complexity of O(maxRequests).
However, each timestamp is inserted once and removed once, so across many requests the cleanup cost is amortized O(1) per request. 
Space complexity is O(maxRequests) per active key because we store one timestamp for every accepted request in the current window. 
*/
