package com.daimielcr.backend.domain.model.review;

import com.daimielcr.backend.domain.exceptions.InvalidReviewException;

public record Rating(int value) {

    private static final int MIN_VALUE = 1;
    private static final int MAX_VALUE = 5;

    public Rating {
        if (value < MIN_VALUE || value > MAX_VALUE) {
            throw new InvalidReviewException(
                    "La puntuación debe estar entre %d y %d"
                            .formatted(MIN_VALUE, MAX_VALUE)
            );
        }
    }
}
