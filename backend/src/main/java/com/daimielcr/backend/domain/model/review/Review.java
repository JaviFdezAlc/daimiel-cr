package com.daimielcr.backend.domain.model.review;

import java.time.Instant;
import java.util.Objects;

import com.daimielcr.backend.domain.exceptions.InvalidReviewException;
import com.daimielcr.backend.domain.model.trip.TripId;
import com.daimielcr.backend.domain.model.user.UserId;

public class Review {

    private static final int MAX_COMMENT_LENGTH = 500;

    private final ReviewId id;
    private final TripId tripId;
    private final UserId reviewerId;
    private final UserId reviewedUserId;
    private final Rating rating;
    private final String comment;
    private final Instant createdAt;

    private Review(
            ReviewId id,
            TripId tripId,
            UserId reviewerId,
            UserId reviewedUserId,
            Rating rating,
            String comment,
            Instant createdAt
    ) {
        this.id = Objects.requireNonNull(id, "El id de la valoración es obligatorio");
        this.tripId = Objects.requireNonNull(tripId, "El viaje es obligatorio");
        this.reviewerId = Objects.requireNonNull(
                reviewerId,
                "El usuario que valora es obligatorio"
        );
        this.reviewedUserId = Objects.requireNonNull(
                reviewedUserId,
                "El usuario valorado es obligatorio"
        );
        this.rating = Objects.requireNonNull(rating, "La puntuación es obligatoria");
        this.comment = normalizeOptionalComment(comment);
        this.createdAt = Objects.requireNonNull(
                createdAt,
                "La fecha de creación es obligatoria"
        );

        ensureDifferentUsers();
    }

    public static Review create(
            ReviewId id,
            TripId tripId,
            UserId reviewerId,
            UserId reviewedUserId,
            Rating rating,
            String comment,
            Instant now
    ) {
        return new Review(
                id,
                tripId,
                reviewerId,
                reviewedUserId,
                rating,
                comment,
                now
        );
    }

    public static Review restore(
            ReviewId id,
            TripId tripId,
            UserId reviewerId,
            UserId reviewedUserId,
            Rating rating,
            String comment,
            Instant createdAt
    ) {
        return new Review(
                id,
                tripId,
                reviewerId,
                reviewedUserId,
                rating,
                comment,
                createdAt
        );
    }

    public boolean isWrittenBy(UserId userId) {
        return reviewerId.equals(userId);
    }

    public boolean isFor(UserId userId) {
        return reviewedUserId.equals(userId);
    }

    public boolean belongsToTrip(TripId tripId) {
        return this.tripId.equals(tripId);
    }

    private void ensureDifferentUsers() {
        if (reviewerId.equals(reviewedUserId)) {
            throw new InvalidReviewException(
                    "Un usuario no puede valorarse a sí mismo"
            );
        }
    }

    private static String normalizeOptionalComment(String comment) {
        if (comment == null || comment.isBlank()) {
            return null;
        }

        String normalized = comment.trim().replaceAll("\\s+", " ");

        if (normalized.length() > MAX_COMMENT_LENGTH) {
            throw new InvalidReviewException(
                    "El comentario no puede superar %d caracteres"
                            .formatted(MAX_COMMENT_LENGTH)
            );
        }

        return normalized;
    }

    public ReviewId id() {
        return id;
    }

    public TripId tripId() {
        return tripId;
    }

    public UserId reviewerId() {
        return reviewerId;
    }

    public UserId reviewedUserId() {
        return reviewedUserId;
    }

    public Rating rating() {
        return rating;
    }

    public String comment() {
        return comment;
    }

    public Instant createdAt() {
        return createdAt;
    }
}