package com.daimielcr.backend.domain.model.trip;

import java.time.Instant;
import java.util.Objects;

import com.daimielcr.backend.domain.exceptions.InsufficientSeatsException;
import com.daimielcr.backend.domain.exceptions.InvalidTripException;
import com.daimielcr.backend.domain.exceptions.TripNotAvailableException;
import com.daimielcr.backend.domain.exceptions.UnauthorizedTripActionException;
import com.daimielcr.backend.domain.model.user.UserId;

public class Trip {

    private static final int MAX_POINT_LENGTH = 120;
    private static final int MAX_COMMENT_LENGTH = 500;

    private final TripId id;
    private final UserId driverId;

    private Route route;
    private DepartureAt departureAt;
    private SeatCount totalSeats;
    private SeatCount availableSeats;
    private ContributionAmount contributionAmount;
    private String departurePoint;
    private String arrivalPoint;
    private String comment;
    private TripStatus status;
    private final Instant createdAt;
    private Instant updatedAt;

    private Trip(
            TripId id,
            UserId driverId,
            Route route,
            DepartureAt departureAt,
            SeatCount totalSeats,
            SeatCount availableSeats,
            ContributionAmount contributionAmount,
            String departurePoint,
            String arrivalPoint,
            String comment,
            TripStatus status,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = Objects.requireNonNull(id, "El id del viaje es obligatorio");
        this.driverId = Objects.requireNonNull(driverId, "El conductor es obligatorio");
        this.route = Objects.requireNonNull(route, "La ruta es obligatoria");
        this.departureAt = Objects.requireNonNull(
                departureAt,
                "La salida es obligatoria"
        );
        this.totalSeats = Objects.requireNonNull(
                totalSeats,
                "Las plazas totales son obligatorias"
        );
        this.availableSeats = Objects.requireNonNull(
                availableSeats,
                "Las plazas disponibles son obligatorias"
        );
        this.contributionAmount = Objects.requireNonNull(
                contributionAmount,
                "La contribución es obligatoria"
        );
        this.departurePoint = normalizeOptionalText(
                departurePoint,
                "El punto de salida",
                MAX_POINT_LENGTH
        );
        this.arrivalPoint = normalizeOptionalText(
                arrivalPoint,
                "El punto de llegada",
                MAX_POINT_LENGTH
        );
        this.comment = normalizeOptionalText(
                comment,
                "El comentario",
                MAX_COMMENT_LENGTH
        );
        this.status = Objects.requireNonNull(status, "El estado es obligatorio");
        this.createdAt = Objects.requireNonNull(
                createdAt,
                "La fecha de creación es obligatoria"
        );
        this.updatedAt = Objects.requireNonNull(
                updatedAt,
                "La fecha de actualización es obligatoria"
        );

        validateCapacityInvariant();
        validateStatusInvariant();
    }

    public static Trip create(
            TripId id,
            UserId driverId,
            Route route,
            DepartureAt departureAt,
            SeatCount totalSeats,
            ContributionAmount contributionAmount,
            String departurePoint,
            String arrivalPoint,
            String comment,
            Instant now
    ) {
        Objects.requireNonNull(totalSeats, "Las plazas totales son obligatorias");
        Objects.requireNonNull(departureAt, "La salida es obligatoria");
        Objects.requireNonNull(now, "La fecha actual es obligatoria");

        if (totalSeats.isZero()) {
            throw new InvalidTripException(
                    "Un viaje debe ofrecer al menos una plaza"
            );
        }

        if (!departureAt.isAfter(now)) {
            throw new InvalidTripException(
                    "La salida debe estar en el futuro"
            );
        }

        return new Trip(
                id,
                driverId,
                route,
                departureAt,
                totalSeats,
                totalSeats,
                contributionAmount,
                departurePoint,
                arrivalPoint,
                comment,
                TripStatus.ACTIVE,
                now,
                now
        );
    }

