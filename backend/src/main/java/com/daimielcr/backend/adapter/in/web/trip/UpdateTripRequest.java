package com.daimielcr.backend.adapter.in.web.trip;

import java.math.BigDecimal;
import java.time.Instant;

import com.daimielcr.backend.domain.model.trip.TripLocation;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record UpdateTripRequest(
        @NotNull
        TripLocation origin,

        @NotNull
        TripLocation destination,

        @NotNull
        Instant departureAt,

        @Positive
        int totalSeats,

        @NotNull
        @DecimalMin(value = "0.00", inclusive = true)
        BigDecimal contributionAmount,

        @NotBlank
        @Size(max = 150)
        String departurePoint,

        @NotBlank
        @Size(max = 150)
        String arrivalPoint,

        @Size(max = 500)
        String comment
) {
}