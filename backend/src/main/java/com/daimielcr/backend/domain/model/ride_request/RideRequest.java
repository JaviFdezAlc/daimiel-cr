package com.daimielcr.backend.domain.model.ride_request;

import java.time.Instant;
import java.util.Objects;

import com.daimielcr.backend.domain.exceptions.InvalidRideRequestException;
import com.daimielcr.backend.domain.exceptions.InvalidRideRequestStateException;
import com.daimielcr.backend.domain.exceptions.UnauthorizedRideRequestActionException;
import com.daimielcr.backend.domain.model.trip.TripId;
import com.daimielcr.backend.domain.model.user.UserId;

public class RideRequest {

    private static final int MAX_MESSAGE_LENGTH = 300;

    private final RideRequestId id;
    private final TripId tripId;
    private final UserId passengerId;
    private final RequestedSeats requestedSeats;

    private String message;
    private RideRequestStatus status;
    private final Instant createdAt;
    private Instant updatedAt;

    private RideRequest(
            RideRequestId id,
            TripId tripId,
            UserId passengerId,
            RequestedSeats requestedSeats,
            String message,
            RideRequestStatus status,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = Objects.requireNonNull(id, "El id de la solicitud es obligatorio");
        this.tripId = Objects.requireNonNull(tripId, "El id del viaje es obligatorio");
        this.passengerId = Objects.requireNonNull(
                passengerId,
                "El pasajero es obligatorio"
        );
        this.requestedSeats = Objects.requireNonNull(
                requestedSeats,
                "Las plazas solicitadas son obligatorias"
        );
        this.message = normalizeMessage(message);
        this.status = Objects.requireNonNull(status, "El estado es obligatorio");
        this.createdAt = Objects.requireNonNull(
                createdAt,
                "La fecha de creación es obligatoria"
        );
        this.updatedAt = Objects.requireNonNull(
                updatedAt,
                "La fecha de actualización es obligatoria"
        );
    }

    public static RideRequest create(
            RideRequestId id,
            TripId tripId,
            UserId tripDriverId,
            UserId passengerId,
            RequestedSeats requestedSeats,
            String message,
            Instant now
    ) {
        Objects.requireNonNull(tripDriverId, "El conductor del viaje es obligatorio");
        Objects.requireNonNull(passengerId, "El pasajero es obligatorio");
        Objects.requireNonNull(now, "La fecha actual es obligatoria");

        if (tripDriverId.equals(passengerId)) {
            throw new InvalidRideRequestException(
                    "No puedes solicitar plaza en tu propio viaje"
            );
        }

        return new RideRequest(
                id,
                tripId,
                passengerId,
                requestedSeats,
                message,
                RideRequestStatus.PENDING,
                now,
                now
        );
    }

    public static RideRequest restore(
            RideRequestId id,
            TripId tripId,
            UserId passengerId,
            RequestedSeats requestedSeats,
            String message,
            RideRequestStatus status,
            Instant createdAt,
            Instant updatedAt
    ) {
        return new RideRequest(
                id,
                tripId,
                passengerId,
                requestedSeats,
                message,
                status,
                createdAt,
                updatedAt
        );
    }

    public void accept(Instant now) {
        ensurePending();
        this.status = RideRequestStatus.ACCEPTED;
        this.updatedAt = Objects.requireNonNull(now, "La fecha actual es obligatoria");
    }

    public void reject(Instant now) {
        ensurePending();
        this.status = RideRequestStatus.REJECTED;
        this.updatedAt = Objects.requireNonNull(now, "La fecha actual es obligatoria");
    }

    public void cancel(UserId requesterId, Instant now) {
        Objects.requireNonNull(requesterId, "El usuario que cancela es obligatorio");
        Objects.requireNonNull(now, "La fecha actual es obligatoria");

        ensurePassenger(requesterId);
        ensureCancellable();

        this.status = RideRequestStatus.CANCELLED;
        this.updatedAt = now;
    }

    public boolean isPending() {
        return status == RideRequestStatus.PENDING;
    }

    public boolean isAccepted() {
        return status == RideRequestStatus.ACCEPTED;
    }

    public boolean requiresSeatReleaseWhenCancelled() {
        return isAccepted();
    }

    public boolean belongsToPassenger(UserId userId) {
        return passengerId.equals(userId);
    }

    private void ensurePending() {
        if (status != RideRequestStatus.PENDING) {
            throw new InvalidRideRequestStateException(
                    "Solo se puede realizar esta acción sobre una solicitud pendiente"
            );
        }
    }

    private void ensureCancellable() {
        if (status != RideRequestStatus.PENDING
                && status != RideRequestStatus.ACCEPTED) {
            throw new InvalidRideRequestStateException(
                    "La solicitud no puede cancelarse en su estado actual"
            );
        }
    }

    private void ensurePassenger(UserId requesterId) {
        if (!passengerId.equals(requesterId)) {
            throw new UnauthorizedRideRequestActionException(
                    "Solo el pasajero puede cancelar esta solicitud"
            );
        }
    }

    private static String normalizeMessage(String message) {
        if (message == null || message.isBlank()) {
            return null;
        }

        String normalized = message.trim().replaceAll("\\s+", " ");

        if (normalized.length() > MAX_MESSAGE_LENGTH) {
            throw new InvalidRideRequestException(
                    "El mensaje no puede superar %d caracteres"
                            .formatted(MAX_MESSAGE_LENGTH)
            );
        }

        return normalized;
    }

    public RideRequestId id() {
        return id;
    }

    public TripId tripId() {
        return tripId;
    }

    public UserId passengerId() {
        return passengerId;
    }

    public RequestedSeats requestedSeats() {
        return requestedSeats;
    }

    public String message() {
        return message;
    }

    public RideRequestStatus status() {
        return status;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }
}
