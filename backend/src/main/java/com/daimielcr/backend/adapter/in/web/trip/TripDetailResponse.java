package com.daimielcr.backend.adapter.in.web.trip;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import com.daimielcr.backend.domain.model.trip.TripLocation;
import com.daimielcr.backend.domain.model.trip.TripStatus;

public record TripDetailResponse(
        UUID id,
        UUID driverId,
        TripLocation origin,
        TripLocation destination,
        Instant departureAt,
        int totalSeats,
        int availableSeats,
        BigDecimal contributionAmount,
        String departurePoint,
        String arrivalPoint,
        String comment,
        TripStatus status,
        Instant createdAt,
        Instant updatedAt
) {

    public TripDetailResponse {
        Objects.requireNonNull(id, "El id del viaje es obligatorio");
        Objects.requireNonNull(driverId, "El id del conductor es obligatorio");
        Objects.requireNonNull(origin, "El origen es obligatorio");
        Objects.requireNonNull(destination, "El destino es obligatorio");
        Objects.requireNonNull(departureAt, "La salida es obligatoria");
        Objects.requireNonNull(contributionAmount, "La contribución es obligatoria");
        Objects.requireNonNull(status, "El estado es obligatorio");
        Objects.requireNonNull(createdAt, "La fecha de creación es obligatoria");
        Objects.requireNonNull(updatedAt, "La fecha de actualización es obligatoria");
    }
}