package com.daimielcr.backend.application.port.in.ride_request;

import java.util.Objects;

import com.daimielcr.backend.domain.model.trip.TripId;
import com.daimielcr.backend.domain.model.user.UserId;

public record GetTripRideRequestsQuery(
        TripId tripId,
        UserId requesterId
) {

    public GetTripRideRequestsQuery {
        Objects.requireNonNull(
                tripId,
                "El id del viaje es obligatorio"
        );
        Objects.requireNonNull(
                requesterId,
                "El usuario solicitante es obligatorio"
        );
    }
}