package com.daimielcr.backend.domain.model.ride_request;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.daimielcr.backend.domain.exceptions.InvalidRideRequestException;
import com.daimielcr.backend.domain.exceptions.InvalidRideRequestStateException;
import com.daimielcr.backend.domain.exceptions.UnauthorizedRideRequestActionException;
import com.daimielcr.backend.domain.model.trip.TripId;
import com.daimielcr.backend.domain.model.user.UserId;

class RideRequestTest {

    private static final Instant NOW = Instant.parse("2026-06-26T10:00:00Z");
    private static final Instant LATER = NOW.plus(5, ChronoUnit.MINUTES);

    private static final UserId DRIVER_ID =
            new UserId(UUID.fromString("11111111-1111-1111-1111-111111111111"));

    private static final UserId PASSENGER_ID =
            new UserId(UUID.fromString("22222222-2222-2222-2222-222222222222"));

    private static final UserId OTHER_USER_ID =
            new UserId(UUID.fromString("33333333-3333-3333-3333-333333333333"));

    @Test
    void shouldCreatePendingRideRequest() {
        RideRequest request = createRideRequest(2, "  Hola, me interesan dos plazas.  ");

        assertEquals(RideRequestStatus.PENDING, request.status());
        assertTrue(request.isPending());
        assertFalse(request.isAccepted());
        assertEquals(new RequestedSeats(2), request.requestedSeats());
        assertEquals("Hola, me interesan dos plazas.", request.message());
        assertEquals(NOW, request.createdAt());
        assertEquals(NOW, request.updatedAt());
    }

    @Test
    void shouldCreateRideRequestWithoutMessage() {
        RideRequest request = createRideRequest(1, "   ");

        assertNull(request.message());
    }

    @Test
    void shouldRejectRideRequestInOwnTrip() {
        assertThrows(
                InvalidRideRequestException.class,
                () -> RideRequest.create(
                        RideRequestId.newId(),
                        TripId.newId(),
                        DRIVER_ID,
                        DRIVER_ID,
                        new RequestedSeats(1),
                        null,
                        NOW
                )
        );
    }

    @Test
    void shouldAcceptPendingRideRequest() {
        RideRequest request = createRideRequest(1, null);

        request.accept(LATER);

        assertEquals(RideRequestStatus.ACCEPTED, request.status());
        assertTrue(request.isAccepted());
        assertFalse(request.isPending());
        assertEquals(LATER, request.updatedAt());
    }

    @Test
    void shouldRejectPendingRideRequest() {
        RideRequest request = createRideRequest(1, null);

        request.reject(LATER);

        assertEquals(RideRequestStatus.REJECTED, request.status());
        assertEquals(LATER, request.updatedAt());
    }

    @Test
    void shouldRejectAcceptingAlreadyAcceptedRideRequest() {
        RideRequest request = createRideRequest(1, null);
        request.accept(NOW);

        assertThrows(
                InvalidRideRequestStateException.class,
                () -> request.accept(LATER)
        );
    }

    @Test
    void shouldRejectRejectingAlreadyAcceptedRideRequest() {
        RideRequest request = createRideRequest(1, null);
        request.accept(NOW);

        assertThrows(
                InvalidRideRequestStateException.class,
                () -> request.reject(LATER)
        );
    }

    @Test
    void shouldRejectAcceptingRejectedRideRequest() {
        RideRequest request = createRideRequest(1, null);
        request.reject(NOW);

        assertThrows(
                InvalidRideRequestStateException.class,
                () -> request.accept(LATER)
        );
    }

    @Test
    void shouldAllowPassengerToCancelPendingRideRequest() {
        RideRequest request = createRideRequest(1, null);

        request.cancel(PASSENGER_ID, LATER);

        assertEquals(RideRequestStatus.CANCELLED, request.status());
        assertEquals(LATER, request.updatedAt());
        assertFalse(request.requiresSeatReleaseWhenCancelled());
    }

    @Test
    void shouldAllowPassengerToCancelAcceptedRideRequest() {
        RideRequest request = createRideRequest(2, null);
        request.accept(NOW);

        assertTrue(request.requiresSeatReleaseWhenCancelled());

        request.cancel(PASSENGER_ID, LATER);

        assertEquals(RideRequestStatus.CANCELLED, request.status());
        assertEquals(LATER, request.updatedAt());
    }

    @Test
    void shouldRejectCancellationByUserWhoIsNotPassenger() {
        RideRequest request = createRideRequest(1, null);

        assertThrows(
                UnauthorizedRideRequestActionException.class,
                () -> request.cancel(OTHER_USER_ID, LATER)
        );
    }

    @Test
    void shouldRejectCancellationOfRejectedRideRequest() {
        RideRequest request = createRideRequest(1, null);
        request.reject(NOW);

        assertThrows(
                InvalidRideRequestStateException.class,
                () -> request.cancel(PASSENGER_ID, LATER)
        );
    }

    @Test
    void shouldRejectCancellationOfAlreadyCancelledRideRequest() {
        RideRequest request = createRideRequest(1, null);
        request.cancel(PASSENGER_ID, NOW);

        assertThrows(
                InvalidRideRequestStateException.class,
                () -> request.cancel(PASSENGER_ID, LATER)
        );
    }

    @Test
    void shouldRecognizePassengerOwnership() {
        RideRequest request = createRideRequest(1, null);

        assertTrue(request.belongsToPassenger(PASSENGER_ID));
        assertFalse(request.belongsToPassenger(OTHER_USER_ID));
    }

    private RideRequest createRideRequest(int seats, String message) {
        return RideRequest.create(
                RideRequestId.newId(),
                TripId.newId(),
                DRIVER_ID,
                PASSENGER_ID,
                new RequestedSeats(seats),
                message,
                NOW
        );
    }
}
