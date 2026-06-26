package com.daimielcr.backend.domain.model.user;

import java.util.Objects;

public record DisplayName(String value) {

    private static final int MIN_LENGTH = 2;
    private static final int MAX_LENGTH = 60;

    public DisplayName {
        Objects.requireNonNull(value, "El nombre es obligatorio");

        var normalizedValue = value.trim().replaceAll("\\s+", " ");

        if (normalizedValue.length() < MIN_LENGTH) {
            throw new IllegalArgumentException(
                    "El nombre debe tener al menos %d caracteres".formatted(MIN_LENGTH)
            );
        }

        if (normalizedValue.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "El nombre no puede superar los %d caracteres".formatted(MAX_LENGTH)
            );
        }

        value = normalizedValue;
    }
}
