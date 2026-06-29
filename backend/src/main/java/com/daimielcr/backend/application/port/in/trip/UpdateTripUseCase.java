package com.daimielcr.backend.application.port.in.trip;

public interface UpdateTripUseCase {

    TripDetail update(UpdateTripCommand command);
}