package com.daimielcr.backend.application.port.in.trip;

import com.daimielcr.backend.domain.model.trip.TripId;

public interface CreateTripUseCase {

    TripId create(CreateTripCommand command);
}
