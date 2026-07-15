package amazon_locker.strategy;

import amazon_locker.entities.Locker;
import amazon_locker.entities.LockerStatus;
import amazon_locker.entities.Size;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

// [15] Matches req #2 directly: among AVAILABLE lockers whose size fits the
//      parcel, pick the smallest one. Size's declaration order
//      (SMALL < MEDIUM < LARGE) doubles as its natural Comparable ordering,
//      so no separate ranking table is needed.
public class SmallestAvailableFitStrategy implements LockerFindingStrategy {
    @Override
    public Optional<Locker> findLocker(List<Locker> lockers, Size parcelSize) {
        return lockers.stream()
                .filter(locker -> locker.getStatus() == LockerStatus.AVAILABLE)
                .filter(locker -> locker.getSize().compareTo(parcelSize) >= 0)
                .min(Comparator.comparing(Locker::getSize));
    }
}
