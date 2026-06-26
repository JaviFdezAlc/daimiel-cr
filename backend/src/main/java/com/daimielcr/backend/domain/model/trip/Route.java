package com.daimielcr.backend.domain.model.trip;

import java.util.Objects;

import com.daimielcr.backend.domain.exceptions.InvalidTripException;

public record Route(
        TripLocation origin,
        TripLocation destination
) {

    public Route {
        Objects.requireNonNull(origin, "El origen es obligatorio");
        Objects.requireNonNull(destination, "El destino es obligatorio");

        if (origin == destination) {
            throw new InvalidTripException(
                    "El origen y el destino deben ser distintos"
            );
        }
    }
}
