package com.daimielcr.backend.adapter.in.web.ride_request;

import java.util.Objects;
import java.util.UUID;

import com.daimielcr.backend.application.port.in.ride_request.AcceptRideRequestCommand;
import com.daimielcr.backend.application.port.in.ride_request.CreateRideRequestCommand;
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
}