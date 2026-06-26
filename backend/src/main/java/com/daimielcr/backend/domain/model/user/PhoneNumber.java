package com.daimielcr.backend.domain.model.user;

import java.util.Objects;
import java.util.regex.Pattern;

public record PhoneNumber(String value) {

    private static final Pattern E164_PATTERN =
            Pattern.compile("^\\+[1-9]\\d{7,14}$");

    public PhoneNumber {
        Objects.requireNonNull(value, "El número de teléfono es obligatorio");

        var normalizedValue = value.replaceAll("[\\s-]", "");

        if (!E164_PATTERN.matcher(normalizedValue).matches()) {
            throw new IllegalArgumentException(
                    "El número de teléfono debe tener formato internacional E.164"
            );
        }

        value = normalizedValue;
    }

    public String whatsappValue() {
        return value.substring(1);
    }
}
