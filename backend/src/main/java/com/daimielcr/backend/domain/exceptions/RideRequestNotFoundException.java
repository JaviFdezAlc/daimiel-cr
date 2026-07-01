package com.daimielcr.backend.domain.exceptions;

public class RideRequestNotFoundException extends RuntimeException {

    public RideRequestNotFoundException(String message) {
        super(message);
    }
}