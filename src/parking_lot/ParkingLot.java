package parking_lot;

import java.util.ArrayList;
import java.util.HashMap;

public class ParkingLot {

    ArrayList<Level> levels = new ArrayList<>();

    HashMap<Vehicle,Level> vehicleLevelMap = new HashMap<>();


    void initiateLevel(int level,int carSpotCount, int truckSpotCount, int mcSpotCount){
        levels.add(level,new Level(level,carSpotCount,truckSpotCount,mcSpotCount, new SpotProvider()));
    }

    void parkVehicle(Vehicle v){

        for (Level level : levels){
            if (level.occupyAvailableSpot(v)){
                vehicleLevelMap.put(v,level);
                System.out.println("parking_lot.Vehicle Parked at level " + level.levelNo);
                return;
            }
        }

        throw new RuntimeException("no spots available");

    }

    void ejectVehicle(Vehicle v){
        if (vehicleLevelMap.containsKey(v)){
            Level lvl=vehicleLevelMap.get(v);
            lvl.vacateVehicle(v);
            vehicleLevelMap.remove(v);
        }
        else {
            throw  new RuntimeException("parking_lot.Vehicle doesn't exist");
        }
    }

    void getInfo(int level){
        Level lvl = levels.get(level);
        System.out.println(lvl.getInfo());

    }
    void getInfo(Level lvl){
        System.out.println(lvl.getInfo());

    }

    void getInfo(){
       for (Level lvl:levels){
           getInfo(lvl);
       }
    }

}
