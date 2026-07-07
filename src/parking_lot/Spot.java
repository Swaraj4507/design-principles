package parking_lot;

import java.time.LocalDateTime;

public class Spot {

    // this should support a specific type of vehicle

    private volatile Vehicle vehicle;
    private final int capacity;
    private LocalDateTime parkingTime;

    Spot(int capacity){
        this.capacity=capacity;
    }

    synchronized boolean isOccupied(){
        return vehicle!=null;
    }

    boolean canAccommodate(Vehicle v){
        return v.getSize()==capacity; // this we can change to <=
    }

    Spot copy(){
        return  new Spot(this.capacity);
    }

    synchronized boolean occupy(Vehicle v){
        if(vehicle!=null || !canAccommodate(v)) return false;
        this.vehicle=v;
        this.parkingTime= LocalDateTime.now();
        return true;
    }

    synchronized LocalDateTime vacate(){
        LocalDateTime pTime= this.parkingTime;
        this.vehicle=null;
        this.parkingTime=null;
        return pTime;
    }
}
