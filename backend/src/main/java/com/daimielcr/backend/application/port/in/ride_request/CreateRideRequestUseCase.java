package com.daimielcr.backend.application.port.in.ride_request;

import com.daimielcr.backend.domain.model.ride_request.RideRequestId;

public interface CreateRideRequestUseCase {

    RideRequestId create(CreateRideRequestCommand command);
}