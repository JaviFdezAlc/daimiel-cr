package com.daimielcr.backend.application.port.in.trip;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

import com.daimielcr.backend.domain.model.trip.TripId;
import com.daimielcr.backend.domain.model.trip.TripLocation;
import com.daimielcr.backend.domain.model.user.UserId;

public record UpdateTripCommand(
        TripId tripId,
        UserId requesterId,
        TripLocation origin,
        TripLocation destination,
        Instant departureAt,
        int totalSeats,
        BigDecimal contributionAmount,
        String departurePoint,
        String arrivalPoint,
        String comment
) {

    public UpdateTripCommand {
        Objects.requireNonNull(tripId, "El id del viaje es obligatorio");
        Objects.requireNonNull(requesterId, "El usuario que modifica es obligatorio");
        Objects.requireNonNull(origin, "El origen es obligatorio");
        Objects.requireNonNull(destination, "El destino es obligatorio");
        Objects.requireNonNull(departureAt, "La fecha y hora de salida son obligatorias");
        Objects.requireNonNull(
                contributionAmount,
                "La contribución a gastos es obligatoria"
        );
    }
}