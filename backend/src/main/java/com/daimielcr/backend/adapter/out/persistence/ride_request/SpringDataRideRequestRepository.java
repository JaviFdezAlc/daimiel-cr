package com.daimielcr.backend.adapter.out.persistence.ride_request;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.daimielcr.backend.domain.model.ride_request.RideRequestStatus;

public interface SpringDataRideRequestRepository
                extends JpaRepository<RideRequestJpaEntity, UUID> {

        boolean existsByTripIdAndPassengerIdAndStatus(
                        UUID tripId,
                        UUID passengerId,
                        RideRequestStatus status);

        List<RideRequestJpaEntity> findAllByTripIdOrderByCreatedAtAsc(
                        UUID tripId);
}