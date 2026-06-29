package com.daimielcr.backend.adapter.in.web.trip;

import java.util.List;
import java.util.Objects;

public record SearchTripsResponse(
        List<TripSummaryResponse> trips,
        int page,
        int size,
        long totalElements,
        int totalPages
) {

    public SearchTripsResponse {
        Objects.requireNonNull(trips, "La lista de viajes es obligatoria");

        trips = List.copyOf(trips);
    }
}