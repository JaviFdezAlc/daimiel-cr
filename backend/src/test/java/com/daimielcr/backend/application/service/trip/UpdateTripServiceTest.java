package com.daimielcr.backend.application.service.trip;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.daimielcr.backend.application.port.in.trip.TripDetail;
import com.daimielcr.backend.application.port.in.trip.UpdateTripCommand;
import com.daimielcr.backend.application.port.out.trip.TripRepositoryPort;
import com.daimielcr.backend.domain.exceptions.TripNotFoundException;
import com.daimielcr.backend.domain.exceptions.UnauthorizedTripActionException;
import com.daimielcr.backend.domain.model.trip.ContributionAmount;
import com.daimielcr.backend.domain.model.trip.DepartureAt;
import com.daimielcr.backend.domain.model.trip.Route;
import com.daimielcr.backend.domain.model.trip.SeatCount;
import com.daimielcr.backend.domain.model.trip.Trip;
import com.daimielcr.backend.domain.model.trip.TripId;
import com.daimielcr.backend.domain.model.trip.TripLocation;
import com.daimielcr.backend.domain.model.user.UserId;

@ExtendWith(MockitoExtension.class)
class UpdateTripServiceTest {

    private static final Instant NOW =
            Instant.parse("2026-06-29T16:00:00Z");

    private static final TripId TRIP_ID =
            new TripId(UUID.fromString(
                    "11111111-1111-1111-1111-111111111111"
            ));

    private static final UserId DRIVER_ID =
            new UserId(UUID.fromString(
                    "22222222-2222-2222-2222-222222222222"
            ));

    private static final UserId OTHER_USER_ID =
            new UserId(UUID.fromString(
                    "33333333-3333-3333-3333-333333333333"
            ));

    @Mock
    private TripRepositoryPort tripRepository;

    private UpdateTripService service;

    @BeforeEach
    void setUp() {
        service = new UpdateTripService(
                tripRepository,
                Clock.fixed(NOW, ZoneOffset.UTC)
        );
    }

    @Test
    void shouldUpdateTripWhenRequesterIsDriver() {
        Trip trip = existingTrip();

        when(tripRepository.findById(TRIP_ID))
                .thenReturn(Optional.of(trip));

        UpdateTripCommand command = new UpdateTripCommand(
                TRIP_ID,
                DRIVER_ID,
                TripLocation.CIUDAD_REAL,
                TripLocation.DAIMIEL,
                Instant.parse("2030-07-02T15:30:00Z"),
                4,
                new BigDecimal("3.50"),
                "Estación de autobuses, Ciudad Real",
                "Plaza de España, Daimiel",
                "Vuelvo después de clase"
        );

        TripDetail result = service.update(command);

        verify(tripRepository).findById(TRIP_ID);
        verify(tripRepository).save(trip);

        assertEquals(TRIP_ID, result.id());
        assertEquals(TripLocation.CIUDAD_REAL, result.origin());
        assertEquals(TripLocation.DAIMIEL, result.destination());
        assertEquals(
                Instant.parse("2030-07-02T15:30:00Z"),
                result.departureAt()
        );
        assertEquals(4, result.totalSeats());
        assertEquals(4, result.availableSeats());
        assertEquals(new BigDecimal("3.50"), result.contributionAmount());
        assertEquals(
                "Estación de autobuses, Ciudad Real",
                result.departurePoint()
        );
        assertEquals(
                "Plaza de España, Daimiel",
                result.arrivalPoint()
        );
        assertEquals("Vuelvo después de clase", result.comment());
    }

    @Test
    void shouldThrowWhenTripDoesNotExist() {
        when(tripRepository.findById(TRIP_ID))
                .thenReturn(Optional.empty());

        UpdateTripCommand command = updateCommand(DRIVER_ID);

        assertThrows(
                TripNotFoundException.class,
                () -> service.update(command)
        );

        verify(tripRepository).findById(TRIP_ID);
        verify(tripRepository, never()).save(any());
    }

    @Test
    void shouldNotUpdateTripWhenRequesterIsNotDriver() {
        Trip trip = existingTrip();

        when(tripRepository.findById(TRIP_ID))
                .thenReturn(Optional.of(trip));

        UpdateTripCommand command = updateCommand(OTHER_USER_ID);

        assertThrows(
                UnauthorizedTripActionException.class,
                () -> service.update(command)
        );

        verify(tripRepository).findById(TRIP_ID);
        verify(tripRepository, never()).save(any());
    }

    private UpdateTripCommand updateCommand(UserId requesterId) {
        return new UpdateTripCommand(
                TRIP_ID,
                requesterId,
                TripLocation.CIUDAD_REAL,
                TripLocation.DAIMIEL,
                Instant.parse("2030-07-02T15:30:00Z"),
                4,
                new BigDecimal("3.50"),
                "Estación de autobuses, Ciudad Real",
                "Plaza de España, Daimiel",
                "Vuelvo después de clase"
        );
    }

    private Trip existingTrip() {
        return Trip.create(
                TRIP_ID,
                DRIVER_ID,
                new Route(
                        TripLocation.DAIMIEL,
                        TripLocation.CIUDAD_REAL
                ),
                new DepartureAt(
                        Instant.parse("2030-07-01T08:00:00Z")
                ),
                new SeatCount(3),
                new ContributionAmount(new BigDecimal("3.00")),
                "Plaza de España, Daimiel",
                "Estación de autobuses, Ciudad Real",
                "Salgo puntual",
                NOW
        );
    }
}