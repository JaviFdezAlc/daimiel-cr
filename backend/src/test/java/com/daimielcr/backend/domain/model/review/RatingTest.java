package com.daimielcr.backend.domain.model.review;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import com.daimielcr.backend.domain.exceptions.InvalidReviewException;

class RatingTest {

    @Test
    void shouldCreateRatingBetweenOneAndFive() {
        Rating rating = new Rating(4);

        assertEquals(4, rating.value());
    }

    @Test
    void shouldAcceptMinimumRating() {
        Rating rating = new Rating(1);

        assertEquals(1, rating.value());
    }

    @Test
    void shouldAcceptMaximumRating() {
        Rating rating = new Rating(5);

        assertEquals(5, rating.value());
    }

    @Test
    void shouldRejectRatingBelowMinimum() {
        assertThrows(
                InvalidReviewException.class,
                () -> new Rating(0)
        );
    }

    @Test
    void shouldRejectRatingAboveMaximum() {
        assertThrows(
                InvalidReviewException.class,
                () -> new Rating(6)
        );
    }
}
