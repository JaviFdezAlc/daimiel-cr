package com.daimielcr.backend.domain.exceptions;

public class DuplicateRideRequestException extends RuntimeException {

    public DuplicateRideRequestException(String message) {
        super(message);
    }
}