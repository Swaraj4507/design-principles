package notification_system.strategy;

import notification_system.entities.ChannelType;

public class SmsChannel implements NotificationChannel {
    @Override
    public ChannelType getType() {
        return ChannelType.SMS;
    }

    @Override
    public void send(String userId, String content) {
        System.out.println("[SMS -> " + userId + "] " + content);
    }
}
