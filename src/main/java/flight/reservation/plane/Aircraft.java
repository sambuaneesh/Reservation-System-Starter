package flight.reservation.plane;

public interface Aircraft {
    String getModel();
    int getPassengerCapacity() throws NoSuchFieldException;
    int getCrewCapacity() throws NoSuchFieldException;
}