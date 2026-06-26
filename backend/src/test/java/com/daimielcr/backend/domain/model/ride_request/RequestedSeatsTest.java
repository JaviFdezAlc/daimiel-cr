package com.daimielcr.backend.domain.model.ride_request;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import com.daimielcr.backend.domain.exceptions.InvalidRideRequestException;

class RequestedSeatsTest {

    @Test
    void shouldCreateRequestedSeatsWhenValueIsPositive() {
        RequestedSeats requestedSeats = new RequestedSeats(2);

        assertEquals(2, requestedSeats.value());
    }

    @Test
    void shouldRejectZeroRequestedSeats() {
        assertThrows(
                InvalidRideRequestException.class,
                () -> new RequestedSeats(0)
        );
    }

    @Test
    void shouldRejectNegativeRequestedSeats() {
        assertThrows(
                InvalidRideRequestException.class,
                () -> new RequestedSeats(-1)
        );
    }
}
