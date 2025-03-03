package flight.reservation.journey;

import flight.reservation.flight.ScheduledFlight;

import java.util.List;

public class JourneyFactory {

    public static Journey createSingleFlightJourney(ScheduledFlight flight) {
        return new SingleFlightJourney(flight);
    }

    public static Journey createMultiFlightJourney(List<ScheduledFlight> flights) {
        MultiFlightJourney multiFlightJourney = new MultiFlightJourney();

        for (ScheduledFlight flight : flights) {
            multiFlightJourney.addJourney(new SingleFlightJourney(flight));
        }

        return multiFlightJourney;
    }

    public static Journey createFromJourneys(List<Journey> journeys) {
        MultiFlightJourney multiFlightJourney = new MultiFlightJourney();

        for (Journey journey : journeys) {
            multiFlightJourney.addJourney(journey);
        }

        return multiFlightJourney;
    }
}