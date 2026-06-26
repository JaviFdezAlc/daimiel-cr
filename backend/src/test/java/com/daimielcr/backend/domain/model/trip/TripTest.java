package com.daimielcr.backend.domain.model.trip;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import com.daimielcr.backend.domain.exceptions.InsufficientSeatsException;
import com.daimielcr.backend.domain.exceptions.InvalidTripException;
import com.daimielcr.backend.domain.exceptions.TripNotAvailableException;
import com.daimielcr.backend.domain.exceptions.UnauthorizedTripActionException;
import com.daimielcr.backend.domain.model.user.UserId;

class TripTest {

    private static final Instant NOW = Instant.parse("2026-06-26T10:00:00Z");
    private static final Instant DEPARTURE = Instant.parse("2026-06-27T07:30:00Z");

    private static final UserId DRIVER_ID =
            new UserId(UUID.fromString("11111111-1111-1111-1111-111111111111"));

    private static final UserId OTHER_USER_ID =
            new UserId(UUID.fromString("22222222-2222-2222-2222-222222222222"));

    @Test
    void shouldCreateActiveTripWithAllSeatsAvailable() {
        Trip trip = createTrip(3);

        assertEquals(TripStatus.ACTIVE, trip.status());
        assertEquals(new SeatCount(3), trip.totalSeats());
        assertEquals(new SeatCount(3), trip.availableSeats());
        assertEquals(new SeatCount(0), trip.occupiedSeats());
        assertEquals(DEPARTURE, trip.departureAt().value());
    }

    @Test
    void shouldRejectTripWithPastDeparture() {
        assertThrows(
                InvalidTripException.class,
                () -> Trip.create(
                        TripId.newId(),
                        DRIVER_ID,
                        route(),
                        new DepartureAt(NOW.minus(1, ChronoUnit.MINUTES)),
                        new SeatCount(2),
                        new ContributionAmount(new BigDecimal("3.00")),
                        "Daimiel",
                        "Ciudad Real",
                        null,
                        NOW
                )
        );
    }

    @Test
    void shouldBecomeFullWhenLastSeatIsReserved() {
        Trip trip = createTrip(2);

        trip.reserveSeats(new SeatCount(2), NOW);

        assertEquals(TripStatus.FULL, trip.status());
        assertEquals(new SeatCount(0), trip.availableSeats());
        assertEquals(new SeatCount(2), trip.occupiedSeats());
    }

    @Test
    void shouldBecomeActiveAgainWhenSeatIsReleased() {
        Trip trip = createTrip(2);
        trip.reserveSeats(new SeatCount(2), NOW);

        trip.releaseSeats(new SeatCount(1), NOW.plus(1, ChronoUnit.MINUTES));

        assertEquals(TripStatus.ACTIVE, trip.status());
        assertEquals(new SeatCount(1), trip.availableSeats());
        assertEquals(new SeatCount(1), trip.occupiedSeats());
    }

    @Test
    void shouldRejectReservationWhenRequestedSeatsExceedAvailability() {
        Trip trip = createTrip(2);

        assertThrows(
                InsufficientSeatsException.class,
                () -> trip.reserveSeats(new SeatCount(3), NOW)
        );
    }

    @Test
    void shouldRejectTripUpdateWhenNewCapacityIsLowerThanOccupiedSeats() {
        Trip trip = createTrip(3);
        trip.reserveSeats(new SeatCount(2), NOW);

        assertThrows(
                InvalidTripException.class,
                () -> trip.updateDetails(
                        DRIVER_ID,
                        route(),
                        new DepartureAt(DEPARTURE.plus(1, ChronoUnit.HOURS)),
                        new SeatCount(1),
                        new ContributionAmount(new BigDecimal("3.50")),
                        "Estación de autobuses de Daimiel",
                        "Centro de Ciudad Real",
                        "Salgo puntual",
                        NOW.plus(1, ChronoUnit.MINUTES)
                )
        );
    }

    @Test
    void shouldRejectTripCancellationByNonDriver() {
        Trip trip = createTrip(2);

        assertThrows(
                UnauthorizedTripActionException.class,
                () -> trip.cancel(OTHER_USER_ID, NOW)
        );
    }

    @Test
    void shouldRejectReservationAfterDeparture() {
        Trip trip = createTrip(2);

        assertThrows(
                TripNotAvailableException.class,
                () -> trip.reserveSeats(
                        new SeatCount(1),
                        DEPARTURE.plus(1, ChronoUnit.SECONDS)
                )
        );
    }

    @Test
    void shouldRejectFinishingTripBeforeDeparture() {
        Trip trip = createTrip(2);

        assertThrows(
                TripNotAvailableException.class,
                () -> trip.finish(DRIVER_ID, NOW)
        );
    }

    @Test
    void shouldFinishTripAfterDeparture() {
        Trip trip = createTrip(2);

        trip.finish(DRIVER_ID, DEPARTURE);

        assertEquals(TripStatus.FINISHED, trip.status());
        assertEquals(DEPARTURE, trip.updatedAt());
    }

    private Trip createTrip(int seats) {
        return Trip.create(
                TripId.newId(),
                DRIVER_ID,
                route(),
                new DepartureAt(DEPARTURE),
                new SeatCount(seats),
                new ContributionAmount(new BigDecimal("3.00")),
                "Daimiel",
                "Ciudad Real",
                "Viaje directo",
                NOW
        );
    }

    private Route route() {
        return new Route(
                TripLocation.DAIMIEL,
                TripLocation.CIUDAD_REAL
        );
    }
}