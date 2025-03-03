package flight.reservation.journey;

import flight.reservation.Airport;
import flight.reservation.Passenger;
import flight.reservation.flight.ScheduledFlight;

import java.util.Date;
import java.util.List;

public interface Journey {
    double getPrice();
    Airport getDeparture();
    Airport getArrival();
    Date getDepartureTime();
    Date getArrivalTime();
    List<Airport> getStops();
    List<ScheduledFlight> getFlights();
    int getTotalDistance();
    void addPassenger(Passenger passenger);
    void removePassenger(Passenger passenger);
    List<Passenger> getPassengers();
    int getAvailableCapacity() throws NoSuchFieldException;
}