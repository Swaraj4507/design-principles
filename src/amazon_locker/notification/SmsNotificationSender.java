package amazon_locker.notification;

import amazon_locker.entities.Parcel;
import amazon_locker.manager.LockerAssignment;

public class SmsNotificationSender implements NotificationSender {
    @Override
    public void send(Parcel parcel, LockerAssignment assignment) {
        System.out.printf(
                "[SMS to %s] Locker %s at station %s. Code: %s (expires %s)%n",
                parcel.getRecipientContact(), assignment.getLockerId(), assignment.getStationId(),
                assignment.getAccessCode(), assignment.getExpiryAt());
    }
}
