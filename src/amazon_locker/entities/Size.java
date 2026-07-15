package amazon_locker.entities;

// [1] Shared by both Locker and Parcel so "does this parcel fit" is a single
//     enum comparison instead of translating between two separate scales.
public enum Size {
    SMALL,
    MEDIUM,
    LARGE
}
