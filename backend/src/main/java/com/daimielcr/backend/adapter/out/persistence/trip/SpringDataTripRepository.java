package com.daimielcr.backend.adapter.out.persistence.trip;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataTripRepository
        extends JpaRepository<TripJpaEntity, UUID> {
}