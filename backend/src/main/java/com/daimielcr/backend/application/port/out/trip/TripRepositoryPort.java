package com.daimielcr.backend.application.port.out.trip;

import java.util.Optional;

import com.daimielcr.backend.domain.model.trip.Trip;
import com.daimielcr.backend.domain.model.trip.TripId;

public interface TripRepositoryPort {

    void save(Trip trip);

    Optional<Trip> findById(TripId tripId);

    TripSearchPage search(TripSearchCriteria criteria);

}
