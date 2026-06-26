package com.daimielcr.backend.domain.model.trip;

import com.daimielcr.backend.domain.exceptions.InvalidTripException;

public record SeatCount(int value) {

    public SeatCount {
        if (value < 0) {
            throw new InvalidTripException(
                    "El número de plazas no puede ser negativo"
            );
        }
    }

    public boolean isZero() {
        return value == 0;
    }

    public boolean isPositive() {
        return value > 0;
    }
}
