package flight.reservation.example;

import flight.reservation.Airport;
import flight.reservation.Customer;
import flight.reservation.flight.ScheduledFlight;
import flight.reservation.plane.Aircraft;
import flight.reservation.plane.AircraftFactory;

import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class ObserverPatternDemo {

    public static void main(String[] args) throws Exception {
        // Create airports
        Airport departureAirport = new Airport("Berlin Airport", "BER", "Berlin, Berlin");
        Airport arrivalAirport = new Airport("Frankfurt Airport", "FRA", "Frankfurt, Hesse");

        // Create aircraft
        Aircraft aircraft = AircraftFactory.createPlane("A380");

        // Create a scheduled flight
        Date departureTime = new Date();
        departureTime = new Date(departureTime.getTime() + TimeUnit.DAYS.toMillis(7)); // 7 days from now
        ScheduledFlight flight = new ScheduledFlight(101, departureAirport, arrivalAirport, aircraft, departureTime, 299.99);

        // Create customers
        Customer john = new Customer("John", "john@example.com");
        Customer alice = new Customer("Alice", "alice@example.com");

        // Book flights for customers
        john.createOrder(Arrays.asList("John", "Jane"), Arrays.asList(flight), 599.98);
        alice.createOrder(Arrays.asList("Alice"), Arrays.asList(flight), 299.99);

        // Make changes to the flight
        System.out.println("\nChanging flight price...");
        flight.setCurrentPrice(349.99);

        System.out.println("\nChanging departure time...");
        Date newDepartureTime = new Date(departureTime.getTime() + TimeUnit.HOURS.toMillis(2)); // 2 hours later
        flight.setDepartureTime(newDepartureTime);

        System.out.println("\nCancelling flight...");
        flight.cancelFlight();

        // Print all notifications received by customers
        System.out.println("\nJohn's notifications:");
        john.getNotifications().forEach(System.out::println);

        System.out.println("\nAlice's notifications:");
        alice.getNotifications().forEach(System.out::println);
    }
}