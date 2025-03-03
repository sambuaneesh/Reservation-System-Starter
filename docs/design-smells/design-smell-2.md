# Factory Method Pattern Refactoring

See [Refactoring Guru](https://refactoring.guru/design-patterns/factory-method). This document details the implementation of the Factory Method Pattern in this repository to improve the aircraft class hierarchy and management.

## Problem Identification

### Original Implementation

In the original codebase, the aircraft types (`PassengerPlane`, `Helicopter`, and `PassengerDrone`) had inconsistent interfaces and were handled using type checking and casting in the `Flight` and `ScheduledFlight` classes:

```java
// In Flight.java
private boolean isAircraftValid(Airport airport) {
    return Arrays.stream(airport.getAllowedAircrafts()).anyMatch(x -> {
        String model;
        if (this.aircraft instanceof PassengerPlane) {
            model = ((PassengerPlane) this.aircraft).model;
        } else if (this.aircraft instanceof Helicopter) {
            model = ((Helicopter) this.aircraft).getModel();
        } else if (this.aircraft instanceof PassengerDrone) {
            model = "HypaHype";
        } else {
            throw new IllegalArgumentException(String.format("Aircraft is not recognized"));
        }
        return x.equals(model);
    });
}

// In ScheduledFlight.java
public int getCrewMemberCapacity() throws NoSuchFieldException {
    if (this.aircraft instanceof PassengerPlane) {
        return ((PassengerPlane) this.aircraft).crewCapacity;
    }
    if (this.aircraft instanceof Helicopter) {
        return 2;
    }
    if (this.aircraft instanceof PassengerDrone) {
        return 0;
    }
    throw new NoSuchFieldException("this aircraft has no information about its crew capacity");
}

public int getCapacity() throws NoSuchFieldException {
    if (this.aircraft instanceof PassengerPlane) {
        return ((PassengerPlane) this.aircraft).passengerCapacity;
    }
    if (this.aircraft instanceof Helicopter) {
        return ((Helicopter) this.aircraft).getPassengerCapacity();
    }
    if (this.aircraft instanceof PassengerDrone) {
        return 4;
    }
    throw new NoSuchFieldException("this aircraft has no information about its capacity");
}
```

### Issues with the Original Implementation

1. **Type Checking**: The code relied on `instanceof` checks, which is a sign of poor design and violates the Open/Closed Principle.

2. **Inconsistent Interfaces**: The aircraft classes had inconsistent methods and properties. For example, `PassengerPlane` used public fields while `Helicopter` used private fields with getters.

3. **Tight Coupling**: The `Flight` and `ScheduledFlight` classes were tightly coupled to the concrete aircraft implementations.

4. **Code Duplication**: Similar type-checking logic was duplicated across multiple methods.

5. **Hard-coded Values**: Some values like the crew capacity for helicopters and passenger capacity for drones were hard-coded in multiple places.

6. **Maintainability Issues**: Adding a new aircraft type would require modifying the existing code in multiple places.

## Solution: Factory Method Pattern Implementation

The Factory Method Pattern was implemented to address these issues by:

1. Creating a common interface for all aircraft types
2. Refactoring existing aircraft classes to implement this interface
3. Creating a factory class to handle aircraft creation
4. Updating client code to use the interface instead of concrete types

### 1. Aircraft Interface

A new `Aircraft` interface was created to define the contract for all aircraft types:

```java
public interface Aircraft {
    String getModel();
    int getPassengerCapacity() throws NoSuchFieldException;
    int getCrewCapacity() throws NoSuchFieldException;
}
```

### 2. Refactored Aircraft Classes

#### PassengerPlane

```java
public class PassengerPlane implements Aircraft {
    public String model;
    public int passengerCapacity;
    public int crewCapacity;

    public PassengerPlane(String model) {
        this.model = model;
        switch (model) {
            case "A380":
                passengerCapacity = 500;
                crewCapacity = 42;
                break;
            case "A350":
                passengerCapacity = 320;
                crewCapacity = 40;
                break;
            case "Embraer 190":
                passengerCapacity = 25;
                crewCapacity = 5;
                break;
            case "Antonov AN2":
                passengerCapacity = 15;
                crewCapacity = 3;
                break;
            default:
                throw new IllegalArgumentException(String.format("Model type '%s' is not recognized", model));
        }
    }

    @Override
    public String getModel() {
        return model;
    }

    @Override
    public int getPassengerCapacity() {
        return passengerCapacity;
    }

    @Override
    public int getCrewCapacity() {
        return crewCapacity;
    }
}
```

#### Helicopter

```java
public class Helicopter implements Aircraft {
    private final String model;
    private final int passengerCapacity;

    public Helicopter(String model) {
        if (model.equals("H1")) {
            passengerCapacity = 4;
        } else if (model.equals("H2")) {
            passengerCapacity = 6;
        } else {
            throw new IllegalArgumentException(String.format("Model type '%s' is not recognized", model));
        }
        this.model = model;
    }

    @Override
    public String getModel() {
        return model;
    }

    @Override
    public int getPassengerCapacity() {
        return passengerCapacity;
    }

    @Override
    public int getCrewCapacity() {
        return 2; // Fixed for all helicopters
    }
}
```

#### PassengerDrone

```java
public class PassengerDrone implements Aircraft {
    private final String model;

    public PassengerDrone(String model) {
        if (model.equals("HypaHype")) {
            this.model = model;
        } else {
            throw new IllegalArgumentException(String.format("Model type '%s' is not recognized", model));
        }
    }

    @Override
    public String getModel() {
        return model;
    }

    @Override
    public int getPassengerCapacity() {
        return 4; // Fixed for all drones
    }

    @Override
    public int getCrewCapacity() {
        return 0; // No crew needed
    }
}
```

### 3. Aircraft Factory

A new `AircraftFactory` class was created to encapsulate aircraft creation logic:

```java
public class AircraftFactory {
    
    public static Aircraft createAircraft(String type, String model) {
        switch (type.toLowerCase()) {
            case "plane":
                return new PassengerPlane(model);
            case "helicopter":
                return new Helicopter(model);
            case "drone":
                return new PassengerDrone(model);
            default:
                throw new IllegalArgumentException("Unknown aircraft type: " + type);
        }
    }
    
    // Convenience methods
    public static Aircraft createPlane(String model) {
        return new PassengerPlane(model);
    }
    
    public static Aircraft createHelicopter(String model) {
        return new Helicopter(model);
    }
    
    public static Aircraft createDrone(String model) {
        return new PassengerDrone(model);
    }
}
```

### 4. Updated Client Code

#### Flight Class

```java
public class Flight {
    private int number;
    private Airport departure;
    private Airport arrival;
    protected Aircraft aircraft;

    public Flight(int number, Airport departure, Airport arrival, Aircraft aircraft) throws IllegalArgumentException {
        this.number = number;
        this.departure = departure;
        this.arrival = arrival;
        this.aircraft = aircraft;
        checkValidity();
    }

    private void checkValidity() throws IllegalArgumentException {
        if (!isAircraftValid(departure) || !isAircraftValid(arrival)) {
            throw new IllegalArgumentException("Selected aircraft is not valid for the selected route.");
        }
    }

    private boolean isAircraftValid(Airport airport) {
        return Arrays.stream(airport.getAllowedAircrafts())
                .anyMatch(x -> x.equals(aircraft.getModel()));
    }

    // Rest of the class remains the same
}
```

#### ScheduledFlight Class

```java
public class ScheduledFlight extends Flight {
    // Existing code...

    public int getCrewMemberCapacity() throws NoSuchFieldException {
        return aircraft.getCrewCapacity();
    }

    public int getCapacity() throws NoSuchFieldException {
        return aircraft.getPassengerCapacity();
    }

    // Rest of the class remains the same
}
```

## Benefits of the Refactoring

1. **Eliminated Type Checking**: No more `instanceof` checks, making the code cleaner and more maintainable.

2. **Consistent Interface**: All aircraft types now implement the same interface, providing a consistent way to interact with them.

3. **Decoupled Client Code**: `Flight` and `ScheduledFlight` classes now depend on the abstract `Aircraft` interface rather than concrete implementations.

4. **Improved Encapsulation**: Aircraft creation logic is now encapsulated in the `AircraftFactory` class.

5. **Better Extensibility**: New aircraft types can be added by implementing the `Aircraft` interface without modifying existing client code.

6. **Reduced Code Duplication**: Common logic for type checking has been eliminated.

7. **Centralized Aircraft Configuration**: Aircraft configuration is now managed within each aircraft class.

## Example Usage

Before refactoring:
```java
// Creating aircraft
PassengerPlane plane = new PassengerPlane("A380");
Helicopter helicopter = new Helicopter("H1");
PassengerDrone drone = new PassengerDrone("HypaHype");

// Using aircraft in Flight
Flight flight1 = new Flight(1, departure, arrival, plane);
Flight flight2 = new Flight(2, departure, arrival, helicopter);
Flight flight3 = new Flight(3, departure, arrival, drone);
```

After refactoring:
```java
// Using the factory to create aircraft
Aircraft plane = AircraftFactory.createPlane("A380");
Aircraft helicopter = AircraftFactory.createHelicopter("H1");
Aircraft drone = AircraftFactory.createDrone("HypaHype");

// Alternative using the general factory method
Aircraft plane2 = AircraftFactory.createAircraft("plane", "A380");

// Using aircraft in Flight - interface remains the same
Flight flight1 = new Flight(1, departure, arrival, plane);
Flight flight2 = new Flight(2, departure, arrival, helicopter);
Flight flight3 = new Flight(3, departure, arrival, drone);
```

## Trade-offs

### Advantages

1. **Better Design**: The Factory Method Pattern promotes the use of interfaces over implementations, following the Dependency Inversion Principle.

2. **Extensibility**: New aircraft types can be added without modifying existing code, adhering to the Open/Closed Principle.

3. **Maintainability**: Code is more maintainable with clear separation of concerns and consistent interfaces.

4. **Type Safety**: The use of a common interface prevents type errors and removes the need for type casting.

### Disadvantages

1. **Additional Classes**: The pattern introduces additional interfaces and classes, which might increase the complexity of the codebase for small projects.

2. **Indirection**: The factory adds a level of indirection, which might make the code slightly harder to follow for new developers.

3. **Performance**: There might be a very slight performance overhead due to the additional abstraction layer, but it's negligible in most cases.