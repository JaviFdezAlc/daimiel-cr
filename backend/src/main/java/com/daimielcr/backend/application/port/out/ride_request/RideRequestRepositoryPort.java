package com.daimielcr.backend.application.port.out.ride_request;

import com.daimielcr.backend.domain.model.ride_request.RideRequest;
import com.daimielcr.backend.domain.model.trip.TripId;
import com.daimielcr.backend.domain.model.user.UserId;

public interface RideRequestRepositoryPort {

    void save(RideRequest rideRequest);

    boolean existsPendingByTripIdAndPassengerId(
            TripId tripId,
            UserId passengerId
    );
}