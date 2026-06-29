package com.daimielcr.backend.application.port.in.trip;

import java.util.Objects;

import com.daimielcr.backend.domain.model.trip.TripId;
import com.daimielcr.backend.domain.model.user.UserId;

public record CancelTripCommand(
        TripId tripId,
        UserId requesterId
) {

    public CancelTripCommand {
        Objects.requireNonNull(tripId, "El id del viaje es obligatorio");
        Objects.requireNonNull(
                requesterId,
                "El usuario que cancela es obligatorio"
        );
    }
}