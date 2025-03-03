package flight.reservation.journey;

import flight.reservation.Airport;
import flight.reservation.Passenger;
import flight.reservation.flight.ScheduledFlight;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class SingleFlightJourney implements Journey {
    private final ScheduledFlight flight;

    public SingleFlightJourney(ScheduledFlight flight) {
        this.flight = flight;
    }

    @Override
    public double getPrice() {
        return flight.getCurrentPrice();
    }

    @Override
    public Airport getDeparture() {
        return flight.getDeparture();
    }

    @Override
    public Airport getArrival() {
        return flight.getArrival();
    }

    @Override
    public Date getDepartureTime() {
        return flight.getDepartureTime();
    }

    @Override
    public Date getArrivalTime() {
        long estimatedFlightTimeInMillis = calculateEstimatedFlightTime();
        return new Date(flight.getDepartureTime().getTime() + estimatedFlightTimeInMillis);
    }

    @Override
    public List<Airport> getStops() {
        return new ArrayList<>();
    }

    @Override
    public List<ScheduledFlight> getFlights() {
        return Arrays.asList(flight);
    }

    @Override
    public int getTotalDistance() {
        return calculateDistance(flight.getDeparture(), flight.getArrival());
    }

    @Override
    public void addPassenger(Passenger passenger) {
        flight.addPassengers(Arrays.asList(passenger));
    }

    @Override
    public void removePassenger(Passenger passenger) {
        flight.removePassengers(Arrays.asList(passenger));
    }

    @Override
    public List<Passenger> getPassengers() {
        return flight.getPassengers();
    }

    @Override
    public int getAvailableCapacity() throws NoSuchFieldException {
        return flight.getAvailableCapacity();
    }

    private long calculateEstimatedFlightTime() {
        int distance = getTotalDistance();
        return (long) (distance / 800.0 * 60 * 60 * 1000);
    }

    private int calculateDistance(Airport departure, Airport arrival) {
        return 500;
    }
}