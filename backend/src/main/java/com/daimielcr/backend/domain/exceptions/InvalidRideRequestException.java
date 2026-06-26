package com.daimielcr.backend.domain.exceptions;

public class InvalidRideRequestException extends RuntimeException {

    public InvalidRideRequestException(String message) {
        super(message);
    }
}
