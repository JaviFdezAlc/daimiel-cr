package com.daimielcr.backend.application.port.in.trip;

import com.daimielcr.backend.domain.model.trip.TripId;

public interface GetTripDetailUseCase {

    TripDetail getById(TripId tripId);
}