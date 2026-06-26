package com.daimielcr.backend.domain.exceptions;

public class UnauthorizedRideRequestActionException extends RuntimeException {

    public UnauthorizedRideRequestActionException(String message) {
        super(message);
    }
}
