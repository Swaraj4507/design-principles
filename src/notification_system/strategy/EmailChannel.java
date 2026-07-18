package notification_system.strategy;

import notification_system.entities.ChannelType;

public class EmailChannel implements NotificationChannel {
    @Override
    public ChannelType getType() {
        return ChannelType.EMAIL;
    }

    @Override
    public void send(String userId, String content) {
        System.out.println("[EMAIL -> " + userId + "] " + content);
    }
}
