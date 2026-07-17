# Designing a Rate Limiter

## Requirements
1. The system should let a client check whether a given request is allowed
   right now, for some key identifying the caller (e.g. user id, API key, or
   IP address), and record the request if it is allowed.
2. Each key should be rate-limited independently of every other key — one
   user hitting their limit must not affect another user's requests.
3. The system should support configuring a limit as "N requests per time
   window" (e.g. 100 requests per minute), with the limit/window
   configurable per key or per group of keys (e.g. per API tier).
4. The system should support more than one limiting algorithm (e.g. token
   bucket, sliding window) behind the same interface, swappable without
   changing calling code.
5. A denied request should be distinguishable from an allowed one, and the
   system should be able to report how long the caller should wait before
   retrying (retry-after).
6. The system should be thread-safe: concurrent requests for the same key
   must not corrupt that key's count/bucket state or allow more requests
   through than the configured limit.
7. Limiter state should be held in memory (no external store), sized so
   that keys which stop sending requests don't grow the state unboundedly
   forever.
8. The system should be extensible to new limiting algorithms without
   changing the core "is this request allowed" call path.
9. The active limiting algorithm should be swappable at runtime without
   losing already-registered per-key/tier configuration — a caller who
   registered a limit before the swap shouldn't have to re-register it
   after. In-flight state (token counts, timestamp logs) does not need to
   survive the swap, since it isn't meaningful across algorithms.

## Out of scope (for now)
- Distributed rate limiting across multiple server instances / shared
  external store (e.g. Redis-backed counters).
- Dynamic reconfiguration of the numeric limits themselves at runtime via
  an admin API (changing what "N requests per window" means for an
  already-registered key) — req #9 is only about the algorithm swap
  preserving whatever config was already registered, not about editing
  that config live.
- Request queuing/throttling (delaying a request until it's allowed) —
  only allow/deny decisions.
- Authentication/identification of the caller (assume the key is already
  known and trusted when passed in).
