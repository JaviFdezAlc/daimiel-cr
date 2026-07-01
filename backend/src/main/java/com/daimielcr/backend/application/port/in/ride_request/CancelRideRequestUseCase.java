package com.daimielcr.backend.application.port.in.ride_request;

public interface CancelRideRequestUseCase {

    void cancel(CancelRideRequestCommand command);
}