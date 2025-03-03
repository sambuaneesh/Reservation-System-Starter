package flight.reservation.payment;

public interface PaymentStrategy {
    boolean pay(double amount) throws IllegalStateException;
    
    boolean isValid();
} 