package com.daimielcr.backend.adapter.out.persistence.trip;

import java.util.Objects;

import com.daimielcr.backend.domain.model.trip.ContributionAmount;
import com.daimielcr.backend.domain.model.trip.DepartureAt;
import com.daimielcr.backend.domain.model.trip.Route;
import com.daimielcr.backend.domain.model.trip.SeatCount;
import com.daimielcr.backend.domain.model.trip.Trip;
import com.daimielcr.backend.domain.model.trip.TripId;
import com.daimielcr.backend.domain.model.user.UserId;

public final class TripPersistenceMapper {

    private TripPersistenceMapper() {
    }

    public static TripJpaEntity toEntity(Trip trip) {
        Objects.requireNonNull(trip, "El viaje es obligatorio");

        return new TripJpaEntity(
                trip.id().value(),
                trip.driverId().value(),
                trip.route().origin(),
                trip.route().destination(),
                trip.departureAt().value(),
                trip.totalSeats().value(),
                trip.availableSeats().value(),
                trip.contributionAmount().value(),
                trip.departurePoint(),
                trip.arrivalPoint(),
                trip.comment(),
                trip.status(),
                trip.createdAt(),
                trip.updatedAt()
        );
    }

    public static Trip toDomain(TripJpaEntity entity) {
        Objects.requireNonNull(entity, "La entidad de viaje es obligatoria");

        return Trip.restore(
                new TripId(entity.getId()),
                new UserId(entity.getDriverId()),
                new Route(entity.getOrigin(), entity.getDestination()),
                new DepartureAt(entity.getDepartureAt()),
                new SeatCount(entity.getTotalSeats()),
                new SeatCount(entity.getAvailableSeats()),
                new ContributionAmount(entity.getContributionAmount()),
                entity.getDeparturePoint(),
                entity.getArrivalPoint(),
                entity.getComment(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}