# Template Method Pattern Refactoring

This document details the implementation of the Template Method Pattern in the flight reservation system to standardize and improve the order processing workflow.

## Problem Identification

### Original Implementation

In the original codebase, the order processing logic was scattered across the `FlightOrder` class with inconsistent validation and processing steps:

```java
// In FlightOrder.java
public boolean processOrder() throws IllegalStateException {
    if (isClosed()) {
        // Payment is already proceeded
        return true;
    }
    
    if (paymentStrategy == null) {
        throw new IllegalStateException("Payment strategy is not set.");
    }
    
    if (!paymentStrategy.isValid()) {
        throw new IllegalStateException("Payment information is not valid.");
    }
    
    boolean isPaid = paymentStrategy.pay(this.getPrice());
    if (isPaid) {
        this.setClosed();
    }
    return isPaid;
}

// Various convenience methods handled their own validation
public boolean processOrderWithCreditCard(CreditCard creditCard) throws IllegalStateException {
    setPaymentStrategy(new CreditCardPaymentStrategy(creditCard));
    return processOrder();
}
```

### Issues with the Original Implementation

1. **Lack of Standardization**: The order processing sequence wasn't standardized, making it difficult to ensure all orders follow the same workflow.

2. **Code Duplication**: Similar validation and processing logic was duplicated across different methods.

3. **Poor Extensibility**: Adding new order types would require replicating the entire order processing logic.

4. **Inconsistent Error Handling**: Different methods had different approaches to error handling.

5. **Tight Coupling**: The order processing steps were tightly coupled, making it difficult to modify individual steps without affecting others.

## Solution: Template Method Pattern Implementation

The Template Method Pattern was implemented to address these issues by:

1. Defining a skeleton algorithm in a template method in the base class
2. Deferring some steps to subclasses
3. Ensuring common steps are implemented once in the parent class

### 1. Modified Base Order Class

The `Order` abstract class was updated to include the template method for order processing:

```java
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

    // Getters and setters remain the same
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
```

### 2. Updated FlightOrder Implementation

The `FlightOrder` class was updated to implement the abstract methods defined in the base class:

```java
public class FlightOrder extends Order {
    private final List<ScheduledFlight> flights;
    static List<String> noFlyList = Arrays.asList("Peter", "Johannes");
    private PaymentStrategy paymentStrategy;

    public FlightOrder(List<ScheduledFlight> flights) {
        this.flights = flights;
    }

    @Override
    protected boolean validateOrder() {
        if (paymentStrategy == null) {
            return false;
        }
        
        if (getCustomer() == null) {
            return false;
        }
        
        if (noFlyList.contains(getCustomer().getName())) {
            return false;
        }
        
        if (getPassengers() == null || getPassengers().isEmpty()) {
            return false;
        }
        
        for (var passenger : getPassengers()) {
            if (noFlyList.contains(passenger.getName())) {
                return false;
            }
        }
        
        return true;
    }

    @Override
    protected boolean processPayment() {
        if (!paymentStrategy.isValid()) {
            return false;
        }
        
        return paymentStrategy.pay(this.getPrice());
    }

    // The rest of the class remains the same
    public static List<String> getNoFlyList() {
        return noFlyList;
    }

    public List<ScheduledFlight> getScheduledFlights() {
        return flights;
    }

    public void setPaymentStrategy(PaymentStrategy paymentStrategy) {
        this.paymentStrategy = paymentStrategy;
    }

    // Convenience methods
    public boolean processOrderWithCreditCardDetail(String number, Date expirationDate, String cvv) throws IllegalStateException {
        setPaymentStrategy(new CreditCardPaymentStrategy(number, expirationDate, cvv));
        return processOrder();
    }

    public boolean processOrderWithCreditCard(CreditCard creditCard) throws IllegalStateException {
        setPaymentStrategy(new CreditCardPaymentStrategy(creditCard));
        return processOrder();
    }

    public boolean processOrderWithPayPal(String email, String password) throws IllegalStateException {
        setPaymentStrategy(new PaypalPaymentStrategy(email, password));
        return processOrder();
    }
}
```

## Benefits of the Refactoring

1. **Standardized Process**: All orders now follow the same processing sequence, ensuring consistency.

2. **Code Reuse**: Common steps are implemented once in the parent class, reducing duplication.

3. **Improved Extensibility**: New order types can be added by extending the base `Order` class and implementing only the required abstract methods.

4. **Clearer Responsibilities**: Each step in the order processing sequence has a clear responsibility.

5. **Consistent Error Handling**: Error handling is standardized across all order types.

6. **Better Control Over Process**: The template method controls the overall flow, ensuring steps are executed in the correct order.

## Example Usage

The usage of the orders remains the same from the client's perspective, but internally the process is now more structured:

```java
// Create a flight order
FlightOrder order = customer.createOrder(passengerNames, flights, totalPrice);

// Set payment strategy and process
order.setPaymentStrategy(new CreditCardPaymentStrategy(creditCard));
boolean success = order.processOrder();

// Or use convenience methods
boolean success = order.processOrderWithCreditCard(creditCard);
```

## Trade-offs

### Advantages

1. **Better Design**: The Template Method Pattern clearly separates invariant parts of the algorithm from variant parts.

2. **Reusability**: Common steps are implemented once, reducing duplication.

3. **Extensibility**: The pattern makes it easier to add new types of orders without changing the processing sequence.

4. **Control**: The template method controls the overall sequence, preventing subclasses from changing essential steps.

### Disadvantages

1. **Limited Flexibility**: Subclasses can only override the steps provided by the template method, which might be restrictive in some cases.

2. **Complexity**: The pattern adds a level of abstraction, which might make the code slightly harder to follow for new developers.

3. **Inheritance Limitations**: The pattern relies on inheritance rather than composition, which can sometimes be a limitation.