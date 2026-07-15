package amazon_locker.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// [8] Container + identity only — no assignment logic here, that's
//     LockerManager + LockerFindingStrategy's job. Backed by a map (not a
//     List) so a locker can be looked up by id in O(1) for pickup, while
//     getLockers() still hands the strategy a List to scan when it needs to
//     compare sizes across all lockers.
public class LockerStation {
    private final String id;
    private final String address;
    private final Map<String, Locker> lockersById = new ConcurrentHashMap<>();

    public LockerStation(String id, String address) {
        this.id = id;
        this.address = address;
    }

    public String getId() {
        return id;
    }

    public String getAddress() {
        return address;
    }

    public void addLocker(Locker locker) {
        lockersById.put(locker.getId(), locker);
    }

    public Locker getLocker(String lockerId) {
        return lockersById.get(lockerId);
    }

    public List<Locker> getLockers() {
        return new ArrayList<>(lockersById.values());
    }
}
