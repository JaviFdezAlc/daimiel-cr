package com.daimielcr.backend.adapter.in.web.ride_request;

import java.time.Instant;
import java.util.UUID;

import com.daimielcr.backend.domain.model.ride_request.RideRequestStatus;

public record RideRequestSummaryResponse(
        UUID id,
        UUID passengerId,
        int requestedSeats,
        String message,
        RideRequestStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}