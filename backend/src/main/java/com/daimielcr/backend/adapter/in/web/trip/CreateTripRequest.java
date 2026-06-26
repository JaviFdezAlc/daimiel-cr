package com.daimielcr.backend.adapter.in.web.trip;

import java.math.BigDecimal;
import java.time.Instant;

import com.daimielcr.backend.domain.model.trip.TripLocation;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateTripRequest(

        @NotNull(message = "El origen es obligatorio")
        TripLocation origin,

        @NotNull(message = "El destino es obligatorio")
        TripLocation destination,

        @NotNull(message = "La fecha y hora de salida son obligatorias")
        @Future(message = "La salida debe estar en el futuro")
        Instant departureAt,

        @NotNull(message = "El número de plazas es obligatorio")
        @Min(value = 1, message = "Debe ofrecerse al menos una plaza")
        Integer totalSeats,

        @DecimalMin(
                value = "0.00",
                inclusive = true,
                message = "La contribución no puede ser negativa"
        )
        @Digits(
                integer = 8,
                fraction = 2,
                message = "La contribución puede tener como máximo dos decimales"
        )
        BigDecimal contributionAmount,

        @Size(
                max = 120,
                message = "El punto de salida no puede superar los 120 caracteres"
        )
        String departurePoint,

        @Size(
                max = 120,
                message = "El punto de llegada no puede superar los 120 caracteres"
        )
        String arrivalPoint,

        @Size(
                max = 500,
                message = "El comentario no puede superar los 500 caracteres"
        )
        String comment
) {
}