package com.daimielcr.backend.adapter.out.persistence.trip;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import com.daimielcr.backend.domain.model.trip.TripLocation;
import com.daimielcr.backend.domain.model.trip.TripStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "trips")
public class TripJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "driver_id", nullable = false, updatable = false)
    private UUID driverId;

    @Enumerated(EnumType.STRING)
    @Column(name = "origin", nullable = false, length = 20)
    private TripLocation origin;

    @Enumerated(EnumType.STRING)
    @Column(name = "destination", nullable = false, length = 20)
    private TripLocation destination;

    @Column(name = "departure_at", nullable = false)
    private Instant departureAt;

    @Column(name = "total_seats", nullable = false)
    private int totalSeats;

    @Column(name = "available_seats", nullable = false)
    private int availableSeats;

    @Column(name = "contribution_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal contributionAmount;

    @Column(name = "departure_point", length = 120)
    private String departurePoint;

    @Column(name = "arrival_point", length = 120)
    private String arrivalPoint;

    @Column(name = "comment", length = 500)
    private String comment;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private TripStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected TripJpaEntity() {
        // Required by JPA.
    }

    public TripJpaEntity(
            UUID id,
            UUID driverId,
            TripLocation origin,
            TripLocation destination,
            Instant departureAt,
            int totalSeats,
            int availableSeats,
            BigDecimal contributionAmount,
            String departurePoint,
            String arrivalPoint,
            String comment,
            TripStatus status,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = Objects.requireNonNull(id);
        this.driverId = Objects.requireNonNull(driverId);
        this.origin = Objects.requireNonNull(origin);
        this.destination = Objects.requireNonNull(destination);
        this.departureAt = Objects.requireNonNull(departureAt);
        this.totalSeats = totalSeats;
        this.availableSeats = availableSeats;
        this.contributionAmount = Objects.requireNonNull(contributionAmount);
        this.departurePoint = departurePoint;
        this.arrivalPoint = arrivalPoint;
        this.comment = comment;
        this.status = Objects.requireNonNull(status);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public UUID getId() {
        return id;
    }

    public UUID getDriverId() {
        return driverId;
    }

    public TripLocation getOrigin() {
        return origin;
    }

    public TripLocation getDestination() {
        return destination;
    }

    public Instant getDepartureAt() {
        return departureAt;
    }

    public int getTotalSeats() {
        return totalSeats;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    public BigDecimal getContributionAmount() {
        return contributionAmount;
    }

    public String getDeparturePoint() {
        return departurePoint;
    }

    public String getArrivalPoint() {
        return arrivalPoint;
    }

    public String getComment() {
        return comment;
    }

    public TripStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}