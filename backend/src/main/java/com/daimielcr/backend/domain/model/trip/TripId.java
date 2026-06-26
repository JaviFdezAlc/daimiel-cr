package com.daimielcr.backend.domain.model.trip;

import java.util.Objects;
import java.util.UUID;

public record TripId(UUID value) {

    public TripId {
        Objects.requireNonNull(value, "El id del viaje es obligatorio");
    }

    public static TripId newId() {
        return new TripId(UUID.randomUUID());
    }

    public static TripId from(String value) {
        return new TripId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
