package rate_limiter.strategy;

import rate_limiter.entities.RateLimitDecision;
import rate_limiter.entities.RateLimitPolicy;

// [RL-3] Takes the policy as a parameter on every call rather than having
//        each strategy own its own key-to-policy lookup. Keeps policy
//        resolution as a single concern in RateLimiter (the entry class)
//        and lets a strategy stay purely mechanical: given a key and the
//        policy that currently applies to it, decide allow/deny. Also
//        what makes swapping the algorithm (req #4) safe — no policy
//        state is trapped inside the old strategy instance, and each
//        strategy derives its own internal mechanics from the same
//        policy shape (see RateLimitPolicy [RL-1]).
public interface RateLimitingStrategy {
    RateLimitDecision tryAcquire(String key, RateLimitPolicy policy);
}
