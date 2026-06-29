package com.daimielcr.backend.application.port.in.trip;

import java.util.List;
import java.util.Objects;

public record SearchTripsResult(
        List<TripSummary> trips,
        int page,
        int size,
        long totalElements,
        int totalPages
) {

    public SearchTripsResult {
        Objects.requireNonNull(trips, "La lista de viajes es obligatoria");

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

        if (totalElements < 0) {
            throw new IllegalArgumentException(
                    "El total de elementos no puede ser negativo"
            );
        }

        if (totalPages < 0) {
            throw new IllegalArgumentException(
                    "El total de páginas no puede ser negativo"
            );
        }

        trips = List.copyOf(trips);
    }

    public boolean isEmpty() {
        return trips.isEmpty();
    }
}