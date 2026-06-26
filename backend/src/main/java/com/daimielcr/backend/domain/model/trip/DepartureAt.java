package com.daimielcr.backend.domain.model.trip;

import java.time.Instant;
import java.util.Objects;

public record DepartureAt(Instant value) {

    public DepartureAt {
        Objects.requireNonNull(value, "La fecha y hora de salida son obligatorias");
    }

    public boolean isAfter(Instant instant) {
        return value.isAfter(Objects.requireNonNull(instant));
    }

    public boolean hasStartedAt(Instant instant) {
        return !isAfter(instant);
    }
}
