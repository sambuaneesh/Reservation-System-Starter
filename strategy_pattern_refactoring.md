# Strategy Pattern Refactoring

This md details the implementation of the Strategy Pattern in this repository to improve the payment processing mechanism.

## Problem Identification

### Original Implementation

In the original codebase, the payment processing logic was tightly coupled with the `FlightOrder` class. The class had multiple methods for different payment types:

```java
public boolean processOrderWithCreditCard(CreditCard creditCard) throws IllegalStateException {
    if (isClosed()) {
        // Payment is already proceeded
        return true;
    }
    // validate payment information
    if (!cardIsPresentAndValid(creditCard)) {
        throw new IllegalStateException("Payment information is not set or not valid.");
    }
    boolean isPaid = payWithCreditCard(creditCard, this.getPrice());
    if (isPaid) {
        this.setClosed();
    }
    return isPaid;
}

public boolean processOrderWithPayPal(String email, String password) throws IllegalStateException {
    if (isClosed()) {
        // Payment is already proceeded
        return true;
    }
    // validate payment information
    if (email == null || password == null || !email.equals(Paypal.DATA_BASE.get(password))) {
        throw new IllegalStateException("Payment information is not set or not valid.");
    }
    boolean isPaid = payWithPayPal(email, password, this.getPrice());
    if (isPaid) {
        this.setClosed();
    }
    return isPaid;
}

public boolean payWithCreditCard(CreditCard card, double amount) throws IllegalStateException {
    if (cardIsPresentAndValid(card)) {
        System.out.println("Paying " + getPrice() + " using Credit Card.");
        double remainingAmount = card.getAmount() - getPrice();
        if (remainingAmount < 0) {
            System.out.printf("Card limit reached - Balance: %f%n", remainingAmount);
            throw new IllegalStateException("Card limit reached");
        }
        card.setAmount(remainingAmount);
        return true;
    } else {
        return false;
    }
}

public boolean payWithPayPal(String email, String password, double amount) throws IllegalStateException {
    if (email.equals(Paypal.DATA_BASE.get(password))) {
        System.out.println("Paying " + getPrice() + " using PayPal.");
        return true;
    } else {
        return false;
    }
}
```

### Issues with the Original Implementation

1. **Tight Coupling**: The payment processing logic was tightly coupled with the `FlightOrder` class, making it difficult to add new payment methods without modifying the class.

2. **Code Duplication**: Similar validation and processing logic was duplicated across different payment methods.

3. **Single Responsibility Principle Violation**: The `FlightOrder` class was responsible for both order management and payment processing, violating the Single Responsibility Principle.

4. **Open/Closed Principle Violation**: The class was not open for extension but closed for modification. Adding a new payment method required modifying the existing class.

5. **Maintainability Issues**: Changes to payment processing logic required modifying the `FlightOrder` class, increasing the risk of introducing bugs.

## Solution: Strategy Pattern Implementation

The Strategy Pattern was implemented to address these issues by:

1. Creating a common interface for all payment strategies
2. Implementing concrete strategies for each payment method
3. Modifying the `FlightOrder` class to use these strategies

### 1. Payment Strategy Interface

A new `PaymentStrategy` interface was created to define the contract for all payment methods:

```java
public interface PaymentStrategy {
    /**
     * Process a payment with the given amount.
     * 
     * @param amount The amount to be paid
     * @return true if payment was successful, false otherwise
     * @throws IllegalStateException if payment information is invalid or payment fails
     */
    boolean pay(double amount) throws IllegalStateException;
    
    /**
     * Validates if the payment information is valid.
     * 
     * @return true if the payment information is valid, false otherwise
     */
    boolean isValid();
}
```

### 2. Concrete Strategy Implementations

#### Credit Card Payment Strategy

