package com.daimielcr.backend.adapter.in.web.ride_request;

import java.util.Objects;
import java.util.UUID;

public record CreateRideRequestResponse(
        UUID rideRequestId
) {

    public CreateRideRequestResponse {
        Objects.requireNonNull(
                rideRequestId,
                "El id de la solicitud es obligatorio"
        );
    }
}