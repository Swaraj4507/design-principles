package amazon_locker.notification;

import amazon_locker.entities.Parcel;
import amazon_locker.manager.LockerAssignment;

public class EmailNotificationSender implements NotificationSender {
    @Override
    public void send(Parcel parcel, LockerAssignment assignment) {
        System.out.printf(
                "[EMAIL to %s] Your parcel is in locker %s at station %s. Code: %s (expires %s)%n",
                parcel.getRecipientContact(), assignment.getLockerId(), assignment.getStationId(),
                assignment.getAccessCode(), assignment.getExpiryAt());
    }
}
