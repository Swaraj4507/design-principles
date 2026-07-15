package amazon_locker.notification;

import amazon_locker.entities.Parcel;
import amazon_locker.manager.LockerAssignment;

// [17] One method, taking the domain objects it actually needs (Parcel for
//      recipient contact, LockerAssignment for locker/code/expiry) rather
//      than a generic "message envelope" — nothing today needs
//      channel-specific templating, so that abstraction would be
//      speculative right now.
public interface NotificationSender {
    void send(Parcel parcel, LockerAssignment assignment);
}
