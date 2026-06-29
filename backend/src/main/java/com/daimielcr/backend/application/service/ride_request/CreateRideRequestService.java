package com.daimielcr.backend.application.service.ride_request;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

import com.daimielcr.backend.application.port.in.ride_request.CreateRideRequestCommand;
import com.daimielcr.backend.application.port.in.ride_request.CreateRideRequestUseCase;
import com.daimielcr.backend.application.port.out.ride_request.RideRequestRepositoryPort;
import com.daimielcr.backend.application.port.out.trip.TripRepositoryPort;
import com.daimielcr.backend.application.port.out.user.UserRepositoryPort;
import com.daimielcr.backend.domain.exceptions.DuplicateRideRequestException;
import com.daimielcr.backend.domain.exceptions.TripNotAvailableException;
import com.daimielcr.backend.domain.exceptions.TripNotFoundException;
import com.daimielcr.backend.domain.exceptions.UserNotFoundException;
import com.daimielcr.backend.domain.exceptions.UserPhoneNotVerifiedException;
import com.daimielcr.backend.domain.model.ride_request.RequestedSeats;
import com.daimielcr.backend.domain.model.ride_request.RideRequest;
import com.daimielcr.backend.domain.model.ride_request.RideRequestId;
import com.daimielcr.backend.domain.model.trip.Trip;
import com.daimielcr.backend.domain.model.user.User;

public class CreateRideRequestService implements CreateRideRequestUseCase {

    private final UserRepositoryPort userRepository;
    private final TripRepositoryPort tripRepository;
    private final RideRequestRepositoryPort rideRequestRepository;
    private final Clock clock;

    public CreateRideRequestService(
            UserRepositoryPort userRepository,
            TripRepositoryPort tripRepository,
            RideRequestRepositoryPort rideRequestRepository,
            Clock clock
    ) {
        this.userRepository = Objects.requireNonNull(
                userRepository,
                "El repositorio de usuarios es obligatorio"
        );
        this.tripRepository = Objects.requireNonNull(
                tripRepository,
                "El repositorio de viajes es obligatorio"
        );
        this.rideRequestRepository = Objects.requireNonNull(
                rideRequestRepository,
                "El repositorio de solicitudes es obligatorio"
        );
        this.clock = Objects.requireNonNull(
                clock,
                "El reloj es obligatorio"
        );
    }

    @Override
    public RideRequestId create(CreateRideRequestCommand command) {
        Objects.requireNonNull(command, "El comando es obligatorio");

        User passenger = userRepository.findById(command.passengerId())
                .orElseThrow(() -> new UserNotFoundException(
                        "No existe el usuario: %s"
                                .formatted(command.passengerId())
                ));

        if (!passenger.isPhoneVerified()) {
            throw new UserPhoneNotVerifiedException(
                    "Debes verificar tu teléfono antes de solicitar plaza"
            );
        }

        Trip trip = tripRepository.findById(command.tripId())
                .orElseThrow(() -> new TripNotFoundException(
                        "No existe el viaje: %s"
                                .formatted(command.tripId())
                ));

        Instant now = Instant.now(clock);

        RideRequest rideRequest = RideRequest.create(
                RideRequestId.newId(),
                trip.id(),
                trip.driverId(),
                passenger.id(),
                new RequestedSeats(command.requestedSeats()),
                command.message(),
                now
        );

        if (!trip.canAcceptRequestsAt(now)) {
            throw new TripNotAvailableException(
                    "El viaje no acepta nuevas solicitudes"
            );
        }

        if (rideRequestRepository.existsPendingByTripIdAndPassengerId(
                trip.id(),
                passenger.id()
        )) {
            throw new DuplicateRideRequestException(
                    "Ya tienes una solicitud pendiente para este viaje"
            );
        }

        rideRequestRepository.save(rideRequest);

        return rideRequest.id();
    }
}