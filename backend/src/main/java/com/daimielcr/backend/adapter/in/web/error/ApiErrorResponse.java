package com.daimielcr.backend.adapter.in.web.error;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String code,
        String message,
        String path,
        Map<String, String> fieldErrors
) {

    public ApiErrorResponse {
        Objects.requireNonNull(timestamp, "El timestamp es obligatorio");
        Objects.requireNonNull(code, "El código de error es obligatorio");
        Objects.requireNonNull(message, "El mensaje es obligatorio");
        Objects.requireNonNull(path, "La ruta es obligatoria");

        fieldErrors = fieldErrors == null
                ? Map.of()
                : Map.copyOf(fieldErrors);
    }
}