```java
public class CreditCardPaymentStrategy implements PaymentStrategy {
    private final CreditCard creditCard;

    public CreditCardPaymentStrategy(String number, Date expirationDate, String cvv) {
        this.creditCard = new CreditCard(number, expirationDate, cvv);
    }

    public CreditCardPaymentStrategy(CreditCard creditCard) {
        this.creditCard = creditCard;
    }

    @Override
    public boolean pay(double amount) throws IllegalStateException {
        if (!isValid()) {
            throw new IllegalStateException("Credit card information is not valid.");
        }
        
        System.out.println("Paying " + amount + " using Credit Card.");
        double remainingAmount = creditCard.getAmount() - amount;
        if (remainingAmount < 0) {
            System.out.printf("Card limit reached - Balance: %f%n", remainingAmount);
            throw new IllegalStateException("Card limit reached");
        }
        creditCard.setAmount(remainingAmount);
        return true;
    }

    @Override
    public boolean isValid() {
        return creditCard != null && creditCard.isValid();
    }
}
```

#### PayPal Payment Strategy

```java
public class PaypalPaymentStrategy implements PaymentStrategy {
    private final String email;
    private final String password;

    public PaypalPaymentStrategy(String email, String password) {
        this.email = email;
        this.password = password;
    }

    @Override
    public boolean pay(double amount) throws IllegalStateException {
        if (!isValid()) {
            throw new IllegalStateException("PayPal credentials are not valid.");
        }
        
        System.out.println("Paying " + amount + " using PayPal.");
        return true;
    }

    @Override
    public boolean isValid() {
        return email != null && password != null && 
               email.equals(Paypal.DATA_BASE.get(password));
    }
}
```

### 3. Modified FlightOrder Class

The `FlightOrder` class was modified to use the Strategy Pattern:

```java
public class FlightOrder extends Order {
    private final List<ScheduledFlight> flights;
    static List<String> noFlyList = Arrays.asList("Peter", "Johannes");
    private PaymentStrategy paymentStrategy;

    // ... existing code ...

    /**
     * Sets the payment strategy to use for processing the order.
     * 
     * @param paymentStrategy The payment strategy to use
     */
    public void setPaymentStrategy(PaymentStrategy paymentStrategy) {
        this.paymentStrategy = paymentStrategy;
    }

    /**
     * Process the order with the current payment strategy.
     * 
     * @return true if payment was successful, false otherwise
     * @throws IllegalStateException if payment information is invalid or payment fails
     */
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

    // Convenience methods for backward compatibility
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

1. **Decoupled Payment Logic**: Payment processing logic is now decoupled from the `FlightOrder` class, making it easier to maintain and extend.

2. **Improved Code Organization**: Each payment method has its own class, following the Single Responsibility Principle.

3. **Extensibility**: New payment methods can be added by implementing the `PaymentStrategy` interface without modifying existing code, adhering to the Open/Closed Principle.

4. **Reduced Duplication**: Common payment processing logic is now centralized in the `processOrder()` method.

5. **Runtime Flexibility**: Payment methods can be changed at runtime by setting a different payment strategy.

6. **Testability**: Payment strategies can be tested in isolation, making unit testing easier.

## Example Usage

Before refactoring:
```java
FlightOrder order = customer.createOrder(passengerNames, flights, totalPrice);
order.processOrderWithCreditCard(creditCard);
// or
order.processOrderWithPayPal(email, password);
```

After refactoring:
```java
FlightOrder order = customer.createOrder(passengerNames, flights, totalPrice);
// Option 1: Using convenience methods (backward compatible)
order.processOrderWithCreditCard(creditCard);
// or
order.processOrderWithPayPal(email, password);

// Option 2: Using the strategy pattern directly
order.setPaymentStrategy(new CreditCardPaymentStrategy(creditCard));
order.processOrder();
// or
order.setPaymentStrategy(new PaypalPaymentStrategy(email, password));
order.processOrder();
```

## Trade-offs

### Advantages (Lets go SOLID!!)

1. **Flexibility**: The Strategy Pattern provides runtime flexibility to switch between different payment methods.

2. **Maintainability**: Code is more maintainable with clear separation of concerns.

3. **Extensibility**: New payment methods can be added without modifying existing code.

### Disadvantages

1. **Increased Number of Classes**: The refactoring introduces additional classes, which might increase the complexity of the codebase for small projects.

2. **Potential Overhead**: For very simple use cases, the pattern might introduce unnecessary abstraction. (but had to do cause of srp and class activity constraints lol)
