package notification_system.manager;

import notification_system.entities.ChannelType;
import notification_system.entities.DeliveryLog;
import notification_system.entities.DeliveryStatus;
import notification_system.entities.NotificationPolicy;
import notification_system.entities.NotificationType;
import notification_system.entities.UserPreferences;
import notification_system.strategy.NotificationChannel;
import notification_system.strategy.NotificationDeliveryException;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class Dispatcher {
    private static final int MAX_ATTEMPTS = 3;

    // [NS-4] policies/channels only grow from explicit registerPolicy/
    //        registerChannel calls (an admin/wiring-time action, not
    //        request traffic) - same reasoning as RateLimiter's
    //        keyPolicies [RL-14]. ConcurrentHashMap so a registration
    //        from one thread is safely visible to dispatch() calls
    //        running on others, without needing a lock around reads.
    private final Map<NotificationType, NotificationPolicy> policies = new ConcurrentHashMap<>();
    private final Map<ChannelType, NotificationChannel> channels = new ConcurrentHashMap<>();

    // [NS-7] userPreferences is keyed by userId and owned here rather
    //        than pushed onto the caller, so dispatch() only ever needs
    //        a userId. Unlike policies/channels, this map is sized by
    //        user count, not by admin config - so it must only grow from
    //        an explicit disableChannel/enableChannel call (someone
    //        actually touching their settings), never from a lookup
    //        during dispatch. A user who's merely receiving
    //        notifications and has never configured anything must not
    //        plant a row here - see the getOrDefault-not-computeIfAbsent
    //        split between the read path (resolveChannels) and the write
    //        path (disableChannel/enableChannel) below.
    private final Map<String, UserPreferences> userPreferences = new ConcurrentHashMap<>();

    // [NS-9] Keyed by notificationId (req #6 wants status per
    //        (notification, channel), not per (user, type) - the same
    //        user can get several PROMOTION sends over time, so type
    //        alone can't distinguish them). One dispatch() call fans out
    //        to several channels, and a channel can log more than once
    //        (RETRYING per failed attempt, then a final SENT/FAILED), so
    //        this is a Map to a List, not a single log per id.
    //        CopyOnWriteArrayList because a given id's list is written by
    //        whichever thread is running that dispatch() call and could
    //        be read concurrently by a caller polling status - writes are
    //        rare relative to reads for a given id's log.
    private final Map<String, List<DeliveryLog>> deliveryLogs = new ConcurrentHashMap<>();

    public void registerPolicy(NotificationPolicy policy) {
        policies.put(policy.getType(), policy);
    }

    public void registerChannel(NotificationChannel channel) {
        channels.put(channel.getType(), channel);
    }

    public void disableChannel(String userId, NotificationType type, ChannelType channel) {
        userPreferences.computeIfAbsent(userId, UserPreferences::new).disableChannel(type, channel);
    }

    public void enableChannel(String userId, NotificationType type, ChannelType channel) {
        UserPreferences preferences = userPreferences.get(userId);
        if (preferences != null) {
            preferences.enableChannel(type, channel);
        }
    }

    // [NS-5] The two-tier resolution this package settled on: a
    //        non-configurable policy (e.g. PASSWORD_RESET) always sends
    //        on every eligible channel - preferences are never even
    //        looked at, so a user's promo-fatigue opt-outs can't silently
    //        swallow a security notification. A configurable policy
    //        (e.g. PROMOTION) sends on eligible channels minus whatever
    //        the user has explicitly disabled for that type. A userId
    //        with no entry in userPreferences at all behaves exactly
    //        like one with an empty disabled set - "not found" already
    //        is the default (see [NS-2], [NS-7]), so there's no separate
    //        defaultPreferences object to fall back to.
    public Set<ChannelType> resolveChannels(String userId, NotificationType type) {
        NotificationPolicy policy = policies.get(type);
        if (policy == null) {
            throw new IllegalArgumentException("No policy registered for " + type);
        }
        if (!policy.isConfigurable()) {
            return new HashSet<>(policy.getEligibleChannels());
        }
        UserPreferences preferences = userPreferences.get(userId);
        Set<ChannelType> disabled = (preferences == null) ? Collections.emptySet() : preferences.getDisabledChannels(type);
        Set<ChannelType> resolved = new HashSet<>(policy.getEligibleChannels());
        resolved.removeAll(disabled);
        return resolved;
    }

    // [NS-6] A resolved channel with no registered NotificationChannel is
    //        a wiring bug (a policy references a channel nobody
    //        implemented for), not a runtime condition to swallow -
    //        failing loudly here surfaces it at dispatch time instead of
    //        silently dropping a notification a user was supposed to get.
    //        Generates one notificationId per call and returns it so the
    //        caller can look up delivery status afterward via
    //        getDeliveryLogs (see [NS-9]).
    public String dispatch(String userId, NotificationType type, String content) {
        String notificationId = UUID.randomUUID().toString();
        Set<ChannelType> resolvedChannels = resolveChannels(userId, type);
        for (ChannelType channelType : resolvedChannels) {
            NotificationChannel channel = channels.get(channelType);
            if (channel == null) {
                throw new IllegalStateException("No channel registered for " + channelType);
            }
            sendWithRetry(notificationId, channelType, channel, userId, content);
        }
        return notificationId;
    }

    // [NS-10] Retry lives here rather than behind a decorator wrapping
    //         NotificationChannel, specifically so each attempt can be
    //         logged as it happens - a RETRYING entry per failed attempt,
    //         then one final SENT or FAILED. No PENDING entry is ever
    //         logged: dispatch() attempts synchronously with no gap
    //         between "queued" and "first attempt," so PENDING has
    //         nothing to represent here. It's kept in DeliveryStatus
    //         anyway since it's a real state a future async/queued
    //         dispatch path would need.
    private void sendWithRetry(String notificationId, ChannelType channelType, NotificationChannel channel,
                                String userId, String content) {
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                channel.send(userId, content);
                log(notificationId, channelType, DeliveryStatus.SENT);
                return;
            } catch (NotificationDeliveryException e) {
                boolean lastAttempt = attempt == MAX_ATTEMPTS;
                log(notificationId, channelType, lastAttempt ? DeliveryStatus.FAILED : DeliveryStatus.RETRYING);
            }
        }
    }

    private void log(String notificationId, ChannelType channelType, DeliveryStatus status) {
        deliveryLogs.computeIfAbsent(notificationId, id -> new CopyOnWriteArrayList<>())
                .add(new DeliveryLog(notificationId,channelType, status, Instant.now()));
    }

    public List<DeliveryLog> getDeliveryLogs(String notificationId) {
        return deliveryLogs.getOrDefault(notificationId, Collections.emptyList());
    }
}