    public static Trip restore(
            TripId id,
            UserId driverId,
            Route route,
            DepartureAt departureAt,
            SeatCount totalSeats,
            SeatCount availableSeats,
            ContributionAmount contributionAmount,
            String departurePoint,
            String arrivalPoint,
            String comment,
            TripStatus status,
            Instant createdAt,
            Instant updatedAt
    ) {
        return new Trip(
                id,
                driverId,
                route,
                departureAt,
                totalSeats,
                availableSeats,
                contributionAmount,
                departurePoint,
                arrivalPoint,
                comment,
                status,
                createdAt,
                updatedAt
        );
    }

    public void updateDetails(
            UserId requesterId,
            Route route,
            DepartureAt departureAt,
            SeatCount newTotalSeats,
            ContributionAmount contributionAmount,
            String departurePoint,
            String arrivalPoint,
            String comment,
            Instant now
    ) {
        Objects.requireNonNull(route, "La ruta es obligatoria");
        Objects.requireNonNull(departureAt, "La salida es obligatoria");
        Objects.requireNonNull(newTotalSeats, "Las plazas son obligatorias");
        Objects.requireNonNull(contributionAmount, "La contribución es obligatoria");
        Objects.requireNonNull(now, "La fecha actual es obligatoria");

        ensureDriver(requesterId);
        ensureEditable(now);

        if (!departureAt.isAfter(now)) {
            throw new InvalidTripException(
                    "La salida debe estar en el futuro"
            );
        }

        if (newTotalSeats.isZero()) {
            throw new InvalidTripException(
                    "Un viaje debe ofrecer al menos una plaza"
            );
        }

        int occupiedSeats = occupiedSeats().value();

        if (newTotalSeats.value() < occupiedSeats) {
            throw new InvalidTripException(
                    "No se pueden reducir las plazas por debajo de las ya ocupadas"
            );
        }

        this.route = route;
        this.departureAt = departureAt;
        this.totalSeats = newTotalSeats;
        this.availableSeats = new SeatCount(
                newTotalSeats.value() - occupiedSeats
        );
        this.contributionAmount = contributionAmount;
        this.departurePoint = normalizeOptionalText(
                departurePoint,
                "El punto de salida",
                MAX_POINT_LENGTH
        );
        this.arrivalPoint = normalizeOptionalText(
                arrivalPoint,
                "El punto de llegada",
                MAX_POINT_LENGTH
        );
        this.comment = normalizeOptionalText(
                comment,
                "El comentario",
                MAX_COMMENT_LENGTH
        );
        this.status = availableSeats.isZero()
                ? TripStatus.FULL
                : TripStatus.ACTIVE;
        this.updatedAt = now;
    }

    public void reserveSeats(SeatCount seats, Instant now) {
        Objects.requireNonNull(seats, "Las plazas solicitadas son obligatorias");
        Objects.requireNonNull(now, "La fecha actual es obligatoria");

        ensureAcceptsRequests(now);

        if (!seats.isPositive()) {
            throw new InvalidTripException(
                    "La solicitud debe incluir al menos una plaza"
            );
        }

        if (seats.value() > availableSeats.value()) {
            throw new InsufficientSeatsException(
                    "No hay suficientes plazas disponibles"
            );
        }

        this.availableSeats = new SeatCount(
                availableSeats.value() - seats.value()
        );

        if (availableSeats.isZero()) {
            this.status = TripStatus.FULL;
        }

        this.updatedAt = now;
    }

    public void releaseSeats(SeatCount seats, Instant now) {
        Objects.requireNonNull(seats, "Las plazas a liberar son obligatorias");
        Objects.requireNonNull(now, "La fecha actual es obligatoria");

        ensureEditable(now);

        if (!seats.isPositive()) {
            throw new InvalidTripException(
                    "Las plazas a liberar deben ser mayores que cero"
            );
        }

        int occupiedSeats = occupiedSeats().value();

        if (seats.value() > occupiedSeats) {
            throw new InvalidTripException(
                    "No se pueden liberar más plazas de las ocupadas"
            );
        }

        this.availableSeats = new SeatCount(
                availableSeats.value() + seats.value()
        );
        this.status = TripStatus.ACTIVE;
        this.updatedAt = now;
    }

