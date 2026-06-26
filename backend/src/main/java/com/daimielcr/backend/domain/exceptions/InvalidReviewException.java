package com.daimielcr.backend.domain.exceptions;

public class InvalidReviewException extends RuntimeException {

    public InvalidReviewException(String message) {
        super(message);
    }
}
