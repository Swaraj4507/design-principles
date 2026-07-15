package amazon_locker.strategy;

import amazon_locker.entities.Locker;
import amazon_locker.entities.Size;

import java.util.List;
import java.util.Optional;

// [9] Interface only for now — SmallestAvailableFitStrategy / ExactFitStrategy
//     implementations come next. LockerManager is written against this
//     abstraction so swapping the fit policy later touches nothing else.
public interface LockerFindingStrategy {
    Optional<Locker> findLocker(List<Locker> lockers, Size parcelSize);
}
