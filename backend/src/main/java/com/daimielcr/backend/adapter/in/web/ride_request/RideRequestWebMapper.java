package com.daimielcr.backend.adapter.in.web.ride_request;

import java.util.Objects;
import java.util.UUID;

import com.daimielcr.backend.application.port.in.ride_request.AcceptRideRequestCommand;
import com.daimielcr.backend.application.port.in.ride_request.CancelRideRequestCommand;
import com.daimielcr.backend.application.port.in.ride_request.CreateRideRequestCommand;
import com.daimielcr.backend.application.port.in.ride_request.GetTripRideRequestsQuery;
import com.daimielcr.backend.application.port.in.ride_request.RejectRideRequestCommand;
import com.daimielcr.backend.application.port.in.ride_request.RideRequestSummary;
import com.daimielcr.backend.domain.model.ride_request.RideRequestId;
import com.daimielcr.backend.domain.model.trip.TripId;
import com.daimielcr.backend.domain.model.user.UserId;

public final class RideRequestWebMapper {

        private RideRequestWebMapper() {
        }

        public static CreateRideRequestCommand toCommand(
                        UUID tripId,
                        UUID passengerId,
                        CreateRideRequestRequest request) {
                Objects.requireNonNull(tripId, "El id del viaje es obligatorio");
                Objects.requireNonNull(
                                passengerId,
                                "El pasajero es obligatorio");
                Objects.requireNonNull(request, "La petición es obligatoria");

                return new CreateRideRequestCommand(
                                new TripId(tripId),
                                new UserId(passengerId),
                                request.requestedSeats(),
                                request.message());
        }

        public static CreateRideRequestResponse toResponse(
                        RideRequestId rideRequestId) {
                Objects.requireNonNull(
                                rideRequestId,
                                "El id de la solicitud es obligatorio");

                return new CreateRideRequestResponse(
                                rideRequestId.value());
        }

        public static AcceptRideRequestCommand toAcceptCommand(
                        UUID rideRequestId,
                        UUID requesterId) {
                Objects.requireNonNull(
                                rideRequestId,
                                "El id de la solicitud es obligatorio");
                Objects.requireNonNull(
                                requesterId,
                                "El usuario que acepta es obligatorio");

                return new AcceptRideRequestCommand(
                                new RideRequestId(rideRequestId),
                                new UserId(requesterId));
        }

        public static RejectRideRequestCommand toRejectCommand(
                        UUID rideRequestId,
                        UUID requesterId) {
                Objects.requireNonNull(
                                rideRequestId,
                                "El id de la solicitud es obligatorio");
                Objects.requireNonNull(
                                requesterId,
                                "El usuario que rechaza es obligatorio");

                return new RejectRideRequestCommand(
                                new RideRequestId(rideRequestId),
                                new UserId(requesterId));
        }

        public static CancelRideRequestCommand toCancelCommand(
                        UUID rideRequestId,
                        UUID requesterId) {
                Objects.requireNonNull(
                                rideRequestId,
                                "El id de la solicitud es obligatorio");
                Objects.requireNonNull(
                                requesterId,
                                "El usuario que cancela es obligatorio");

                return new CancelRideRequestCommand(
                                new RideRequestId(rideRequestId),
                                new UserId(requesterId));
        }

        public static GetTripRideRequestsQuery toGetTripRideRequestsQuery(
                        UUID tripId,
                        UUID requesterId) {
                Objects.requireNonNull(tripId, "El id del viaje es obligatorio");
                Objects.requireNonNull(
                                requesterId,
                                "El usuario solicitante es obligatorio");

                return new GetTripRideRequestsQuery(
                                new TripId(tripId),
                                new UserId(requesterId));
        }

        public static RideRequestSummaryResponse toResponse(
                        RideRequestSummary summary) {
                Objects.requireNonNull(
                                summary,
                                "El resumen de la solicitud es obligatorio");

                return new RideRequestSummaryResponse(
                                summary.id().value(),
                                summary.passengerId().value(),
                                summary.requestedSeats(),
                                summary.message(),
                                summary.status(),
                                summary.createdAt(),
                                summary.updatedAt());
        }
}