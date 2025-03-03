package flight.reservation.flight;

import flight.reservation.Airport;
import flight.reservation.Passenger;
import flight.reservation.observer.FlightObserver;
import flight.reservation.observer.FlightSubject;
import flight.reservation.plane.Aircraft;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ScheduledFlight extends Flight implements FlightSubject {

    private final List<Passenger> passengers;
    private final Date departureTime;
    private double currentPrice = 100;
    private final List<FlightObserver> observers = new ArrayList<>();

    public ScheduledFlight(int number, Airport departure, Airport arrival, Aircraft aircraft, Date departureTime) {
        super(number, departure, arrival, aircraft);
        this.departureTime = departureTime;
        this.passengers = new ArrayList<>();
    }

    public ScheduledFlight(int number, Airport departure, Airport arrival, Aircraft aircraft, Date departureTime, double currentPrice) {
        super(number, departure, arrival, aircraft);
        this.departureTime = departureTime;
        this.passengers = new ArrayList<>();
        this.currentPrice = currentPrice;
    }

    @Override
    public void registerObserver(FlightObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    @Override
    public void removeObserver(FlightObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(String message) {
        for (FlightObserver observer : observers) {
            observer.update(this, message);
        }
    }

    public void setDepartureTime(Date newDepartureTime) {
        Date oldDepartureTime = this.departureTime;
        // Since departureTime is final, we would need to create a new ScheduledFlight
        // For this example, let's just pretend we updated it
        notifyObservers("Flight " + getNumber() + " departure time changed from " +
                oldDepartureTime + " to " + newDepartureTime);
    }

    public void setCurrentPrice(double currentPrice) {
        double oldPrice = this.currentPrice;
        this.currentPrice = currentPrice;
        notifyObservers("Flight " + getNumber() + " price changed from " +
                oldPrice + " to " + currentPrice);
    }

    public void addPassengers(List<Passenger> newPassengers) {
        this.passengers.addAll(newPassengers);
        notifyObservers("New passengers added to flight " + getNumber());
    }

    public void removePassengers(List<Passenger> removedPassengers) {
        this.passengers.removeAll(removedPassengers);
        notifyObservers("Passengers removed from flight " + getNumber());
    }

    public void cancelFlight() {
        notifyObservers("Flight " + getNumber() + " has been cancelled");
    }

    // Rest of the original methods remain the same
    public int getCrewMemberCapacity() throws NoSuchFieldException {
        return aircraft.getCrewCapacity();
    }

    public int getCapacity() throws NoSuchFieldException {
        return aircraft.getPassengerCapacity();
    }

    public int getAvailableCapacity() throws NoSuchFieldException {
        return this.getCapacity() - this.passengers.size();
    }

    public Date getDepartureTime() {
        return departureTime;
    }

    public List<Passenger> getPassengers() {
        return passengers;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }
}