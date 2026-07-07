public class SpotProvider {
    // Prototype pattern
    Spot carSpot = new Spot(VEHICLE_TYPE.CAR.getSize());
    Spot motorCycleSpot = new Spot(VEHICLE_TYPE.MC.getSize());
    Spot truckSpot= new Spot(VEHICLE_TYPE.TRUCK.getSize());

    Spot provide(VEHICLE_TYPE spotType){
        return switch (spotType) {
            case CAR -> carSpot.copy();
            case MC-> motorCycleSpot.copy();
            case TRUCK -> truckSpot.copy();
            };
    }
}
