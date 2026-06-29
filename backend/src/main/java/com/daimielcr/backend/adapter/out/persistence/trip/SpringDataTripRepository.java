package com.daimielcr.backend.adapter.out.persistence.trip;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SpringDataTripRepository extends
        JpaRepository<TripJpaEntity, UUID>,
        JpaSpecificationExecutor<TripJpaEntity> {
}