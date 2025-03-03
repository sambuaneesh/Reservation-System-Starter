package flight.reservation.order;

import flight.reservation.Customer;
import flight.reservation.flight.ScheduledFlight;
import flight.reservation.payment.CreditCard;
import flight.reservation.payment.CreditCardPaymentStrategy;
import flight.reservation.payment.PaymentStrategy;
import flight.reservation.payment.PaypalPaymentStrategy;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class FlightOrder extends Order {
    private final List<ScheduledFlight> flights;
    static List<String> noFlyList = Arrays.asList("Peter", "Johannes");
    private PaymentStrategy paymentStrategy;

    public FlightOrder(List<ScheduledFlight> flights) {
        this.flights = flights;
    }

    public static List<String> getNoFlyList() {
        return noFlyList;
    }

    public List<ScheduledFlight> getScheduledFlights() {
        return flights;
    }

    private boolean isOrderValid(Customer customer, List<String> passengerNames, List<ScheduledFlight> flights) {
        boolean valid = true;
        valid = valid && !noFlyList.contains(customer.getName());
        valid = valid && passengerNames.stream().noneMatch(passenger -> noFlyList.contains(passenger));
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

    /**
     * Convenience method to set credit card payment strategy and process the order.
     * 
     * @param number Credit card number
     * @param expirationDate Credit card expiration date
     * @param cvv Credit card CVV
     * @return true if payment was successful, false otherwise
     * @throws IllegalStateException if payment information is invalid or payment fails
     */
    public boolean processOrderWithCreditCardDetail(String number, Date expirationDate, String cvv) throws IllegalStateException {
        setPaymentStrategy(new CreditCardPaymentStrategy(number, expirationDate, cvv));
        return processOrder();
    }

    /**
     * Convenience method to set credit card payment strategy and process the order.
     * 
     * @param creditCard The credit card to use for payment
     * @return true if payment was successful, false otherwise
     * @throws IllegalStateException if payment information is invalid or payment fails
     */
    public boolean processOrderWithCreditCard(CreditCard creditCard) throws IllegalStateException {
        setPaymentStrategy(new CreditCardPaymentStrategy(creditCard));
        return processOrder();
    }

    /**
     * Convenience method to set PayPal payment strategy and process the order.
     * 
     * @param email PayPal email
     * @param password PayPal password
     * @return true if payment was successful, false otherwise
     * @throws IllegalStateException if payment information is invalid or payment fails
     */
    public boolean processOrderWithPayPal(String email, String password) throws IllegalStateException {
        setPaymentStrategy(new PaypalPaymentStrategy(email, password));
        return processOrder();
    }
}
