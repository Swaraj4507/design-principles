package rate_limiter.entities;

import java.time.Duration;

// [1] This is a business policy ("N requests per window"), not an
//     algorithm-specific config - deliberately the one shape every
//     algorithm can agree on the meaning of. TokenBucketStrategy and
//     SlidingWindowLogStrategy each derive their own internal mechanics
//     from it (capacity/refillRate, or the raw numbers directly) instead
//     of owning distinct types like a TokenBucketConfig vs a
//     SlidingWindowConfig. That's what makes req #9 (swap algorithm,
//     keep registered limits) meaningful: it promises the policy survives
//     a swap, not that the resulting behavior is identical - different
//     algorithms enforcing the same policy with different guarantees is
//     the whole reason to have more than one (see Algorithms.md).
public class RateLimitPolicy {
    private final int maxRequests;
    private final Duration windowDuration;

    public RateLimitPolicy(int maxRequests, Duration windowDuration) {
        this.maxRequests = maxRequests;
        this.windowDuration = windowDuration;
    }

    public int getMaxRequests() {
        return maxRequests;
    }

    public Duration getWindowDuration() {
        return windowDuration;
    }
}
