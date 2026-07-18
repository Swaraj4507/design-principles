package notification_system.strategy;

import notification_system.entities.ChannelType;

// [NS-3] Deliberately knows nothing about eligibility or user preference -
//        by the time send() is called, Dispatcher [NS-5] has already
//        decided this channel is allowed to fire for this user/type. That
//        keeps a channel implementation reusable across every
//        notification type without re-implementing preference checks
//        (see the option A vs B discussion this package settled on), and
//        means a new channel only has to answer "how do I deliver," never
//        "should I."
public interface NotificationChannel {
    ChannelType getType();

    void send(String userId, String content) throws NotificationDeliveryException;
}
