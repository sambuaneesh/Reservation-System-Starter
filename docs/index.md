# Design Patterns Documentation

Welcome to the Design Patterns Documentation site. This site contains information about common design patterns implemented in our flight reservation system.

## Design Patterns

This documentation covers five important design patterns:

1. [Strategy Pattern](design-smells/design-smell-1.md)
2. [Factory Method Pattern](design-smells/design-smell-2.md)
3. [Template Method Pattern](design-smells/design-smell-3.md)
4. [Observer Pattern](design-smells/design-smell-4.md)
5. [Composite Pattern](design-smells/design-smell-5.md)

While we focused on different design patterns, much like the real world we believed some of the code had more than one design patterns which could be addressed.
Here are the files where we implemented multiple design patterns simultaneously:

## 1. ScheduledFlight.java
- **Observer Pattern**: Implements `FlightSubject` interface for notification
- **Factory Method Pattern**: Uses the `Aircraft` interface from Factory pattern

## 2. FlightOrder.java
- **Template Method Pattern**: Implements abstract methods from Order superclass
- **Strategy Pattern**: Uses `PaymentStrategy` for flexible payment processing

## 3. Customer.java
- **Observer Pattern**: Implements `FlightObserver` interface to receive notifications
- **Factory Method Pattern**: Uses aircraft system indirectly through flights

## 4. MultiFlightJourney.java (Our new package)
- **Composite Pattern**: Core component that contains other Journey objects
- **Observer Pattern**: Indirectly participates by passing passenger changes to contained journeys

# Resources
- [Refactoring Guru](https://refactoring.guru/)