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

import com.daimielcr.backend.application.port.in.ride_request.CancelRideRequestCommand;
import com.daimielcr.backend.application.port.out.ride_request.RideRequestRepositoryPort;
import com.daimielcr.backend.application.port.out.trip.TripRepositoryPort;
import com.daimielcr.backend.domain.exceptions.InvalidRideRequestStateException;
import com.daimielcr.backend.domain.exceptions.RideRequestNotFoundException;
import com.daimielcr.backend.domain.exceptions.TripNotFoundException;
import com.daimielcr.backend.domain.exceptions.UnauthorizedRideRequestActionException;
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
import com.daimielcr.backend.domain.model.trip.TripStatus;
import com.daimielcr.backend.domain.model.user.UserId;

@ExtendWith(MockitoExtension.class)
class CancelRideRequestServiceTest {

    private static final Instant NOW =
            Instant.parse("2026-07-01T12:00:00Z");

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

    private CancelRideRequestService service;

    @BeforeEach
    void setUp() {
        service = new CancelRideRequestService(
                rideRequestRepository,
                tripRepository,
                Clock.fixed(NOW, ZoneOffset.UTC)
        );
    }

    @Test
    void shouldCancelPendingRideRequestWithoutChangingTrip() {
        RideRequest rideRequest = pendingRequest();

        when(rideRequestRepository.findById(RIDE_REQUEST_ID))
                .thenReturn(Optional.of(rideRequest));

        service.cancel(command(PASSENGER_ID));

        verify(rideRequestRepository).save(rideRequest);
        verifyNoInteractions(tripRepository);

        assertEquals(RideRequestStatus.CANCELLED, rideRequest.status());
        assertEquals(NOW, rideRequest.updatedAt());
    }

    @Test
    void shouldCancelAcceptedRideRequestAndReleaseSeats() {
        RideRequest rideRequest = acceptedRequest();
        Trip trip = fullTripWithOneAcceptedSeat();

        when(rideRequestRepository.findById(RIDE_REQUEST_ID))
                .thenReturn(Optional.of(rideRequest));

        when(tripRepository.findById(TRIP_ID))
                .thenReturn(Optional.of(trip));

        service.cancel(command(PASSENGER_ID));

        verify(tripRepository).save(trip);
        verify(rideRequestRepository).save(rideRequest);

        assertEquals(RideRequestStatus.CANCELLED, rideRequest.status());
        assertEquals(NOW, rideRequest.updatedAt());

        assertEquals(1, trip.availableSeats().value());
        assertEquals(TripStatus.ACTIVE, trip.status());
        assertEquals(NOW, trip.updatedAt());
    }

    @Test
    void shouldThrowWhenRideRequestDoesNotExist() {
        when(rideRequestRepository.findById(RIDE_REQUEST_ID))
                .thenReturn(Optional.empty());

        assertThrows(
                RideRequestNotFoundException.class,
                () -> service.cancel(command(PASSENGER_ID))
        );

        verifyNoInteractions(tripRepository);
        verify(rideRequestRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenTripDoesNotExistForAcceptedRideRequest() {
        RideRequest rideRequest = acceptedRequest();

        when(rideRequestRepository.findById(RIDE_REQUEST_ID))
                .thenReturn(Optional.of(rideRequest));

        when(tripRepository.findById(TRIP_ID))
                .thenReturn(Optional.empty());

        assertThrows(
                TripNotFoundException.class,
                () -> service.cancel(command(PASSENGER_ID))
        );

        verify(rideRequestRepository, never()).save(any());
        verify(tripRepository, never()).save(any());

        assertEquals(RideRequestStatus.ACCEPTED, rideRequest.status());
    }

    @Test
    void shouldThrowWhenRequesterIsNotPassenger() {
        RideRequest rideRequest = pendingRequest();

        when(rideRequestRepository.findById(RIDE_REQUEST_ID))
                .thenReturn(Optional.of(rideRequest));

        assertThrows(
                UnauthorizedRideRequestActionException.class,
                () -> service.cancel(command(OTHER_USER_ID))
        );

        verifyNoInteractions(tripRepository);
        verify(rideRequestRepository, never()).save(any());

        assertEquals(RideRequestStatus.PENDING, rideRequest.status());
    }

    @Test
    void shouldThrowWhenRideRequestCannotBeCancelled() {
        RideRequest rideRequest = pendingRequest();
        rideRequest.reject(NOW.minusSeconds(1));

        when(rideRequestRepository.findById(RIDE_REQUEST_ID))
                .thenReturn(Optional.of(rideRequest));

        assertThrows(
                InvalidRideRequestStateException.class,
                () -> service.cancel(command(PASSENGER_ID))
        );

        verifyNoInteractions(tripRepository);
        verify(rideRequestRepository, never()).save(any());

        assertEquals(RideRequestStatus.REJECTED, rideRequest.status());
    }

    private CancelRideRequestCommand command(UserId requesterId) {
        return new CancelRideRequestCommand(
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
                "Quiero reservar una plaza",
                NOW.minusSeconds(10)
        );
    }

    private RideRequest acceptedRequest() {
        RideRequest rideRequest = pendingRequest();
        rideRequest.accept(NOW.minusSeconds(5));
        return rideRequest;
    }

    private Trip fullTripWithOneAcceptedSeat() {
        Trip trip = Trip.create(
                TRIP_ID,
                DRIVER_ID,
                new Route(
                        TripLocation.DAIMIEL,
                        TripLocation.CIUDAD_REAL
                ),
                new DepartureAt(
                        Instant.parse("2030-07-10T07:30:00Z")
                ),
                new SeatCount(1),
                new ContributionAmount(new BigDecimal("3.00")),
                "Plaza de España, Daimiel",
                "Estación de autobuses, Ciudad Real",
                "Prueba de cancelación",
                NOW.minusSeconds(10)
        );

        trip.reserveSeats(
                new SeatCount(1),
                NOW.minusSeconds(5)
        );

        return trip;
    }
}