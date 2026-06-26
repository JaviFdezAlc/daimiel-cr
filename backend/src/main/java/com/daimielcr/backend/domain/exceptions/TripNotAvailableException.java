package com.daimielcr.backend.domain.exceptions;

public class TripNotAvailableException extends RuntimeException {

    public TripNotAvailableException(String message) {
        super(message);
    }
}
