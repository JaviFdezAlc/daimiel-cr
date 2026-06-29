package com.daimielcr.backend.application.port.in.trip;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

import com.daimielcr.backend.domain.model.trip.Trip;
import com.daimielcr.backend.domain.model.trip.TripId;
import com.daimielcr.backend.domain.model.trip.TripLocation;
import com.daimielcr.backend.domain.model.trip.TripStatus;
import com.daimielcr.backend.domain.model.user.UserId;

public record TripDetail(
        TripId id,
        UserId driverId,
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

    public TripDetail {
        Objects.requireNonNull(id, "El id del viaje es obligatorio");
        Objects.requireNonNull(driverId, "El conductor es obligatorio");
        Objects.requireNonNull(origin, "El origen es obligatorio");
        Objects.requireNonNull(destination, "El destino es obligatorio");
        Objects.requireNonNull(departureAt, "La salida es obligatoria");
        Objects.requireNonNull(contributionAmount, "La contribución es obligatoria");
        Objects.requireNonNull(status, "El estado es obligatorio");
        Objects.requireNonNull(createdAt, "La fecha de creación es obligatoria");
        Objects.requireNonNull(updatedAt, "La fecha de actualización es obligatoria");
    }

    public static TripDetail from(Trip trip) {
        Objects.requireNonNull(trip, "El viaje es obligatorio");

        return new TripDetail(
                trip.id(),
                trip.driverId(),
                trip.route().origin(),
                trip.route().destination(),
                trip.departureAt().value(),
                trip.totalSeats().value(),
                trip.availableSeats().value(),
                trip.contributionAmount().value(),
                trip.departurePoint(),
                trip.arrivalPoint(),
                trip.comment(),
                trip.status(),
                trip.createdAt(),
                trip.updatedAt()
        );
    }
}