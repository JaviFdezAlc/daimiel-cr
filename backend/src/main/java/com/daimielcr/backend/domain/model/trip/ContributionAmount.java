package com.daimielcr.backend.domain.model.trip;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

import com.daimielcr.backend.domain.exceptions.InvalidTripException;

public record ContributionAmount(BigDecimal value) {

    public ContributionAmount {
        Objects.requireNonNull(value, "La contribución es obligatoria");

        if (value.signum() < 0) {
            throw new InvalidTripException(
                    "La contribución a gastos no puede ser negativa"
            );
        }

        if (value.stripTrailingZeros().scale() > 2) {
            throw new InvalidTripException(
                    "La contribución solo puede tener dos decimales"
            );
        }

        value = value.setScale(2, RoundingMode.UNNECESSARY);
    }

    public static ContributionAmount none() {
        return new ContributionAmount(BigDecimal.ZERO);
    }

    public boolean isFree() {
        return value.signum() == 0;
    }
}
