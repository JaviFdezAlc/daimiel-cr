package com.daimielcr.backend.adapter.out.persistence.trip;

import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.daimielcr.backend.application.port.out.trip.TripRepositoryPort;
import com.daimielcr.backend.domain.model.trip.Trip;
import com.daimielcr.backend.domain.model.trip.TripId;

@Repository
public class TripPersistenceAdapter implements TripRepositoryPort {

    private final SpringDataTripRepository tripRepository;

    public TripPersistenceAdapter(SpringDataTripRepository tripRepository) {
        this.tripRepository = Objects.requireNonNull(
                tripRepository,
                "El repositorio JPA de viajes es obligatorio"
        );
    }

    @Override
    public void save(Trip trip) {
        tripRepository.save(TripPersistenceMapper.toEntity(trip));
    }

    @Override
    public Optional<Trip> findById(TripId tripId) {
        Objects.requireNonNull(tripId, "El id del viaje es obligatorio");

        return tripRepository.findById(tripId.value())
                .map(TripPersistenceMapper::toDomain);
    }
}