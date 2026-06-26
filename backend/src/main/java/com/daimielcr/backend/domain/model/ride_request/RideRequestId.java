package com.daimielcr.backend.domain.model.ride_request;

import java.util.Objects;
import java.util.UUID;

public record RideRequestId(UUID value) {

    public RideRequestId {
        Objects.requireNonNull(value, "El id de la solicitud es obligatorio");
    }

    public static RideRequestId newId() {
        return new RideRequestId(UUID.randomUUID());
    }

    public static RideRequestId from(String value) {
        return new RideRequestId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
