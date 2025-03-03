package flight.reservation.plane;

public class AircraftFactory {

    public static Aircraft createAircraft(String type, String model) {
        switch (type.toLowerCase()) {
            case "plane":
                return new PassengerPlane(model);
            case "helicopter":
                return new Helicopter(model);
            case "drone":
                return new PassengerDrone(model);
            default:
                throw new IllegalArgumentException("Unknown aircraft type: " + type);
        }
    }

    // Convenience methods
    public static Aircraft createPlane(String model) {
        return new PassengerPlane(model);
    }

    public static Aircraft createHelicopter(String model) {
        return new Helicopter(model);
    }

    public static Aircraft createDrone(String model) {
        return new PassengerDrone(model);
    }
}