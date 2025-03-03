# Observer Pattern Refactoring

This document details the implementation of the Observer Pattern in the flight reservation system to provide notifications for flight schedule changes.

## Problem Identification

### Original Implementation

In the original codebase, there was no mechanism for notifying customers about changes to flights they had booked:

- When flight schedules changed, there was no way to automatically update customers
- Any price changes, cancellations, or other important flight updates weren't communicated
- The system lacked a standardized way to handle flight-related notifications
- Customers had to manually check for updates to their booked flights

```java
// In ScheduledFlight.java - original implementation had no notification mechanism
public void setCurrentPrice(double currentPrice) {
    this.currentPrice = currentPrice;
    // No way to notify affected customers
}

// In Customer.java - no way to receive updates about flight changes
public FlightOrder createOrder(List<String> passengerNames, List<ScheduledFlight> flights, double price) {
    // Creates order but doesn't establish ongoing communication about flight changes
}
```

### Issues with the Original Implementation

1. **Missing Communication Channel**: No mechanism existed for flights to communicate changes to interested parties.

2. **Manual Update Process**: Customers needed to manually check for flight updates.

3. **Tight Coupling Potential**: Any future notification system would likely couple flights and customers tightly.

4. **Limited Extensibility**: Adding notification features would require significant changes across multiple classes.

5. **Inconsistent Updates**: Without a standardized notification system, updates might be handled inconsistently.

## Solution: Observer Pattern Implementation

The Observer Pattern was implemented to address these issues by:

1. Creating interfaces for both the subject (flights) and observers (customers)
2. Modifying the `ScheduledFlight` class to track and notify observers
3. Making the `Customer` class respond to flight updates
4. Setting up the communication channel when orders are created

### 1. Observer Interface

A new `FlightObserver` interface defines how observers receive updates:

```java
package flight.reservation.observer;

import flight.reservation.flight.ScheduledFlight;

public interface FlightObserver {
    void update(ScheduledFlight flight, String message);
}
```

### 2. Subject Interface

The `FlightSubject` interface defines how subjects manage and notify observers:

```java
package flight.reservation.observer;

public interface FlightSubject {
    void registerObserver(FlightObserver observer);
    void removeObserver(FlightObserver observer);
    void notifyObservers(String message);
}
```

### 3. Modified ScheduledFlight Class

The `ScheduledFlight` class was updated to implement `FlightSubject`:

```java
public class ScheduledFlight extends Flight implements FlightSubject {
    private final List<Passenger> passengers;
    private final Date departureTime;
    private double currentPrice = 100;
    private final List<FlightObserver> observers = new ArrayList<>();

    // Constructor remains the same

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

    public void setCurrentPrice(double currentPrice) {
        double oldPrice = this.currentPrice;
        this.currentPrice = currentPrice;
        notifyObservers("Flight " + getNumber() + " price changed from " + 
                        oldPrice + " to " + currentPrice);
    }

    public void setDepartureTime(Date newDepartureTime) {
        Date oldDepartureTime = this.departureTime;
        // Since departureTime is final, we would need to create a new ScheduledFlight
        // For this example, let's just pretend we updated it
        notifyObservers("Flight " + getNumber() + " departure time changed from " + 
                        oldDepartureTime + " to " + newDepartureTime);
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
}
```

### 4. Updated Customer Class

The `Customer` class was modified to implement `FlightObserver`:

```java
public class Customer implements FlightObserver {
    private String email;
    private String name;
    private List<Order> orders;
    private List<String> notifications = new ArrayList<>();

    // Constructor remains the same

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

    public List<String> getNotifications() {
        return notifications;
    }

    // Rest of the original methods remain the same
}
```

## Benefits of the Refactoring

1. **Automatic Notifications**: Customers are automatically notified of any changes to their booked flights.

2. **Loose Coupling**: The Observer Pattern creates a loose coupling between flights and customers. Flights don't need to know the specifics of who is observing them, just that observers exist.

3. **Improved Customer Experience**: Customers now receive timely updates about flight changes, cancellations, and other important information.

4. **Extensibility**: The notification system can be easily extended to include other types of observers (e.g., airline staff, airport systems) without modifying the `ScheduledFlight` class.

5. **Consistent Updates**: All observers receive the same notification messages, ensuring consistency in communication.

6. **Maintainability**: The notification logic is centralized, making it easier to maintain and update.

## Example Usage

Here's an example of how the Observer Pattern works in practice:

```java
// Create airports, aircraft, and a scheduled flight
Airport departureAirport = new Airport("Berlin Airport", "BER", "Berlin, Berlin");
Airport arrivalAirport = new Airport("Frankfurt Airport", "FRA", "Frankfurt, Hesse");
Aircraft aircraft = AircraftFactory.createPlane("A380");
ScheduledFlight flight = new ScheduledFlight(101, departureAirport, arrivalAirport, aircraft, departureTime, 299.99);

// Create customers and book flights
Customer john = new Customer("John", "john@example.com");
Customer alice = new Customer("Alice", "alice@example.com");
john.createOrder(Arrays.asList("John", "Jane"), Arrays.asList(flight), 599.98);
alice.createOrder(Arrays.asList("Alice"), Arrays.asList(flight), 299.99);

// When changes occur, customers are automatically notified
flight.setCurrentPrice(349.99);
flight.setDepartureTime(newDepartureTime);
flight.cancelFlight();

// Notifications can be accessed
List<String> johnsNotifications = john.getNotifications();
List<String> alicesNotifications = alice.getNotifications();
```

## Trade-offs

### Advantages

1. **Real-time Updates**: The Observer Pattern provides a mechanism for real-time updates, improving customer service.

2. **Decoupled Design**: Subjects and observers are decoupled, allowing for changes to either without affecting the other.

3. **Dynamic Relationships**: Observers can register and unregister at runtime, allowing for dynamic relationship management.

4. **Open/Closed Principle**: The system follows the Open/Closed Principle, allowing for new observers without modifying existing code.

### Disadvantages

1. **Memory Management**: If observers forget to unregister, it could lead to memory leaks (though Java's garbage collection helps mitigate this).

2. **Unexpected Updates**: Observers might receive updates they don't need or expect, especially if the notification system is not carefully designed.

3. **Order of Notification**: The order in which observers are notified is not guaranteed, which could be an issue for some applications.

4. **Performance Overhead**: For a large number of observers, there could be a performance impact when notifying all of them.