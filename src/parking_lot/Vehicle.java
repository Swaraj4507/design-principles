package parking_lot;

public abstract class Vehicle {
    private final int size;
    private final VEHICLE_TYPE vehicleType;
    Vehicle( VEHICLE_TYPE vehicleType){
        this.size=vehicleType.getSize();
        this.vehicleType=vehicleType;
    }

    int getSize(){
        return size;
    }

    VEHICLE_TYPE getVehicleType(){
        return vehicleType;
    }
}
