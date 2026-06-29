package com.daimielcr.backend.adapter.out.persistence.ride_request;

import java.util.Objects;

import org.springframework.stereotype.Repository;

import com.daimielcr.backend.application.port.out.ride_request.RideRequestRepositoryPort;
import com.daimielcr.backend.domain.model.ride_request.RideRequest;
import com.daimielcr.backend.domain.model.ride_request.RideRequestStatus;
import com.daimielcr.backend.domain.model.trip.TripId;
import com.daimielcr.backend.domain.model.user.UserId;

@Repository
public class RideRequestPersistenceAdapter
        implements RideRequestRepositoryPort {

    private final SpringDataRideRequestRepository repository;

    public RideRequestPersistenceAdapter(
            SpringDataRideRequestRepository repository
    ) {
        this.repository = Objects.requireNonNull(
                repository,
                "El repositorio JPA de solicitudes es obligatorio"
        );
    }

    @Override
    public void save(RideRequest rideRequest) {
        repository.save(
                RideRequestPersistenceMapper.toEntity(rideRequest)
        );
    }

    @Override
    public boolean existsPendingByTripIdAndPassengerId(
            TripId tripId,
            UserId passengerId
    ) {
        Objects.requireNonNull(tripId, "El id del viaje es obligatorio");
        Objects.requireNonNull(passengerId, "El pasajero es obligatorio");

        return repository.existsByTripIdAndPassengerIdAndStatus(
                tripId.value(),
                passengerId.value(),
                RideRequestStatus.PENDING
        );
    }
}