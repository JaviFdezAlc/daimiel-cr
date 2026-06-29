package com.daimielcr.backend.application.port.out.trip;

import java.util.List;
import java.util.Objects;

import com.daimielcr.backend.domain.model.trip.Trip;

public record TripSearchPage(
        List<Trip> trips,
        long totalElements
) {

    public TripSearchPage {
        Objects.requireNonNull(trips, "La lista de viajes es obligatoria");

        if (totalElements < 0) {
            throw new IllegalArgumentException(
                    "El total de elementos no puede ser negativo"
            );
        }

        trips = List.copyOf(trips);
    }
}