package flight.reservation;

import flight.reservation.flight.ScheduledFlight;
import flight.reservation.observer.FlightObserver;
import flight.reservation.order.FlightOrder;
import flight.reservation.order.Order;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Customer implements FlightObserver {

    private String email;
    private String name;
    private List<Order> orders;
    private List<String> notifications = new ArrayList<>();

    public Customer(String name, String email) {
        this.name = name;
        this.email = email;
        this.orders = new ArrayList<>();
    }

    @Override
    public void update(ScheduledFlight flight, String message) {
        String notification = "Notification for flight " + flight.getNumber() +
                " from " + flight.getDeparture().getCode() +
                " to " + flight.getArrival().getCode() + ": " + message;

        notifications.add(notification);
        System.out.println("Customer " + name + " received: " + notification);
    }

    public FlightOrder createOrder(List<String> passengerNames, List<ScheduledFlight> flights, double price) {
        if (!isOrderValid(passengerNames, flights)) {
            throw new IllegalStateException("Order is not valid");
        }
        FlightOrder order = new FlightOrder(flights);
        order.setCustomer(this);
        order.setPrice(price);
        List<Passenger> passengers = passengerNames
                .stream()
                .map(Passenger::new)
                .collect(Collectors.toList());
        order.setPassengers(passengers);
        order.getScheduledFlights().forEach(scheduledFlight -> {
            scheduledFlight.addPassengers(passengers);
            scheduledFlight.registerObserver(this); // Register as observer for flight updates
        });
        orders.add(order);
        return order;
    }

    // Rest of the original methods remain the same
    private boolean isOrderValid(List<String> passengerNames, List<ScheduledFlight> flights) {
        boolean valid = true;
        valid = valid && !FlightOrder.getNoFlyList().contains(this.getName());
        valid = valid && passengerNames.stream().noneMatch(passenger -> FlightOrder.getNoFlyList().contains(passenger));
        valid = valid && flights.stream().allMatch(scheduledFlight -> {
            try {
                return scheduledFlight.getAvailableCapacity() >= passengerNames.size();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
                return false;
            }
        });
        return valid;
    }

    public List<String> getNotifications() {
        return notifications;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }
}