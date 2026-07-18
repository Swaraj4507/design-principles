package notification_system.entities;

import java.util.Set;

// [NS-1] Which channels a notification type is even allowed to use
//        (eligibleChannels) and whether the user gets a say at all
//        (configurable) are business rules owned by this class, not by
//        the user. PASSWORD_RESET is deliberately constructed with
//        {EMAIL, SMS} and configurable=false — push is never eligible
//        (a lock-screen push is a worse security posture than email/SMS),
//        and no user opt-out can suppress it, so a security notification
//        can never be silently dropped by promo-fatigue settings. See
//        UserPreferences [NS-2] and Dispatcher#resolveChannels [NS-5] for
//        how this combines with per-user preference.
public class NotificationPolicy {
    private final NotificationType type;
    private final Set<ChannelType> eligibleChannels;
    private final boolean configurable;

    public NotificationPolicy(NotificationType type, Set<ChannelType> eligibleChannels, boolean configurable) {
        this.type = type;
        this.eligibleChannels = eligibleChannels;
        this.configurable = configurable;
    }

    public NotificationType getType() {
        return type;
    }

    public Set<ChannelType> getEligibleChannels() {
        return eligibleChannels;
    }

    public boolean isConfigurable() {
        return configurable;
    }
}
