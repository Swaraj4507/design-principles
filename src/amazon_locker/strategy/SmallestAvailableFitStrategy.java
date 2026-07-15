package amazon_locker.strategy;

import amazon_locker.entities.Locker;
import amazon_locker.entities.LockerStatus;
import amazon_locker.entities.Size;

import java.util.List;
import java.util.Optional;

// [15] Matches req #2 directly: among AVAILABLE lockers whose size fits the
//      parcel, pick the smallest one. Size's declaration order
//      (SMALL < MEDIUM < LARGE) doubles as its natural Comparable ordering,
//      so no separate ranking table is needed.
public class SmallestAvailableFitStrategy implements LockerFindingStrategy {
    @Override
    public Optional<Locker> findLocker(List<Locker> lockers, Size parcelSize) {
        Locker best = null;
        for (Locker locker : lockers) {
            if (locker.getStatus() != LockerStatus.AVAILABLE) {
                continue;
            }
            if (locker.getSize().compareTo(parcelSize) < 0) {
                continue; // too small to fit the parcel
            }
            if (best == null || locker.getSize().compareTo(best.getSize()) < 0) {
                best = locker;
            }
        }
        return Optional.ofNullable(best);
    }
}
