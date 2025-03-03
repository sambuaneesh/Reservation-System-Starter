package flight.reservation.order;

import flight.reservation.Customer;
import flight.reservation.Passenger;

import java.util.List;
import java.util.UUID;

public abstract class Order {
    private final UUID id;
    private double price;
    private boolean isClosed = false;
    private Customer customer;
    private List<Passenger> passengers;

    public Order() {
        this.id = UUID.randomUUID();
    }

    // Template method defining the skeleton of order processing
    public final boolean processOrder() throws IllegalStateException {
        if (isClosed()) {
            return true;
        }

        if (!validateOrder()) {
            throw new IllegalStateException("Order validation failed.");
        }

        if (!processPayment()) {
            return false;
        }

        finalizeOrder();
        return true;
    }

    // Abstract methods to be implemented by subclasses
    protected abstract boolean validateOrder();
    protected abstract boolean processPayment();

    // Hook method with default implementation
    protected void finalizeOrder() {
        setClosed();
    }

    public UUID getId() {
        return id;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public List<Passenger> getPassengers() {
        return passengers;
    }

    public void setPassengers(List<Passenger> passengers) {
        this.passengers = passengers;
    }

    public boolean isClosed() {
        return isClosed;
    }

    public void setClosed() {
        isClosed = true;
    }
}