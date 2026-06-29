package com.daimielcr.backend.adapter.out.persistence.trip;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import com.daimielcr.backend.application.port.in.trip.TripSort;
import com.daimielcr.backend.application.port.out.trip.TripRepositoryPort;
import com.daimielcr.backend.application.port.out.trip.TripSearchCriteria;
import com.daimielcr.backend.application.port.out.trip.TripSearchPage;
import com.daimielcr.backend.domain.model.trip.Trip;
import com.daimielcr.backend.domain.model.trip.TripId;
import com.daimielcr.backend.domain.model.trip.TripStatus;

import jakarta.persistence.criteria.Predicate;

@Repository
public class TripPersistenceAdapter implements TripRepositoryPort {

    private final SpringDataTripRepository tripRepository;

    public TripPersistenceAdapter(SpringDataTripRepository tripRepository) {
        this.tripRepository = Objects.requireNonNull(
                tripRepository,
                "El repositorio JPA de viajes es obligatorio");
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

    @Override
    public TripSearchPage search(TripSearchCriteria criteria) {
        Objects.requireNonNull(criteria, "Los criterios de búsqueda son obligatorios");

        Specification<TripJpaEntity> specification = (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(builder.equal(
                    root.get("origin"),
                    criteria.origin()));

            predicates.add(builder.equal(
                    root.get("destination"),
                    criteria.destination()));

            predicates.add(builder.equal(
                    root.get("status"),
                    TripStatus.ACTIVE));

            predicates.add(builder.greaterThanOrEqualTo(
                    root.<Instant>get("departureAt"),
                    criteria.departureFromInclusive()));

            predicates.add(builder.greaterThanOrEqualTo(
                    root.<Integer>get("availableSeats"),
                    criteria.requiredSeats()));

            if (criteria.hasDepartureEnd()) {
                predicates.add(builder.lessThan(
                        root.<Instant>get("departureAt"),
                        criteria.departureToExclusive()));
            }

            return builder.and(predicates.toArray(Predicate[]::new));
        };

        Page<TripJpaEntity> result = tripRepository.findAll(
                specification,
                PageRequest.of(
                        criteria.page(),
                        criteria.size(),
                        toSort(criteria.sort())));

        return new TripSearchPage(
                result.getContent()
                        .stream()
                        .map(TripPersistenceMapper::toDomain)
                        .toList(),
                result.getTotalElements());
    }

    private Sort toSort(TripSort sort) {
        return switch (sort) {
            case DEPARTURE_ASC -> Sort.by(
                    Sort.Order.asc("departureAt"));

            case CONTRIBUTION_ASC -> Sort.by(
                    Sort.Order.asc("contributionAmount"),
                    Sort.Order.asc("departureAt"));

            case AVAILABLE_SEATS_DESC -> Sort.by(
                    Sort.Order.desc("availableSeats"),
                    Sort.Order.asc("departureAt"));
        };
    }
}