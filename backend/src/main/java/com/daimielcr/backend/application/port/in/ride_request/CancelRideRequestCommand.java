package com.daimielcr.backend.application.port.in.ride_request;

import java.util.Objects;

import com.daimielcr.backend.domain.model.ride_request.RideRequestId;
import com.daimielcr.backend.domain.model.user.UserId;

public record CancelRideRequestCommand(
        RideRequestId rideRequestId,
        UserId requesterId
) {

    public CancelRideRequestCommand {
        Objects.requireNonNull(
                rideRequestId,
                "El id de la solicitud es obligatorio"
        );
        Objects.requireNonNull(
                requesterId,
                "El usuario que cancela es obligatorio"
        );
    }
}