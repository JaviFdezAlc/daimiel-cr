package com.daimielcr.backend.application.port.in.ride_request;

public interface RejectRideRequestUseCase {

    void reject(RejectRideRequestCommand command);
}