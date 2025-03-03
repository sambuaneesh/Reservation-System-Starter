package flight.reservation.journey;

import flight.reservation.Airport;
import flight.reservation.Passenger;
import flight.reservation.flight.ScheduledFlight;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class MultiFlightJourney implements Journey {
    private final List<Journey> journeys = new ArrayList<>();
    private List<Passenger> passengers = new ArrayList<>();

    public void addJourney(Journey journey) {
        if (!isValidAddition(journey)) {
            throw new IllegalArgumentException("Cannot add this journey: connections don't match or timing is invalid");
        }
        journeys.add(journey);
    }

    public void removeJourney(Journey journey) {
        journeys.remove(journey);
    }

    @Override
    public double getPrice() {
        return journeys.stream().mapToDouble(Journey::getPrice).sum();
    }

    @Override
    public Airport getDeparture() {
        if (journeys.isEmpty()) {
            return null;
        }
        return journeys.get(0).getDeparture();
    }

    @Override
    public Airport getArrival() {
        if (journeys.isEmpty()) {
            return null;
        }
        return journeys.get(journeys.size() - 1).getArrival();
    }

    @Override
    public Date getDepartureTime() {
        if (journeys.isEmpty()) {
            return null;
        }
        return journeys.get(0).getDepartureTime();
    }

    @Override
    public Date getArrivalTime() {
        if (journeys.isEmpty()) {
            return null;
        }
        return journeys.get(journeys.size() - 1).getArrivalTime();
    }

    @Override
    public List<Airport> getStops() {
        if (journeys.size() <= 1) {
            return new ArrayList<>();
        }

        List<Airport> stops = new ArrayList<>();
        for (int i = 0; i < journeys.size() - 1; i++) {
            stops.add(journeys.get(i).getArrival());
        }
        return stops;
    }

    @Override
    public List<ScheduledFlight> getFlights() {
        return journeys.stream()
                .flatMap(journey -> journey.getFlights().stream())
                .collect(Collectors.toList());
    }

    @Override
    public int getTotalDistance() {
        return journeys.stream().mapToInt(Journey::getTotalDistance).sum();
    }

    @Override
    public void addPassenger(Passenger passenger) {
        passengers.add(passenger);
        journeys.forEach(journey -> journey.addPassenger(passenger));
    }

    @Override
    public void removePassenger(Passenger passenger) {
        passengers.remove(passenger);
        journeys.forEach(journey -> journey.removePassenger(passenger));
    }

    @Override
    public List<Passenger> getPassengers() {
        return passengers;
    }

    @Override
    public int getAvailableCapacity() throws NoSuchFieldException {
        if (journeys.isEmpty()) {
            return 0;
        }

        return journeys.stream()
                .mapToInt(journey -> {
                    try {
                        return journey.getAvailableCapacity();
                    } catch (NoSuchFieldException e) {
                        return 0;
                    }
                })
                .min()
                .orElse(0);
    }

    private boolean isValidAddition(Journey journey) {
        if (journeys.isEmpty()) {
            return true;
        }

        Journey lastJourney = journeys.get(journeys.size() - 1);

        boolean airportsConnect = lastJourney.getArrival().equals(journey.getDeparture());
        boolean timingIsValid = journey.getDepartureTime().after(lastJourney.getArrivalTime());

        return airportsConnect && timingIsValid;
    }
}