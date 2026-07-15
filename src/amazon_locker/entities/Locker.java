package amazon_locker.entities;

import java.time.Instant;

public class Locker {
    private final String id;
    private final Size size;
    private LockerStatus status;
    private Parcel occupiedBy;
    private String accessCode;
    private Instant expiryAt;

    public Locker(String id, Size size) {
        this.id = id;
        this.size = size;
        this.status = LockerStatus.AVAILABLE;
    }

    public String getId() {
        return id;
    }

    public Size getSize() {
        return size;
    }

    public LockerStatus getStatus() {
        return status;
    }

    public Parcel getOccupiedBy() {
        return occupiedBy;
    }

    // [4] Assigns parcel + code + expiry together and flips status in one
    //     method, so a Locker can never be observed OCCUPIED without the
    //     rest of its assignment fields set.
    // [5] synchronized + re-checking status here (not just trusting whatever
    //     the caller/strategy already saw) is the last line of defense
    //     against two threads racing to grab the same locker after both saw
    //     it as available (req #9: no double-assignment under concurrency).
    public synchronized boolean assign(Parcel parcel, String accessCode, Instant expiryAt) {
        if (status != LockerStatus.AVAILABLE) {
            return false;
        }
        this.occupiedBy = parcel;
        this.accessCode = accessCode;
        this.expiryAt = expiryAt;
        this.status = LockerStatus.OCCUPIED;
        return true;
    }

    // [6] Mirrors assign(): clears every assignment field together so the
    //     locker never lingers in a half-released state.
    public synchronized void release() {
        this.occupiedBy = null;
        this.accessCode = null;
        this.expiryAt = null;
        this.status = LockerStatus.AVAILABLE;
    }

    // [7] Code match and expiry check both live here rather than in the
    //     caller, since req #5 (wrong code rejected) and req #7 (expired
    //     code rejected) are really the same "is this code still valid"
    //     question.
    public synchronized boolean isCodeValid(String code) {
        if (status != LockerStatus.OCCUPIED) {
            return false;
        }
        return accessCode.equals(code) && Instant.now().isBefore(expiryAt);
    }

    public synchronized boolean isExpired() {
        return status == LockerStatus.OCCUPIED && Instant.now().isAfter(expiryAt);
    }
}
