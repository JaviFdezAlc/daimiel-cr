package com.daimielcr.backend.domain.model.review;

import java.util.Objects;
import java.util.UUID;

public record ReviewId(UUID value) {

    public ReviewId {
        Objects.requireNonNull(value, "El id de la valoración es obligatorio");
    }

    public static ReviewId newId() {
        return new ReviewId(UUID.randomUUID());
    }

    public static ReviewId from(String value) {
        return new ReviewId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
