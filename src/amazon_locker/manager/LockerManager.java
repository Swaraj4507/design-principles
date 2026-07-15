package amazon_locker.manager;

import amazon_locker.entities.Locker;
import amazon_locker.entities.LockerStation;
import amazon_locker.entities.Parcel;
import amazon_locker.notification.NotificationDispatcher;
import amazon_locker.strategy.LockerFindingStrategy;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class LockerManager {
    private static final int ACCESS_CODE_LENGTH = 6;

    private final Map<String, LockerStation> stationsById = new ConcurrentHashMap<>();
    private final LockerFindingStrategy findingStrategy;
    private final NotificationDispatcher notificationDispatcher;
    private final Duration accessWindow;
    private final SecureRandom random = new SecureRandom();

    public LockerManager(LockerFindingStrategy findingStrategy, NotificationDispatcher notificationDispatcher,
                          Duration accessWindow) {
        this.findingStrategy = findingStrategy;
        this.notificationDispatcher = notificationDispatcher;
        this.accessWindow = accessWindow;
    }

    public void addStation(LockerStation station) {
        stationsById.put(station.getId(), station);
    }

    // [11] Manager owns "which station" (O(1) map lookup); the strategy
    //      owns "which locker within that station" — keeps the two
    //      decisions independently swappable.
    // [12] Single attempt, no retry loop. Locker.assign() (synchronized +
    //      status recheck) already guarantees no double-assignment — that's
    //      the safety property, and it holds regardless of what the manager
    //      does here. A lost race just means this call returns empty; the
    //      caller decides whether/how to retry rather than the manager
    //      embedding a fixed attempt-count policy.
    public Optional<LockerAssignment> dropOffParcel(String stationId, Parcel parcel) {
        LockerStation station = stationsById.get(stationId);
        if (station == null) {
            return Optional.empty();
        }

        Optional<Locker> lockerOpt = findingStrategy.findLocker(station.getLockers(), parcel.getSize());
        if (lockerOpt.isEmpty()) {
            return Optional.empty();
        }

        Locker locker = lockerOpt.get();
        String accessCode = generateAccessCode();
        Instant expiryAt = Instant.now().plus(accessWindow);

        if (!locker.assign(parcel, accessCode, expiryAt)) {
            return Optional.empty();
        }
        LockerAssignment assignment = new LockerAssignment(station.getId(), locker.getId(), accessCode, expiryAt);
        // [21] Fires after assign() has already succeeded, so a notification
        //      failure can never leave a locker "occupied but never told to
        //      the customer" out of sync with what actually happened.
        notificationDispatcher.dispatch(parcel, assignment);
        return Optional.of(assignment);
    }

    // [13] O(1) station lookup + O(1) locker-within-station lookup (both
    //      map-backed), since pickup is a "look up by exact id" operation,
    //      not a search.
    public boolean pickup(String stationId, String lockerId, String accessCode) {
        LockerStation station = stationsById.get(stationId);
        if (station == null) {
            return false;
        }
        Locker locker = station.getLocker(lockerId);
        if (locker == null || !locker.isCodeValid(accessCode)) {
            return false;
        }
        locker.release();
        return true;
    }

    // [14] Exposed as a plain method rather than an internal background
    //      thread/scheduler, keeping LockerManager free of threading/
    //      scheduling concerns. A real deployment would invoke this from a
    //      cron job / scheduled executor (req #7: expire stale codes).
    public void releaseExpiredLockers() {
        for (LockerStation station : stationsById.values()) {
            for (Locker locker : station.getLockers()) {
                if (locker.isExpired()) {
                    locker.release();
                }
            }
        }
    }

    private String generateAccessCode() {
        StringBuilder code = new StringBuilder(ACCESS_CODE_LENGTH);
        for (int i = 0; i < ACCESS_CODE_LENGTH; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }
}
