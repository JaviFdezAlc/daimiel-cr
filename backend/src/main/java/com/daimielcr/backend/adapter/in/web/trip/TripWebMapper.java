package com.daimielcr.backend.adapter.in.web.trip;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.daimielcr.backend.application.port.in.trip.CancelTripCommand;
import com.daimielcr.backend.application.port.in.trip.CreateTripCommand;
import com.daimielcr.backend.application.port.in.trip.SearchTripsResult;
import com.daimielcr.backend.application.port.in.trip.TripDetail;
import com.daimielcr.backend.application.port.in.trip.TripSummary;
import com.daimielcr.backend.application.port.in.trip.UpdateTripCommand;
import com.daimielcr.backend.domain.model.trip.TripId;
import com.daimielcr.backend.domain.model.user.UserId;

public final class TripWebMapper {

        private TripWebMapper() {
        }

        public static CreateTripCommand toCommand(
                        CreateTripRequest request,
                        UserId driverId) {
                Objects.requireNonNull(request, "La petición es obligatoria");
                Objects.requireNonNull(driverId, "El conductor es obligatorio");

                return new CreateTripCommand(
                                driverId,
                                request.origin(),
                                request.destination(),
                                request.departureAt(),
                                request.totalSeats(),
                                request.contributionAmount() == null
                                                ? BigDecimal.ZERO
                                                : request.contributionAmount(),
                                request.departurePoint(),
                                request.arrivalPoint(),
                                request.comment());
        }

        public static CreateTripResponse toResponse(TripId tripId) {
                Objects.requireNonNull(tripId, "El id del viaje es obligatorio");

                return new CreateTripResponse(tripId.value());
        }

        public static TripDetailResponse toResponse(TripDetail detail) {
                Objects.requireNonNull(detail, "El detalle del viaje es obligatorio");

                return new TripDetailResponse(
                                detail.id().value(),
                                detail.driverId().value(),
                                detail.origin(),
                                detail.destination(),
                                detail.departureAt(),
                                detail.totalSeats(),
                                detail.availableSeats(),
                                detail.contributionAmount(),
                                detail.departurePoint(),
                                detail.arrivalPoint(),
                                detail.comment(),
                                detail.status(),
                                detail.createdAt(),
                                detail.updatedAt());
        }

        public static SearchTripsResponse toResponse(SearchTripsResult result) {
                Objects.requireNonNull(result, "El resultado de búsqueda es obligatorio");

                List<TripSummaryResponse> trips = result.trips()
                                .stream()
                                .map(TripWebMapper::toResponse)
                                .toList();

                return new SearchTripsResponse(
                                trips,
                                result.page(),
                                result.size(),
                                result.totalElements(),
                                result.totalPages());
        }

        private static TripSummaryResponse toResponse(TripSummary summary) {
                Objects.requireNonNull(summary, "El resumen del viaje es obligatorio");

                return new TripSummaryResponse(
                                summary.id().value(),
                                summary.origin(),
                                summary.destination(),
                                summary.departureAt(),
                                summary.departurePoint(),
                                summary.arrivalPoint(),
                                summary.availableSeats(),
                                summary.contributionAmount());
        }

        public static UpdateTripCommand toCommand(
                        UUID tripId,
                        UUID requesterId,
                        UpdateTripRequest request) {
                Objects.requireNonNull(tripId, "El id del viaje es obligatorio");
                Objects.requireNonNull(
                                requesterId,
                                "El usuario que modifica es obligatorio");
                Objects.requireNonNull(request, "La petición es obligatoria");

                return new UpdateTripCommand(
                                new TripId(tripId),
                                new UserId(requesterId),
                                request.origin(),
                                request.destination(),
                                request.departureAt(),
                                request.totalSeats(),
                                request.contributionAmount(),
                                request.departurePoint(),
                                request.arrivalPoint(),
                                request.comment());
        }

        public static CancelTripCommand toCancelCommand(
                        UUID tripId,
                        UUID requesterId) {
                Objects.requireNonNull(tripId, "El id del viaje es obligatorio");
                Objects.requireNonNull(
                                requesterId,
                                "El usuario que cancela es obligatorio");

                return new CancelTripCommand(
                                new TripId(tripId),
                                new UserId(requesterId));
        }
}