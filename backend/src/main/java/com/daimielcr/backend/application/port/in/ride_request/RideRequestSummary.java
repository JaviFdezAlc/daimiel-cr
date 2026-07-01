package com.daimielcr.backend.application.port.in.ride_request;

import java.time.Instant;
import java.util.Objects;

import com.daimielcr.backend.domain.model.ride_request.RideRequest;
import com.daimielcr.backend.domain.model.ride_request.RideRequestId;
import com.daimielcr.backend.domain.model.ride_request.RideRequestStatus;
import com.daimielcr.backend.domain.model.trip.TripId;
import com.daimielcr.backend.domain.model.user.UserId;

public record RideRequestSummary(
        RideRequestId id,
        TripId tripId,
        UserId passengerId,
        int requestedSeats,
        String message,
        RideRequestStatus status,
        Instant createdAt,
        Instant updatedAt
) {

    public RideRequestSummary {
        Objects.requireNonNull(id, "El id de la solicitud es obligatorio");
        Objects.requireNonNull(tripId, "El id del viaje es obligatorio");
        Objects.requireNonNull(passengerId, "El pasajero es obligatorio");
        Objects.requireNonNull(status, "El estado es obligatorio");
        Objects.requireNonNull(createdAt, "La fecha de creación es obligatoria");
        Objects.requireNonNull(updatedAt, "La fecha de actualización es obligatoria");
    }

    public static RideRequestSummary from(RideRequest rideRequest) {
        Objects.requireNonNull(
                rideRequest,
                "La solicitud es obligatoria"
        );

        return new RideRequestSummary(
                rideRequest.id(),
                rideRequest.tripId(),
                rideRequest.passengerId(),
                rideRequest.requestedSeats().value(),
                rideRequest.message(),
                rideRequest.status(),
                rideRequest.createdAt(),
                rideRequest.updatedAt()
        );
    }
}