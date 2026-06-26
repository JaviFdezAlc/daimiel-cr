package com.daimielcr.backend.domain.model.review;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.daimielcr.backend.domain.exceptions.InvalidReviewException;
import com.daimielcr.backend.domain.model.trip.TripId;
import com.daimielcr.backend.domain.model.user.UserId;

class ReviewTest {

    private static final Instant NOW = Instant.parse("2026-06-26T10:00:00Z");

    private static final TripId TRIP_ID =
            new TripId(UUID.fromString("11111111-1111-1111-1111-111111111111"));

    private static final UserId REVIEWER_ID =
            new UserId(UUID.fromString("22222222-2222-2222-2222-222222222222"));

    private static final UserId REVIEWED_USER_ID =
            new UserId(UUID.fromString("33333333-3333-3333-3333-333333333333"));

    private static final UserId OTHER_USER_ID =
            new UserId(UUID.fromString("44444444-4444-4444-4444-444444444444"));

    @Test
    void shouldCreateReview() {
        Review review = Review.create(
                ReviewId.newId(),
                TRIP_ID,
                REVIEWER_ID,
                REVIEWED_USER_ID,
                new Rating(5),
                "  Muy puntual y agradable.  ",
                NOW
        );

        assertEquals(TRIP_ID, review.tripId());
        assertEquals(REVIEWER_ID, review.reviewerId());
        assertEquals(REVIEWED_USER_ID, review.reviewedUserId());
        assertEquals(new Rating(5), review.rating());
        assertEquals("Muy puntual y agradable.", review.comment());
        assertEquals(NOW, review.createdAt());
    }

    @Test
    void shouldCreateReviewWithoutComment() {
        Review review = Review.create(
                ReviewId.newId(),
                TRIP_ID,
                REVIEWER_ID,
                REVIEWED_USER_ID,
                new Rating(4),
                "   ",
                NOW
        );

        assertNull(review.comment());
    }

    @Test
    void shouldRejectReviewToSelf() {
        assertThrows(
                InvalidReviewException.class,
                () -> Review.create(
                        ReviewId.newId(),
                        TRIP_ID,
                        REVIEWER_ID,
                        REVIEWER_ID,
                        new Rating(5),
                        "Buen viaje",
                        NOW
                )
        );
    }

    @Test
    void shouldRejectCommentLongerThanMaximumLength() {
        String comment = "a".repeat(501);

        assertThrows(
                InvalidReviewException.class,
                () -> Review.create(
                        ReviewId.newId(),
                        TRIP_ID,
                        REVIEWER_ID,
                        REVIEWED_USER_ID,
                        new Rating(5),
                        comment,
                        NOW
                )
        );
    }

    @Test
    void shouldRecognizeReviewAuthor() {
        Review review = review();

        assertTrue(review.isWrittenBy(REVIEWER_ID));
        assertFalse(review.isWrittenBy(OTHER_USER_ID));
    }

    @Test
    void shouldRecognizeReviewedUser() {
        Review review = review();

        assertTrue(review.isFor(REVIEWED_USER_ID));
        assertFalse(review.isFor(OTHER_USER_ID));
    }

    @Test
    void shouldRecognizeAssociatedTrip() {
        Review review = review();

        assertTrue(review.belongsToTrip(TRIP_ID));
        assertFalse(review.belongsToTrip(TripId.newId()));
    }

    private Review review() {
        return Review.create(
                ReviewId.newId(),
                TRIP_ID,
                REVIEWER_ID,
                REVIEWED_USER_ID,
                new Rating(5),
                "Viaje muy cómodo",
                NOW
        );
    }
}
