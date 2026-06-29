package com.daimielcr.backend.adapter.out.persistence.ride_request;

import java.util.Objects;

import com.daimielcr.backend.domain.model.ride_request.RequestedSeats;
import com.daimielcr.backend.domain.model.ride_request.RideRequest;
import com.daimielcr.backend.domain.model.ride_request.RideRequestId;
import com.daimielcr.backend.domain.model.trip.TripId;
import com.daimielcr.backend.domain.model.user.UserId;

public final class RideRequestPersistenceMapper {

    private RideRequestPersistenceMapper() {
    }

    public static RideRequestJpaEntity toEntity(RideRequest rideRequest) {
        Objects.requireNonNull(
                rideRequest,
                "La solicitud es obligatoria"
        );

        return new RideRequestJpaEntity(
                rideRequest.id().value(),
                rideRequest.tripId().value(),
                rideRequest.passengerId().value(),
                rideRequest.requestedSeats().value(),
                rideRequest.message(),
                rideRequest.status(),
                rideRequest.createdAt(),
                rideRequest.updatedAt()
        );
    }

    public static RideRequest toDomain(RideRequestJpaEntity entity) {
        Objects.requireNonNull(entity, "La entidad es obligatoria");

        return RideRequest.restore(
                new RideRequestId(entity.getId()),
                new TripId(entity.getTripId()),
                new UserId(entity.getPassengerId()),
                new RequestedSeats(entity.getRequestedSeats()),
                entity.getMessage(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}