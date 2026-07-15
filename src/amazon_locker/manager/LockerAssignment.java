package amazon_locker.manager;

import java.time.Instant;

// [10] Locker deliberately doesn't expose its raw access code via a getter
//      (only isCodeValid()), so nothing holding a Locker reference can read
//      a live code. LockerManager generates the code, so it hands it back
//      here as the one-time "receipt" needed to notify the customer.
public class LockerAssignment {
    private final String stationId;
    private final String lockerId;
    private final String accessCode;
    private final Instant expiryAt;

    public LockerAssignment(String stationId, String lockerId, String accessCode, Instant expiryAt) {
        this.stationId = stationId;
        this.lockerId = lockerId;
        this.accessCode = accessCode;
        this.expiryAt = expiryAt;
    }

    public String getStationId() {
        return stationId;
    }

    public String getLockerId() {
        return lockerId;
    }

    public String getAccessCode() {
        return accessCode;
    }

    public Instant getExpiryAt() {
        return expiryAt;
    }
}
