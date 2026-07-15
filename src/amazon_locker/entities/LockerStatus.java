package amazon_locker.entities;

// [2] Enum instead of boolean so a future state (e.g. OUT_OF_SERVICE) can be
//     added later without touching every place that flips true/false today.
public enum LockerStatus {
    AVAILABLE,
    OCCUPIED
}
