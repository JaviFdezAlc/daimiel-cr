package com.daimielcr.backend.application.port.in.trip;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

import com.daimielcr.backend.domain.model.trip.Trip;
import com.daimielcr.backend.domain.model.trip.TripId;
import com.daimielcr.backend.domain.model.trip.TripLocation;

public record TripSummary(
        TripId id,
        TripLocation origin,
        TripLocation destination,
        Instant departureAt,
        String departurePoint,
        String arrivalPoint,
        int availableSeats,
        BigDecimal contributionAmount
) {

    public TripSummary {
        Objects.requireNonNull(id, "El id del viaje es obligatorio");
        Objects.requireNonNull(origin, "El origen es obligatorio");
        Objects.requireNonNull(destination, "El destino es obligatorio");
        Objects.requireNonNull(departureAt, "La salida es obligatoria");
        Objects.requireNonNull(
                contributionAmount,
                "La contribución es obligatoria"
        );

        if (availableSeats < 1) {
            throw new IllegalArgumentException(
                    "Un viaje resumido debe tener al menos una plaza disponible"
            );
        }
    }

    public static TripSummary from(Trip trip) {
        Objects.requireNonNull(trip, "El viaje es obligatorio");

        return new TripSummary(
                trip.id(),
                trip.route().origin(),
                trip.route().destination(),
                trip.departureAt().value(),
                trip.departurePoint(),
                trip.arrivalPoint(),
                trip.availableSeats().value(),
                trip.contributionAmount().value()
        );
    }
}