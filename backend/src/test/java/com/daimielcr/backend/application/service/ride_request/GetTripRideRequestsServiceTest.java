package com.daimielcr.backend.application.service.ride_request;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.daimielcr.backend.application.port.in.ride_request.GetTripRideRequestsQuery;
import com.daimielcr.backend.application.port.in.ride_request.RideRequestSummary;
import com.daimielcr.backend.application.port.out.ride_request.RideRequestRepositoryPort;
import com.daimielcr.backend.application.port.out.trip.TripRepositoryPort;
import com.daimielcr.backend.domain.exceptions.TripNotFoundException;
import com.daimielcr.backend.domain.exceptions.UnauthorizedTripActionException;
import com.daimielcr.backend.domain.model.ride_request.RequestedSeats;
import com.daimielcr.backend.domain.model.ride_request.RideRequest;
import com.daimielcr.backend.domain.model.ride_request.RideRequestId;
import com.daimielcr.backend.domain.model.ride_request.RideRequestStatus;
import com.daimielcr.backend.domain.model.trip.ContributionAmount;
import com.daimielcr.backend.domain.model.trip.DepartureAt;
import com.daimielcr.backend.domain.model.trip.Route;
import com.daimielcr.backend.domain.model.trip.SeatCount;
import com.daimielcr.backend.domain.model.trip.Trip;
import com.daimielcr.backend.domain.model.trip.TripId;
import com.daimielcr.backend.domain.model.trip.TripLocation;
import com.daimielcr.backend.domain.model.user.UserId;

@ExtendWith(MockitoExtension.class)
class GetTripRideRequestsServiceTest {

    private static final Instant NOW =
            Instant.parse("2026-07-01T12:00:00Z");

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

    private static final UserId FIRST_PASSENGER_ID =
            new UserId(UUID.fromString(
                    "44444444-4444-4444-4444-444444444444"
            ));

    private static final UserId SECOND_PASSENGER_ID =
            new UserId(UUID.fromString(
                    "55555555-5555-5555-5555-555555555555"
            ));

    @Mock
    private RideRequestRepositoryPort rideRequestRepository;

    @Mock
    private TripRepositoryPort tripRepository;

    private GetTripRideRequestsService service;

    @BeforeEach
    void setUp() {
        service = new GetTripRideRequestsService(
                rideRequestRepository,
                tripRepository
        );
    }

    @Test
    void shouldReturnRideRequestsWhenRequesterIsTripDriver() {
        RideRequest firstRequest = pendingRideRequest(
                "66666666-6666-6666-6666-666666666666",
                FIRST_PASSENGER_ID,
                1,
                "¿Te queda sitio?"
        );

        RideRequest secondRequest = pendingRideRequest(
                "77777777-7777-7777-7777-777777777777",
                SECOND_PASSENGER_ID,
                2,
                "Viajo con un acompañante"
        );

        when(tripRepository.findById(TRIP_ID))
                .thenReturn(Optional.of(activeTrip()));

        when(rideRequestRepository.findAllByTripId(TRIP_ID))
                .thenReturn(List.of(firstRequest, secondRequest));

        List<RideRequestSummary> result = service.getForTrip(
                query(DRIVER_ID)
        );

        assertEquals(2, result.size());

        RideRequestSummary firstSummary = result.getFirst();
        assertEquals(firstRequest.id(), firstSummary.id());
        assertEquals(FIRST_PASSENGER_ID, firstSummary.passengerId());
        assertEquals(1, firstSummary.requestedSeats());
        assertEquals("¿Te queda sitio?", firstSummary.message());
        assertEquals(RideRequestStatus.PENDING, firstSummary.status());

        RideRequestSummary secondSummary = result.get(1);
        assertEquals(secondRequest.id(), secondSummary.id());
        assertEquals(SECOND_PASSENGER_ID, secondSummary.passengerId());
        assertEquals(2, secondSummary.requestedSeats());

        verify(tripRepository).findById(TRIP_ID);
        verify(rideRequestRepository).findAllByTripId(TRIP_ID);
    }

    @Test
    void shouldReturnEmptyListWhenTripHasNoRideRequests() {
        when(tripRepository.findById(TRIP_ID))
                .thenReturn(Optional.of(activeTrip()));

        when(rideRequestRepository.findAllByTripId(TRIP_ID))
                .thenReturn(List.of());

        List<RideRequestSummary> result = service.getForTrip(
                query(DRIVER_ID)
        );

        assertTrue(result.isEmpty());

        verify(rideRequestRepository).findAllByTripId(TRIP_ID);
    }

    @Test
    void shouldThrowWhenTripDoesNotExist() {
        when(tripRepository.findById(TRIP_ID))
                .thenReturn(Optional.empty());

        assertThrows(
                TripNotFoundException.class,
                () -> service.getForTrip(query(DRIVER_ID))
        );

        verifyNoInteractions(rideRequestRepository);
    }

    @Test
    void shouldThrowWhenRequesterIsNotTripDriver() {
        when(tripRepository.findById(TRIP_ID))
                .thenReturn(Optional.of(activeTrip()));

        assertThrows(
                UnauthorizedTripActionException.class,
                () -> service.getForTrip(query(OTHER_USER_ID))
        );

        verifyNoInteractions(rideRequestRepository);
    }

    private GetTripRideRequestsQuery query(UserId requesterId) {
        return new GetTripRideRequestsQuery(TRIP_ID, requesterId);
    }

    private RideRequest pendingRideRequest(
            String id,
            UserId passengerId,
            int requestedSeats,
            String message
    ) {
        return RideRequest.create(
                new RideRequestId(UUID.fromString(id)),
                TRIP_ID,
                DRIVER_ID,
                passengerId,
                new RequestedSeats(requestedSeats),
                message,
                NOW
        );
    }

    private Trip activeTrip() {
        return Trip.create(
                TRIP_ID,
                DRIVER_ID,
                new Route(
                        TripLocation.DAIMIEL,
                        TripLocation.CIUDAD_REAL
                ),
                new DepartureAt(
                        Instant.parse("2030-07-10T07:30:00Z")
                ),
                new SeatCount(3),
                new ContributionAmount(new BigDecimal("3.00")),
                "Plaza de España, Daimiel",
                "Estación de autobuses, Ciudad Real",
                "Prueba de listado de solicitudes",
                NOW
        );
    }
}