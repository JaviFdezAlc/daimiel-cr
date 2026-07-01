package com.daimielcr.backend.application.port.in.ride_request;

import java.util.List;

public interface GetTripRideRequestsUseCase {

    List<RideRequestSummary> getForTrip(
            GetTripRideRequestsQuery query
    );
}