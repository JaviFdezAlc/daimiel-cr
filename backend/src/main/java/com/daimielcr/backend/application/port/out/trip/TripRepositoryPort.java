package com.daimielcr.backend.application.port.out.trip;

import com.daimielcr.backend.domain.model.trip.Trip;

public interface TripRepositoryPort {

    void save(Trip trip);
}
