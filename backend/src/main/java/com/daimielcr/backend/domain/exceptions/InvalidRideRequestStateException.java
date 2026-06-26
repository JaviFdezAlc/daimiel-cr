package com.daimielcr.backend.domain.exceptions;

public class InvalidRideRequestStateException extends RuntimeException {

    public InvalidRideRequestStateException(String message) {
        super(message);
    }
}
