package amazon_locker.strategy;

import amazon_locker.entities.Locker;
import amazon_locker.entities.LockerStatus;
import amazon_locker.entities.Size;

import java.util.List;
import java.util.Optional;

// [16] Stricter policy: only a locker of the exact same size qualifies.
//      Trades off a lower drop-off success rate for better utilization —
//      e.g. keeps LARGE lockers free for LARGE parcels instead of letting
//      SmallestAvailableFitStrategy hand a LARGE locker to a SMALL parcel.
public class ExactFitStrategy implements LockerFindingStrategy {
    @Override
    public Optional<Locker> findLocker(List<Locker> lockers, Size parcelSize) {
        return lockers.stream()
                .filter(locker -> locker.getStatus() == LockerStatus.AVAILABLE)
                .filter(locker -> locker.getSize() == parcelSize)
                .findFirst();
    }
}
