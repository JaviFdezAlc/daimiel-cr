package com.daimielcr.backend.adapter.in.web.ride_request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateRideRequestRequest(
        @NotNull
        @Positive
        Integer requestedSeats,

        @Size(max = 300)
        String message
) {
}