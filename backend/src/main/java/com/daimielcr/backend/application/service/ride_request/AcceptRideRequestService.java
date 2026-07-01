package com.daimielcr.backend.application.service.ride_request;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

import org.springframework.transaction.annotation.Transactional;

import com.daimielcr.backend.application.port.in.ride_request.AcceptRideRequestCommand;
import com.daimielcr.backend.application.port.in.ride_request.AcceptRideRequestUseCase;
import com.daimielcr.backend.application.port.out.ride_request.RideRequestRepositoryPort;
import com.daimielcr.backend.application.port.out.trip.TripRepositoryPort;
import com.daimielcr.backend.domain.exceptions.RideRequestNotFoundException;
import com.daimielcr.backend.domain.exceptions.TripNotFoundException;
import com.daimielcr.backend.domain.exceptions.UnauthorizedTripActionException;
import com.daimielcr.backend.domain.model.ride_request.RideRequest;
import com.daimielcr.backend.domain.model.trip.SeatCount;
import com.daimielcr.backend.domain.model.trip.Trip;

public class AcceptRideRequestService implements AcceptRideRequestUseCase {

    private final RideRequestRepositoryPort rideRequestRepository;
    private final TripRepositoryPort tripRepository;
    private final Clock clock;

    public AcceptRideRequestService(
            RideRequestRepositoryPort rideRequestRepository,
            TripRepositoryPort tripRepository,
            Clock clock
    ) {
        this.rideRequestRepository = Objects.requireNonNull(
                rideRequestRepository,
                "El repositorio de solicitudes es obligatorio"
        );
        this.tripRepository = Objects.requireNonNull(
                tripRepository,
                "El repositorio de viajes es obligatorio"
        );
        this.clock = Objects.requireNonNull(
                clock,
                "El reloj es obligatorio"
        );
    }

    @Override
    @Transactional
    public void accept(AcceptRideRequestCommand command) {
        Objects.requireNonNull(command, "El comando es obligatorio");

        RideRequest rideRequest = rideRequestRepository
                .findById(command.rideRequestId())
                .orElseThrow(() -> new RideRequestNotFoundException(
                        "No existe la solicitud: %s"
                                .formatted(command.rideRequestId())
                ));

        Trip trip = tripRepository.findById(rideRequest.tripId())
                .orElseThrow(() -> new TripNotFoundException(
                        "No existe el viaje: %s"
                                .formatted(rideRequest.tripId())
                ));

        if (!trip.isOwnedBy(command.requesterId())) {
            throw new UnauthorizedTripActionException(
                    "Solo el conductor puede aceptar solicitudes de este viaje"
            );
        }

        Instant now = Instant.now(clock);

        rideRequest.accept(now);
        trip.reserveSeats(
            new SeatCount(rideRequest.requestedSeats().value()),
            now
        );

        tripRepository.save(trip);
        rideRequestRepository.save(rideRequest);
    }
}