package parking_lot;

public enum VEHICLE_TYPE {

    CAR(4),
    MC(2),
    TRUCK(8);
    private final int size;

    VEHICLE_TYPE(int size) {
        this.size = size;
    }

    public int getSize() {
        return size;
    }
}
