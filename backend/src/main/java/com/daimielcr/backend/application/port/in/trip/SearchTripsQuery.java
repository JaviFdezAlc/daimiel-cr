package com.daimielcr.backend.application.port.in.trip;

import java.time.LocalDate;
import java.util.Objects;

import com.daimielcr.backend.domain.model.trip.TripLocation;

public record SearchTripsQuery(
        TripLocation origin,
        TripLocation destination,
        LocalDate date,
        int requiredSeats,
        TripSort sort,
        int page,
        int size
) {

    public static final int DEFAULT_REQUIRED_SEATS = 1;
    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 50;

    public SearchTripsQuery {
        Objects.requireNonNull(origin, "El origen es obligatorio");
        Objects.requireNonNull(destination, "El destino es obligatorio");

        if (origin == destination) {
            throw new IllegalArgumentException(
                    "El origen y el destino deben ser distintos"
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

        if (size < 1 || size > MAX_SIZE) {
            throw new IllegalArgumentException(
                    "El tamaño de página debe estar entre 1 y %d"
                            .formatted(MAX_SIZE)
            );
        }

        sort = sort == null ? TripSort.DEPARTURE_ASC : sort;
    }

    public static SearchTripsQuery defaults(
            TripLocation origin,
            TripLocation destination,
            LocalDate date
    ) {
        return new SearchTripsQuery(
                origin,
                destination,
                date,
                DEFAULT_REQUIRED_SEATS,
                TripSort.DEPARTURE_ASC,
                DEFAULT_PAGE,
                DEFAULT_SIZE
        );
    }
}