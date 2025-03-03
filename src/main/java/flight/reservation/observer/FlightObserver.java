package flight.reservation.observer;

import flight.reservation.flight.ScheduledFlight;

public interface FlightObserver {
    void update(ScheduledFlight flight, String message);
}