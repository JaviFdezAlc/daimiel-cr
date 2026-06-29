package com.daimielcr.backend.application.port.out.trip;

import java.time.Instant;
import java.util.Objects;

import com.daimielcr.backend.application.port.in.trip.TripSort;
import com.daimielcr.backend.domain.model.trip.TripLocation;

public record TripSearchCriteria(
        TripLocation origin,
        TripLocation destination,
        Instant departureFromInclusive,
        Instant departureToExclusive,
        int requiredSeats,
        TripSort sort,
        int page,
        int size
) {

    public TripSearchCriteria {
        Objects.requireNonNull(origin, "El origen es obligatorio");
        Objects.requireNonNull(destination, "El destino es obligatorio");
        Objects.requireNonNull(
                departureFromInclusive,
                "La fecha mínima de salida es obligatoria"
        );

        if (origin == destination) {
            throw new IllegalArgumentException(
                    "El origen y el destino deben ser distintos"
            );
        }

        if (departureToExclusive != null
                && !departureToExclusive.isAfter(departureFromInclusive)) {
            throw new IllegalArgumentException(
                    "La fecha máxima debe ser posterior a la fecha mínima"
            );
        }

        if (requiredSeats < 1) {
            throw new IllegalArgumentException(
                    "Debe solicitarse al menos una plaza"
            );
        }

        if (page < 0) {
            throw new IllegalArgumentException(
                    "La página no puede ser negativa"
            );
        }

        if (size < 1) {
            throw new IllegalArgumentException(
                    "El tamaño de página debe ser mayor que cero"
            );
        }

        sort = sort == null
                ? TripSort.DEPARTURE_ASC
                : sort;
    }

    public boolean hasDepartureEnd() {
        return departureToExclusive != null;
    }
}