package com.daimielcr.backend.adapter.in.web.trip;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import com.daimielcr.backend.domain.model.trip.TripLocation;

public record TripSummaryResponse(
        UUID id,
        TripLocation origin,
        TripLocation destination,
        Instant departureAt,
        String departurePoint,
        String arrivalPoint,
        int availableSeats,
        BigDecimal contributionAmount
) {

    public TripSummaryResponse {
        Objects.requireNonNull(id, "El id del viaje es obligatorio");
        Objects.requireNonNull(origin, "El origen es obligatorio");
        Objects.requireNonNull(destination, "El destino es obligatorio");
        Objects.requireNonNull(departureAt, "La salida es obligatoria");
        Objects.requireNonNull(
                contributionAmount,
                "La contribución es obligatoria"
        );
    }
}