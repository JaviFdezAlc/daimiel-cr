package com.daimielcr.backend.application.service.trip;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.daimielcr.backend.application.port.in.trip.TripDetail;
import com.daimielcr.backend.application.port.out.trip.TripRepositoryPort;
import com.daimielcr.backend.domain.exceptions.TripNotFoundException;
import com.daimielcr.backend.domain.model.trip.ContributionAmount;
import com.daimielcr.backend.domain.model.trip.DepartureAt;
import com.daimielcr.backend.domain.model.trip.Route;
import com.daimielcr.backend.domain.model.trip.SeatCount;
import com.daimielcr.backend.domain.model.trip.Trip;
import com.daimielcr.backend.domain.model.trip.TripId;
import com.daimielcr.backend.domain.model.trip.TripLocation;
import com.daimielcr.backend.domain.model.trip.TripStatus;
import com.daimielcr.backend.domain.model.user.UserId;

@ExtendWith(MockitoExtension.class)
class GetTripDetailServiceTest {

    private static final Instant NOW =
            Instant.parse("2026-06-26T10:00:00Z");

    private static final Instant DEPARTURE_AT =
            Instant.parse("2026-06-27T07:30:00Z");

    private static final TripId TRIP_ID =
            new TripId(UUID.fromString("11111111-1111-1111-1111-111111111111"));

    private static final UserId DRIVER_ID =
            new UserId(UUID.fromString("22222222-2222-2222-2222-222222222222"));

    @Mock
    private TripRepositoryPort tripRepository;

    private GetTripDetailService service;

    @BeforeEach
    void setUp() {
        service = new GetTripDetailService(tripRepository);
    }

    @Test
    void shouldReturnTripDetailWhenTripExists() {
        Trip trip = existingTrip();

        when(tripRepository.findById(TRIP_ID))
                .thenReturn(Optional.of(trip));

        TripDetail detail = service.getById(TRIP_ID);

        assertEquals(TRIP_ID, detail.id());
        assertEquals(DRIVER_ID, detail.driverId());
        assertEquals(TripLocation.DAIMIEL, detail.origin());
        assertEquals(TripLocation.CIUDAD_REAL, detail.destination());
        assertEquals(DEPARTURE_AT, detail.departureAt());
        assertEquals(3, detail.totalSeats());
        assertEquals(3, detail.availableSeats());
        assertEquals(
                0,
                new BigDecimal("3.00").compareTo(detail.contributionAmount())
        );
        assertEquals("Plaza de España, Daimiel", detail.departurePoint());
        assertEquals("Estación de autobuses, Ciudad Real", detail.arrivalPoint());
        assertEquals("Salgo puntual", detail.comment());
        assertEquals(TripStatus.ACTIVE, detail.status());
        assertEquals(NOW, detail.createdAt());
        assertEquals(NOW, detail.updatedAt());

        verify(tripRepository).findById(TRIP_ID);
    }

    @Test
    void shouldThrowWhenTripDoesNotExist() {
        when(tripRepository.findById(TRIP_ID))
                .thenReturn(Optional.empty());

        assertThrows(
                TripNotFoundException.class,
                () -> service.getById(TRIP_ID)
        );

        verify(tripRepository).findById(TRIP_ID);
    }

    private Trip existingTrip() {
        return Trip.create(
                TRIP_ID,
                DRIVER_ID,
                new Route(
                        TripLocation.DAIMIEL,
                        TripLocation.CIUDAD_REAL
                ),
                new DepartureAt(DEPARTURE_AT),
                new SeatCount(3),
                new ContributionAmount(new BigDecimal("3.00")),
                "Plaza de España, Daimiel",
                "Estación de autobuses, Ciudad Real",
                "Salgo puntual",
                NOW
        );
    }
}