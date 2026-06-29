package com.daimielcr.backend.application.port.in.trip;

public interface CancelTripUseCase {

    void cancel(CancelTripCommand command);
}