package flight.reservation.observer;

public interface FlightSubject {
    void registerObserver(FlightObserver observer);
    void removeObserver(FlightObserver observer);
    void notifyObservers(String message);
}