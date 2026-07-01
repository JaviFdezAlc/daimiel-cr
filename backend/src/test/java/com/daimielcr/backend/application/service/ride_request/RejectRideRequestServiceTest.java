package com.daimielcr.backend.application.service.ride_request;

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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.daimielcr.backend.application.port.in.ride_request.RejectRideRequestCommand;
import com.daimielcr.backend.application.port.out.ride_request.RideRequestRepositoryPort;
import com.daimielcr.backend.application.port.out.trip.TripRepositoryPort;
import com.daimielcr.backend.domain.exceptions.InvalidRideRequestStateException;
import com.daimielcr.backend.domain.exceptions.RideRequestNotFoundException;
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
class RejectRideRequestServiceTest {

    private static final Instant NOW =
            Instant.parse("2026-06-29T17:00:00Z");

    private static final RideRequestId RIDE_REQUEST_ID =
            new RideRequestId(UUID.fromString(
                    "11111111-1111-1111-1111-111111111111"
            ));

    private static final TripId TRIP_ID =
            new TripId(UUID.fromString(
                    "22222222-2222-2222-2222-222222222222"
            ));

    private static final UserId DRIVER_ID =
            new UserId(UUID.fromString(
                    "33333333-3333-3333-3333-333333333333"
            ));

    private static final UserId PASSENGER_ID =
            new UserId(UUID.fromString(
                    "44444444-4444-4444-4444-444444444444"
            ));

    private static final UserId OTHER_USER_ID =
            new UserId(UUID.fromString(
                    "55555555-5555-5555-5555-555555555555"
            ));

    @Mock
    private RideRequestRepositoryPort rideRequestRepository;

    @Mock
    private TripRepositoryPort tripRepository;

    private RejectRideRequestService service;

    @BeforeEach
    void setUp() {
        service = new RejectRideRequestService(
                rideRequestRepository,
                tripRepository,
                Clock.fixed(NOW, ZoneOffset.UTC)
        );
    }

    @Test
    void shouldRejectPendingRideRequestWhenRequesterIsDriver() {
        RideRequest rideRequest = pendingRequest();
        Trip trip = activeTrip();

        when(rideRequestRepository.findById(RIDE_REQUEST_ID))
                .thenReturn(Optional.of(rideRequest));

        when(tripRepository.findById(TRIP_ID))
                .thenReturn(Optional.of(trip));

        service.reject(command(DRIVER_ID));

        verify(rideRequestRepository).save(rideRequest);
        verify(tripRepository, never()).save(any());

        assertEquals(RideRequestStatus.REJECTED, rideRequest.status());
        assertEquals(NOW, rideRequest.updatedAt());

        // Rechazar no modifica las plazas disponibles.
        assertEquals(3, trip.availableSeats().value());
    }

    @Test
    void shouldThrowWhenRideRequestDoesNotExist() {
        when(rideRequestRepository.findById(RIDE_REQUEST_ID))
                .thenReturn(Optional.empty());

        assertThrows(
                RideRequestNotFoundException.class,
                () -> service.reject(command(DRIVER_ID))
        );

        verifyNoInteractions(tripRepository);
        verify(rideRequestRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenTripDoesNotExist() {
        RideRequest rideRequest = pendingRequest();

        when(rideRequestRepository.findById(RIDE_REQUEST_ID))
                .thenReturn(Optional.of(rideRequest));

        when(tripRepository.findById(TRIP_ID))
                .thenReturn(Optional.empty());

        assertThrows(
                TripNotFoundException.class,
                () -> service.reject(command(DRIVER_ID))
        );

        verify(tripRepository).findById(TRIP_ID);
        verify(tripRepository, never()).save(any());
        verify(rideRequestRepository, never()).save(any());

        assertEquals(RideRequestStatus.PENDING, rideRequest.status());
    }

    @Test
    void shouldThrowWhenRequesterIsNotTripDriver() {
        RideRequest rideRequest = pendingRequest();
        Trip trip = activeTrip();

        when(rideRequestRepository.findById(RIDE_REQUEST_ID))
                .thenReturn(Optional.of(rideRequest));

        when(tripRepository.findById(TRIP_ID))
                .thenReturn(Optional.of(trip));

        assertThrows(
                UnauthorizedTripActionException.class,
                () -> service.reject(command(OTHER_USER_ID))
        );

        verify(tripRepository, never()).save(any());
        verify(rideRequestRepository, never()).save(any());

        assertEquals(RideRequestStatus.PENDING, rideRequest.status());
        assertEquals(3, trip.availableSeats().value());
    }

    @Test
    void shouldThrowWhenRideRequestIsNotPending() {
        RideRequest rideRequest = pendingRequest();
        rideRequest.accept(NOW.minusSeconds(1));

        Trip trip = activeTrip();

        when(rideRequestRepository.findById(RIDE_REQUEST_ID))
                .thenReturn(Optional.of(rideRequest));

        when(tripRepository.findById(TRIP_ID))
                .thenReturn(Optional.of(trip));

        assertThrows(
                InvalidRideRequestStateException.class,
                () -> service.reject(command(DRIVER_ID))
        );

        verify(tripRepository, never()).save(any());
        verify(rideRequestRepository, never()).save(any());

        assertEquals(RideRequestStatus.ACCEPTED, rideRequest.status());
        assertEquals(3, trip.availableSeats().value());
    }

    private RejectRideRequestCommand command(UserId requesterId) {
        return new RejectRideRequestCommand(
                RIDE_REQUEST_ID,
                requesterId
        );
    }

    private RideRequest pendingRequest() {
        return RideRequest.create(
                RIDE_REQUEST_ID,
                TRIP_ID,
                DRIVER_ID,
                PASSENGER_ID,
                new RequestedSeats(1),
                "¿Te queda sitio?",
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
                "Prueba de rechazo",
                NOW
        );
    }
}