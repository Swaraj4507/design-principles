package rate_limiter.entities;

// [RL-2] A plain boolean can't carry retry-after (req #5), so the outcome
//        is its own type. Construction goes through
//        allow()/deny(retryAfterMillis) rather than a public constructor,
//        so "allowed but with a retryAfter" can't be built by mistake.
public class RateLimitDecision {
    private final boolean allowed;
    private final long retryAfterMillis;

    private RateLimitDecision(boolean allowed, long retryAfterMillis) {
        this.allowed = allowed;
        this.retryAfterMillis = retryAfterMillis;
    }

    public static RateLimitDecision allow() {
        return new RateLimitDecision(true, 0);
    }

    public static RateLimitDecision deny(long retryAfterMillis) {
        return new RateLimitDecision(false, retryAfterMillis);
    }

    public boolean isAllowed() {
        return allowed;
    }

    public long getRetryAfterMillis() {
        return retryAfterMillis;
    }
}
