package com.daimielcr.backend.adapter.out.persistence.ride_request;

import java.time.Instant;
import java.util.UUID;

import com.daimielcr.backend.domain.model.ride_request.RideRequestStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "ride_requests")
public class RideRequestJpaEntity {

    @Id
    private UUID id;

    @Column(name = "trip_id", nullable = false)
    private UUID tripId;

    @Column(name = "passenger_id", nullable = false)
    private UUID passengerId;

    @Column(name = "requested_seats", nullable = false)
    private int requestedSeats;

    @Column(length = 300)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RideRequestStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected RideRequestJpaEntity() {
    }

    public RideRequestJpaEntity(
            UUID id,
            UUID tripId,
            UUID passengerId,
            int requestedSeats,
            String message,
            RideRequestStatus status,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.tripId = tripId;
        this.passengerId = passengerId;
        this.requestedSeats = requestedSeats;
        this.message = message;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getTripId() {
        return tripId;
    }

    public UUID getPassengerId() {
        return passengerId;
    }

    public int getRequestedSeats() {
        return requestedSeats;
    }

    public String getMessage() {
        return message;
    }

    public RideRequestStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}