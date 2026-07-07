package parking_lot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Level {
    final int levelNo;
    final int carSpotCapacity;
    final int truckSpotCapacity;
    final int mcSpotCapacity;
    List<Spot> spots=new ArrayList<>();
    HashMap<Vehicle,Spot> vehicleSpotMap=new HashMap<>();

    int carSpotFilled=0;
    int truckSpotFilled=0;
    int mcSpotFilled=0;
    Level(int levelNo,int carSpotCount, int truckSpotCount, int mcSpotCount , SpotProvider spotProvider){
        this.levelNo=levelNo;
        carSpotCapacity=carSpotCount;
        truckSpotCapacity=truckSpotCount;
        mcSpotCapacity=mcSpotCount;
        while (carSpotCount>0){
            assert spots != null;
            spots.add(spotProvider.provide(VEHICLE_TYPE.CAR));
            carSpotCount--;
        }
        while (truckSpotCount>0){
            assert spots != null;
            spots.add(spotProvider.provide(VEHICLE_TYPE.TRUCK));
            truckSpotCount--;
        }
        while (mcSpotCount>0){
            assert spots != null;
            spots.add(spotProvider.provide(VEHICLE_TYPE.MC));
            mcSpotCount--;
        }

    }


    Spot getAvailableSpot(Vehicle v){
        if(!isAvailable(v)) return null;
        for (Spot spot:spots){
            if (spot.canAccommodate(v)) return spot;
        }
        return null;
    }

    boolean occupyAvailableSpot(Vehicle v){
        Spot avlSpot = getAvailableSpot(v);
        if (avlSpot!=null && avlSpot.occupy(v)){
            vehicleSpotMap.put(v,avlSpot);
            increaseSpecificSpotCount(v.getVehicleType());
            return true;
        }
        return false;
    }



    void vacateVehicle(Vehicle v){
        Spot occupiedSpot = vehicleSpotMap.get(v);
        if(occupiedSpot==null) throw new RuntimeException("Vehicle is not parked in this level");
        occupiedSpot.vacate();
        vehicleSpotMap.remove(v);
        decreaseSpecificSpotCount(v.getVehicleType());

    }

    boolean isAvailable(Vehicle v){
        return switch (v.getVehicleType()) {
            case VEHICLE_TYPE.CAR -> carSpotFilled < carSpotCapacity;
            case VEHICLE_TYPE.MC -> mcSpotFilled < mcSpotCapacity;
            case VEHICLE_TYPE.TRUCK -> truckSpotFilled < truckSpotCapacity;
        };
    }

    void increaseSpecificSpotCount(VEHICLE_TYPE v){
        switch (v){
            case CAR -> carSpotFilled++;
            case MC -> mcSpotFilled++;
            case TRUCK -> truckSpotFilled++;
        }
    }
    void decreaseSpecificSpotCount(VEHICLE_TYPE v){
        switch (v){
            case CAR -> carSpotFilled--;
            case MC -> mcSpotFilled--;
            case TRUCK -> truckSpotFilled--;
        }
    }

    Map<VEHICLE_TYPE,Integer> getInfo(){
        return Map.of(VEHICLE_TYPE.CAR,carSpotCapacity-carSpotFilled,VEHICLE_TYPE.MC,mcSpotCapacity-mcSpotFilled,VEHICLE_TYPE.TRUCK,truckSpotCapacity-truckSpotFilled);
    }



}
