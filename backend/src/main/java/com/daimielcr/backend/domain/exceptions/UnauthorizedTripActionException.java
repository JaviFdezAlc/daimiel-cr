package com.daimielcr.backend.domain.exceptions;

public class UnauthorizedTripActionException extends RuntimeException {

    public UnauthorizedTripActionException(String message) {
        super(message);
    }
}