    public void cancel(UserId requesterId, Instant now) {
        Objects.requireNonNull(now, "La fecha actual es obligatoria");

        ensureDriver(requesterId);
        ensureEditable(now);

        this.status = TripStatus.CANCELLED;
        this.updatedAt = now;
    }

    public void finish(UserId requesterId, Instant now) {
        Objects.requireNonNull(now, "La fecha actual es obligatoria");

        ensureDriver(requesterId);

        if (status == TripStatus.CANCELLED) {
            throw new TripNotAvailableException(
                    "No se puede finalizar un viaje cancelado"
            );
        }

        if (status == TripStatus.FINISHED) {
            throw new TripNotAvailableException(
                    "El viaje ya está finalizado"
            );
        }

        if (!departureAt.hasStartedAt(now)) {
            throw new TripNotAvailableException(
                    "No se puede finalizar un viaje antes de su salida"
            );
        }

        this.status = TripStatus.FINISHED;
        this.updatedAt = now;
    }

    public boolean isOwnedBy(UserId userId) {
        return driverId.equals(userId);
    }

    public boolean canAcceptRequestsAt(Instant now) {
        Objects.requireNonNull(now, "La fecha actual es obligatoria");

        return status == TripStatus.ACTIVE
                && departureAt.isAfter(now)
                && availableSeats.isPositive();
    }

    public SeatCount occupiedSeats() {
        return new SeatCount(
                totalSeats.value() - availableSeats.value()
        );
    }

    private void ensureDriver(UserId requesterId) {
        if (!driverId.equals(requesterId)) {
            throw new UnauthorizedTripActionException(
                    "Solo el conductor puede modificar este viaje"
            );
        }
    }

    private void ensureEditable(Instant now) {
        if (status != TripStatus.ACTIVE && status != TripStatus.FULL) {
            throw new TripNotAvailableException(
                    "El viaje no puede modificarse en su estado actual"
            );
        }

        if (!departureAt.isAfter(now)) {
            throw new TripNotAvailableException(
                    "No se puede modificar un viaje cuya salida ya ha comenzado"
            );
        }
    }

    private void ensureAcceptsRequests(Instant now) {
        if (!canAcceptRequestsAt(now)) {
            throw new TripNotAvailableException(
                    "El viaje no acepta nuevas solicitudes"
            );
        }
    }

    private void validateCapacityInvariant() {
        if (totalSeats.isZero()) {
            throw new InvalidTripException(
                    "Un viaje debe tener al menos una plaza"
            );
        }

        if (availableSeats.value() > totalSeats.value()) {
            throw new InvalidTripException(
                    "Las plazas disponibles no pueden superar las plazas totales"
            );
        }
    }

    private void validateStatusInvariant() {
        if (status == TripStatus.ACTIVE && availableSeats.isZero()) {
            throw new InvalidTripException(
                    "Un viaje activo debe tener plazas disponibles"
            );
        }

        if (status == TripStatus.FULL && availableSeats.isPositive()) {
            throw new InvalidTripException(
                    "Un viaje completo no puede tener plazas disponibles"
            );
        }
    }

    private static String normalizeOptionalText(
            String value,
            String fieldName,
            int maxLength
    ) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.trim().replaceAll("\\s+", " ");

        if (normalized.length() > maxLength) {
            throw new InvalidTripException(
                    "%s no puede superar %d caracteres"
                            .formatted(fieldName, maxLength)
            );
        }

        return normalized;
    }

    public TripId id() {
        return id;
    }

    public UserId driverId() {
        return driverId;
    }

    public Route route() {
        return route;
    }

    public DepartureAt departureAt() {
        return departureAt;
    }

    public SeatCount totalSeats() {
        return totalSeats;
    }

    public SeatCount availableSeats() {
        return availableSeats;
    }

    public ContributionAmount contributionAmount() {
        return contributionAmount;
    }

    public String departurePoint() {
        return departurePoint;
    }

    public String arrivalPoint() {
        return arrivalPoint;
    }

    public String comment() {
        return comment;
    }

    public TripStatus status() {
        return status;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }
}
