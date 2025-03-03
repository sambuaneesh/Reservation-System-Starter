# Composite Pattern Implementation

This document details the implementation of the Composite Pattern in the flight reservation system to handle both simple direct flights and complex multi-leg journeys.

## Problem Identification

### Original Implementation

In the original codebase, the system was only designed to handle individual flights:

```java
// Booking a single flight
FlightOrder order = customer.createOrder(passengerNames, Arrays.asList(flight), price);
```

There was no structured way to handle more complex travel itineraries consisting of multiple connecting flights. Each flight had to be booked separately, and there was no concept of a unified journey.

### Issues with the Original Implementation

1. **Limited Support for Complex Itineraries**: The system could not represent multi-leg journeys as a cohesive unit.

2. **Inconsistent Handling**: Direct flights and connecting flights would need different handling logic in client code.

3. **Difficulty in Calculating Journey Properties**: Properties like total price, travel time, and distance for complex itineraries would need to be calculated manually.

4. **Passenger Management Complexity**: Adding or removing passengers from a multi-leg journey would require operations on each individual flight.

5. **No Unified Interface**: There was no common interface to work with both simple and complex travel arrangements.

## Solution: Composite Pattern Implementation

The Composite Pattern was implemented to address these issues by:

1. Creating a common `Journey` interface for both simple flights and complex itineraries
2. Implementing a `SingleFlightJourney` for direct flights
3. Implementing a `MultiFlightJourney` that can contain multiple journeys
4. Adding a factory to create different types of journeys

### 1. Journey Interface

A common `Journey` interface defines operations for all journey types:

```java
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
```

### 2. SingleFlightJourney Implementation

The `SingleFlightJourney` class represents a journey with a single direct flight:

```java
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
        return (long) (distance / 800.0 * 60 * 60 * 1000); // Estimate based on average speed
    }
    
    private int calculateDistance(Airport departure, Airport arrival) {
        return 500; // Simplified implementation
    }
}
```

### 3. MultiFlightJourney Implementation

The `MultiFlightJourney` class represents a complex journey with multiple legs:

```java
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
```

### 4. JourneyFactory Implementation

A factory to create different types of journeys:

```java
package flight.reservation.journey;

import flight.reservation.flight.ScheduledFlight;

import java.util.List;

public class JourneyFactory {
    
    public static Journey createSingleFlightJourney(ScheduledFlight flight) {
        return new SingleFlightJourney(flight);
    }
    
    public static Journey createMultiFlightJourney(List<ScheduledFlight> flights) {
        MultiFlightJourney multiFlightJourney = new MultiFlightJourney();
        
        for (ScheduledFlight flight : flights) {
            multiFlightJourney.addJourney(new SingleFlightJourney(flight));
        }
        
        return multiFlightJourney;
    }
    
    public static Journey createFromJourneys(List<Journey> journeys) {
        MultiFlightJourney multiFlightJourney = new MultiFlightJourney();
        
        for (Journey journey : journeys) {
            multiFlightJourney.addJourney(journey);
        }
        
        return multiFlightJourney;
    }
}
```

## Benefits of the Implementation

1. **Unified Interface**: Both simple and complex journeys can be treated through the same interface.

2. **Simplified Client Code**: Client code can work with journeys without knowing whether they're dealing with a single flight or a multi-leg journey.

3. **Recursive Composition**: Complex journeys can be built from both single flights and other complex journeys.

4. **Automatic Calculations**: Properties like total price, travel time, and distance are calculated automatically for complex itineraries.

5. **Simplified Passenger Management**: Passengers can be added or removed from an entire journey with a single operation.

6. **Connection Validation**: The `MultiFlightJourney` class validates that connections make sense (airports match and times align).

7. **Extensibility**: New journey types can be added by implementing the `Journey` interface.

## Example Usage

```java
// Create airports and flights
Airport nyc = new Airport("JFK", "JFK", "New York");
Airport lhr = new Airport("Heathrow", "LHR", "London");
Airport cdg = new Airport("Charles de Gaulle", "CDG", "Paris");

ScheduledFlight nycToLhr = new ScheduledFlight(101, nyc, lhr, aircraft, departureTime1);
ScheduledFlight lhrToCdg = new ScheduledFlight(202, lhr, cdg, aircraft, departureTime2);

// Create a simple direct journey
Journey directFlight = JourneyFactory.createSingleFlightJourney(nycToLhr);

// Create a complex journey with connecting flights
Journey connectingFlight = JourneyFactory.createMultiFlightJourney(Arrays.asList(nycToLhr, lhrToCdg));

// Or build it step by step
MultiFlightJourney customJourney = new MultiFlightJourney();
customJourney.addJourney(new SingleFlightJourney(nycToLhr));
customJourney.addJourney(new SingleFlightJourney(lhrToCdg));

// Work with journeys uniformly
System.out.println("Direct flight price: " + directFlight.getPrice());
System.out.println("Connecting flight price: " + connectingFlight.getPrice());

System.out.println("Direct flight stops: " + directFlight.getStops().size());
System.out.println("Connecting flight stops: " + connectingFlight.getStops().size());

// Add a passenger to the entire journey
Passenger passenger = new Passenger("John Doe");
connectingFlight.addPassenger(passenger);
```

## Trade-offs

### Advantages

1. **Simplified Client Code**: The Composite Pattern simplifies client code by providing a unified way to work with both simple and complex structures.

2. **Recursive Composition**: The pattern allows for recursive composition, enabling complex hierarchies to be built.

3. **Extensibility**: New journey types can be easily added without changing existing code.

4. **Operation Distribution**: Operations applied to composite objects are automatically distributed to all components.

### Disadvantages

1. **Component Constraints**: Sometimes components in a composite structure need different behaviors, which can be difficult to handle through a common interface.

2. **Performance Overhead**: For very large composite structures, there could be performance overhead when traversing the entire structure.

3. **Design Complexity**: The pattern adds a layer of abstraction, which may increase design complexity for simple use cases.

4. **Validation Challenges**: Ensuring that all components in a composite structure maintain valid relationships can be challenging.