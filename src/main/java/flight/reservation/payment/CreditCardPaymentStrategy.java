package flight.reservation.payment;

import java.util.Date;

// concrete strategy for credit card payment
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