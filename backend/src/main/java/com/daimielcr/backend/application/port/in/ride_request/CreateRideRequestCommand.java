package com.daimielcr.backend.application.port.in.ride_request;

import java.util.Objects;

import com.daimielcr.backend.domain.model.trip.TripId;
import com.daimielcr.backend.domain.model.user.UserId;

public record CreateRideRequestCommand(
        TripId tripId,
        UserId passengerId,
        int requestedSeats,
        String message
) {

    public CreateRideRequestCommand {
        Objects.requireNonNull(tripId, "El id del viaje es obligatorio");
        Objects.requireNonNull(passengerId, "El pasajero es obligatorio");
    }
}