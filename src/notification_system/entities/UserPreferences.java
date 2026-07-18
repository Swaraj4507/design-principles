package notification_system.entities;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// [NS-2] Stores channels the user has explicitly turned OFF per type,
//        rather than channels turned on. That makes "default-allow"
//        (a type the user has never touched sends on every eligible
//        channel) fall out for free - an empty/missing entry means
//        nothing has been disabled - instead of needing a separate
//        "has this user configured this type yet" flag that an
//        opt-in map would require. Only meaningful for types where
//        NotificationPolicy#isConfigurable is true; Dispatcher never
//        consults this for a non-configurable type (see [NS-5]).
public class UserPreferences {
    private final String userId;
    private final Map<NotificationType, Set<ChannelType>> disabledChannels = new HashMap<>();

    public UserPreferences(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void disableChannel(NotificationType type, ChannelType channel) {
        disabledChannels.computeIfAbsent(type, t -> new HashSet<>()).add(channel);
    }

    public void enableChannel(NotificationType type, ChannelType channel) {
        Set<ChannelType> disabled = disabledChannels.get(type);
        if (disabled != null) {
            disabled.remove(channel);
        }
    }

    public Set<ChannelType> getDisabledChannels(NotificationType type) {
        return disabledChannels.getOrDefault(type, Collections.emptySet());
    }
}
