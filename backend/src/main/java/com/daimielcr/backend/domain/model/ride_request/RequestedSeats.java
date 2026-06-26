package com.daimielcr.backend.domain.model.ride_request;

import com.daimielcr.backend.domain.exceptions.InvalidRideRequestException;

public record RequestedSeats(int value) {

    public RequestedSeats {
        if (value < 1) {
            throw new InvalidRideRequestException(
                    "La solicitud debe incluir al menos una plaza"
            );
        }
    }
}
