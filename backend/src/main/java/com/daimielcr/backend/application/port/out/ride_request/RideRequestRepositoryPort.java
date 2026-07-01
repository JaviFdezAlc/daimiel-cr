package com.daimielcr.backend.application.port.out.ride_request;

import java.util.List;
import java.util.Optional;

import com.daimielcr.backend.domain.model.ride_request.RideRequest;
import com.daimielcr.backend.domain.model.ride_request.RideRequestId;
import com.daimielcr.backend.domain.model.trip.TripId;
import com.daimielcr.backend.domain.model.user.UserId;

public interface RideRequestRepositoryPort {

    void save(RideRequest rideRequest);

    Optional<RideRequest> findById(RideRequestId rideRequestId);

    boolean existsPendingByTripIdAndPassengerId(
            TripId tripId,
            UserId passengerId
    );

    List<RideRequest> findAllByTripId(TripId tripId);
}