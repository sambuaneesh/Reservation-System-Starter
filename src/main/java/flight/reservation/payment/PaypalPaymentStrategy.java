package flight.reservation.payment;

// concrete strategy for paypal payment
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