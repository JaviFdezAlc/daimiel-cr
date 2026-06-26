package com.daimielcr.backend.adapter.in.web.trip;

import java.util.Objects;
import java.util.UUID;

public record CreateTripResponse(UUID tripId) {

    public CreateTripResponse {
        Objects.requireNonNull(tripId, "El id del viaje es obligatorio");
    }
}
