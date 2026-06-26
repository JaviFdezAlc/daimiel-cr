package com.daimielcr.backend.domain.exceptions;

public class InvalidTripException extends RuntimeException {

    public InvalidTripException(String message) {
        super(message);
    }
}
