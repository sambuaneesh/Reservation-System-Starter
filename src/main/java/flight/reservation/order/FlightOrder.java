package flight.reservation.order;

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

    public void setPaymentStrategy(PaymentStrategy paymentStrategy) {
        this.paymentStrategy = paymentStrategy;
    }

    // Convenience methods remain the same
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