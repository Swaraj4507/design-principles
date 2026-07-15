package amazon_locker.notification;

import amazon_locker.entities.Parcel;
import amazon_locker.manager.LockerAssignment;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

// [18] Owns the fan-out to every registered channel so LockerManager just
//      calls dispatch(...) once and doesn't need to know how many channels
//      exist or loop over them itself. This is really the Observer pattern
//      (every registered sender is notified of the same event) — the
//      difference from LockerFindingStrategy is that here we call *every*
//      implementation instead of picking exactly one.
// [19] Method name is dispatch(), not notifyAll() — avoids reading like a
//      call to Object.notifyAll(), which has nothing to do with this class.
// [20] Each sender wrapped in try/catch so one broken channel (e.g. an
//      email provider outage) can't stop other channels (e.g. SMS) from
//      still reaching the customer.
public class NotificationDispatcher {
    private final List<NotificationSender> senders = new CopyOnWriteArrayList<>();

    public void register(NotificationSender sender) {
        senders.add(sender);
    }

    public void dispatch(Parcel parcel, LockerAssignment assignment) {
        for (NotificationSender sender : senders) {
            try {
                sender.send(parcel, assignment);
            } catch (RuntimeException e) {
                // a single channel failing shouldn't block the rest
            }
        }
    }
}
