package notification_system.strategy;

import notification_system.entities.ChannelType;

public class PushChannel implements NotificationChannel {
    @Override
    public ChannelType getType() {
        return ChannelType.PUSH;
    }

    @Override
    public void send(String userId, String content) {
        System.out.println("[PUSH -> " + userId + "] " + content);
    }
}
