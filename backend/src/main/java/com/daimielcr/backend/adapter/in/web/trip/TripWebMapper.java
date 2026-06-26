package com.daimielcr.backend.adapter.in.web.trip;

import java.math.BigDecimal;
import java.util.Objects;

import com.daimielcr.backend.application.port.in.trip.CreateTripCommand;
import com.daimielcr.backend.domain.model.trip.TripId;
import com.daimielcr.backend.domain.model.user.UserId;

public final class TripWebMapper {

    private TripWebMapper() {
    }

    public static CreateTripCommand toCommand(
            CreateTripRequest request,
            UserId driverId
    ) {
        Objects.requireNonNull(request, "La petición es obligatoria");
        Objects.requireNonNull(driverId, "El conductor es obligatorio");

        return new CreateTripCommand(
                driverId,
                request.origin(),
                request.destination(),
                request.departureAt(),
                request.totalSeats(),
                request.contributionAmount() == null
                        ? BigDecimal.ZERO
                        : request.contributionAmount(),
                request.departurePoint(),
                request.arrivalPoint(),
                request.comment()
        );
    }

    public static CreateTripResponse toResponse(TripId tripId) {
        Objects.requireNonNull(tripId, "El id del viaje es obligatorio");

        return new CreateTripResponse(tripId.value());
    }
}