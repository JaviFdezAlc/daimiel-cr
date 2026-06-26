package com.daimielcr.backend.application.port.in.trip;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

import com.daimielcr.backend.domain.model.trip.TripLocation;
import com.daimielcr.backend.domain.model.user.UserId;

public record CreateTripCommand(
        UserId driverId,
        TripLocation origin,
        TripLocation destination,
        Instant departureAt,
        int totalSeats,
        BigDecimal contributionAmount,
        String departurePoint,
        String arrivalPoint,
        String comment
) {

    public CreateTripCommand {
        Objects.requireNonNull(driverId, "El conductor es obligatorio");
        Objects.requireNonNull(origin, "El origen es obligatorio");
        Objects.requireNonNull(destination, "El destino es obligatorio");
        Objects.requireNonNull(departureAt, "La fecha y hora de salida son obligatorias");
        Objects.requireNonNull(
                contributionAmount,
                "La contribución a gastos es obligatoria"
        );
    }
}
