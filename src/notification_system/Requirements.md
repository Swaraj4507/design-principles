# Designing a Notification System

## Requirements
1. A caller should be able to trigger a notification for a user, specifying
   the notification type (e.g. ORDER_SHIPPED, PASSWORD_RESET, PROMOTION) and
   the content/payload to render.
2. The system should support sending through multiple channels — email, SMS,
   push — behind a common interface, so a new channel can be added without
   changing the code that decides what to send.
3. A single logical notification may need to go out on more than one channel
   at once (e.g. both email and push for the same event), driven by which
   channels are applicable for that user/notification.
4. Each user should be able to set channel preferences per notification type
   (e.g. "email me for order updates, don't SMS me for promotions"). The
   system should honor these preferences before dispatching — a channel a
   user has opted out of for that type should never be sent to.
5. A send attempt on a channel can fail (simulating a transient
   provider/network failure). The system should retry a failed send some
   bounded number of times before giving up and marking it failed.
6. The system should track delivery status per (notification, channel) —
   e.g. PENDING, SENT, FAILED, RETRYING — queryable after the fact.
7. The system should be extensible to new notification types and new
   channels without changes to the core "trigger a notification" call path.

## Out of scope (for now)
- Per-user/per-channel rate limiting (spam protection). Already covered as
  its own problem in `src/rate_limiter` — `RateLimiter.tryAcquire(key)` is
  key-agnostic, so this LLD isn't the place to re-explore that design; it'd
  just be reused with a composite `userId:channelType` key if ever wired in.
- Actual integration with real email/SMS/push providers (assume a
  send-to-channel call is simulated and can be made to succeed or fail on
  demand).
- Templating/localization of notification content (assume content is
  already rendered/passed in by the caller).
- Notification batching/digesting (combining multiple events into one
  summary notification).
- Read receipts / in-app notification inbox UI concerns.
- Scheduling notifications for future delivery (assume all triggers are
  "send now").
