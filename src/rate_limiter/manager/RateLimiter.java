package rate_limiter.manager;

import rate_limiter.entities.RateLimitDecision;
import rate_limiter.entities.RateLimitPolicy;
import rate_limiter.strategy.RateLimitingStrategy;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimiter {
    private volatile RateLimitingStrategy strategy;
    private final RateLimitPolicy defaultPolicy;
    private final Map<String, RateLimitPolicy> keyPolicies = new ConcurrentHashMap<>();

    public RateLimiter(RateLimitingStrategy strategy, RateLimitPolicy defaultPolicy) {
        this.strategy = strategy;
        this.defaultPolicy = defaultPolicy;
    }

    // [4] Registering the same policy for every key in a tier is how "per
    //     group of keys" (req #3) is achieved — there's no separate
    //     tier/group entity, just repeated registration of one shared
    //     policy value. Keys with no explicit registration fall back to
    //     defaultPolicy in tryAcquire, so there's no "unconfigured key"
    //     error case to handle.
    public void registerLimit(String key, RateLimitPolicy policy) {
        keyPolicies.put(key, policy);
    }

    // [5] Policy resolution happens here, once, before delegating — the
    //     strategy never sees the keyPolicies map at all (see [3]). Reads
    //     and writes of keyPolicies race against each other under
    //     concurrent callers (req #6), which is why it's a
    //     ConcurrentHashMap rather than a plain HashMap.
    public RateLimitDecision tryAcquire(String key) {
        RateLimitPolicy policy = keyPolicies.getOrDefault(key, defaultPolicy);
        return strategy.tryAcquire(key, policy);
    }

    // [6] This is only safe because keyPolicies stores a RateLimitPolicy —
    //     a business policy ("N requests per window", see RateLimitPolicy
    //     [1]) — never an algorithm-specific config. A policy means the
    //     same thing regardless of which strategy is active, so swapping
    //     the algorithm never touches keyPolicies: every already-registered
    //     key keeps its policy under the new algorithm with no
    //     re-registration (req #9). That does NOT mean identical behavior
    //     before and after the swap — token bucket and sliding window log
    //     enforce the same policy with different guarantees on purpose
    //     (see Algorithms.md) — only that the caller's registered intent
    //     survives. What does NOT carry over is in-flight state (token
    //     counts, timestamp logs) — that's fine, since a token count means
    //     nothing to a sliding-window log or vice versa; each strategy
    //     instance simply starts state-less for every key under the new
    //     algorithm. strategy is volatile so a swap from one thread is
    //     immediately visible to tryAcquire() calls on other threads — no
    //     lock needed, since reassigning a reference is already atomic, and
    //     a request landing on the old vs. new strategy right at the swap
    //     boundary is an acceptable, harmless race either way.
    public void setStrategy(RateLimitingStrategy newStrategy) {
        this.strategy = newStrategy;
    }
}
