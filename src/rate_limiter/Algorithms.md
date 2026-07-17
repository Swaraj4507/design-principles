# Rate Limiting Algorithms

Both strategies are handed the same `RateLimitPolicy` — `N requests per
windowDuration` — but that number means something different depending on
the algorithm. `RateLimitPolicy` is deliberately a business policy, not an
algorithm-specific config (see `[RL-1]` in `RateLimitPolicy.java`): each
strategy derives its own internal mechanics from it, rather than owning a
distinct config type like a `TokenBucketConfig` vs a `SlidingWindowConfig`.
That's what lets a policy survive an algorithm swap (req #9) — but it does
NOT mean the resulting behavior is identical, which is exactly what this
doc works through with concrete examples.

## Token Bucket (`TokenBucketStrategy`)

Each key gets a bucket holding up to `maxRequests` tokens. It starts **full**
and refills continuously at `maxRequests / windowDuration` tokens per unit
time. Each request consumes one token if one is available; otherwise it's
denied with a `retryAfter` computed from the refill rate.

Config: 60 requests / 1 minute → capacity 60, refill rate = 1 token/sec.

| time | event | tokens before | tokens after |
|---|---|---|---|
| t = 0s | bucket created (starts full) | — | 60 |
| t = 0s | 30 requests arrive | 60 | 30 |
| t = 20s | refill: 20s elapsed × 1/sec | 30 | 50 |
| t = 20s | 50 requests arrive | 50 | 0 |

**Total allowed in the first 20 seconds: 30 + 50 = 80** — more than the
configured "60 per minute." Push the same idea further: burst 60 at t=0,
then keep sending at exactly the refill rate — by t=60s that's
`60 (initial) + 60 (refilled over 60s) = 120` requests in the first minute,
**double** the nominal rate.

**This is not a bug.** Token bucket only promises two things:
1. Never more than `capacity` tokens consumed instantaneously (the burst
   ceiling).
2. Long-run average rate never exceeds `maxRequests / windowDuration`.

It does **not** promise "never more than N requests in any rolling window of
length windowDuration" — that's a stronger guarantee, and it's exactly what
sliding window log provides instead. The "starts full" design (see `[RL-8]` in
`TokenBucketStrategy.java`) means every key gets one such burst credit up
front, and gets it again any time it sits idle long enough for the bucket to
refill back to capacity.

**Use it when:** occasional bursts are fine as long as the long-run rate
holds, and you want O(1) memory per key.

## Sliding Window Log (`SlidingWindowLogStrategy`)

Each key keeps a log of the timestamp of every request accepted in the last
`windowDuration`. A request is allowed iff fewer than `maxRequests`
timestamps remain in that trailing window, after evicting anything older
than `now - windowDuration`.

Same policy: 60 requests / 1 minute.

| time | event | timestamps in window (last 60s) | outcome |
|---|---|---|---|
| t = 0s | 30 requests | 0 → 30 | all 30 allowed |
| t = 20s | 50 requests | 30 (none expired yet) | only 30 allowed (60 − 30), then denied |

Unlike token bucket, there is no separate burst credit sitting on top of the
window — the cap is a hard "at most 60 timestamps in any trailing 60s,"
full stop, regardless of when the key was created or how idle it's been.
That's the guarantee token bucket can't make.

**Trade-off:** exactness costs memory. Token bucket is O(1) state per key
(two numbers); sliding window log is O(maxRequests) per key, since it must
retain one timestamp per accepted request still inside the window. That
also means it does more work per call (evicting expired timestamps from the
front of the log) versus token bucket's constant-time refill arithmetic —
still amortized O(1) per request, but more per-call cost in practice.

**Use it when:** you need a hard rolling-window cap with no burst
allowance beyond it — e.g. compliance/quota scenarios where "at most N per
window" must hold exactly, not just on average.

## Summary

| | Token Bucket | Sliding Window Log |
|---|---|---|
| Memory per key | O(1) | O(maxRequests) |
| Burst allowance | Up to full capacity, on top of refill | None — hard cap per rolling window |
| Guarantee | Long-run average rate | Exact count in any trailing window |
| retryAfter | From refill math | Exact expiry of oldest timestamp |
