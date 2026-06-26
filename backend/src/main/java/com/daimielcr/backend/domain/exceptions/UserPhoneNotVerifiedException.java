package com.daimielcr.backend.domain.exceptions;

public class UserPhoneNotVerifiedException extends RuntimeException {
    public UserPhoneNotVerifiedException(String message) {
        super(message);
    }
}
