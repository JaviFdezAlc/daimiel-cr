package com.daimielcr.backend.application.service.ride_request;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

import org.springframework.transaction.annotation.Transactional;

import com.daimielcr.backend.application.port.in.ride_request.CancelRideRequestCommand;
import com.daimielcr.backend.application.port.in.ride_request.CancelRideRequestUseCase;
import com.daimielcr.backend.application.port.out.ride_request.RideRequestRepositoryPort;
import com.daimielcr.backend.application.port.out.trip.TripRepositoryPort;
import com.daimielcr.backend.domain.exceptions.RideRequestNotFoundException;
import com.daimielcr.backend.domain.exceptions.TripNotFoundException;
import com.daimielcr.backend.domain.model.ride_request.RideRequest;
import com.daimielcr.backend.domain.model.trip.SeatCount;
import com.daimielcr.backend.domain.model.trip.Trip;

public class CancelRideRequestService implements CancelRideRequestUseCase {

    private final RideRequestRepositoryPort rideRequestRepository;
    private final TripRepositoryPort tripRepository;
    private final Clock clock;

    public CancelRideRequestService(
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
    public void cancel(CancelRideRequestCommand command) {
        Objects.requireNonNull(command, "El comando es obligatorio");

        RideRequest rideRequest = rideRequestRepository
                .findById(command.rideRequestId())
                .orElseThrow(() -> new RideRequestNotFoundException(
                        "No existe la solicitud: %s"
                                .formatted(command.rideRequestId())
                ));

        boolean mustReleaseSeats =
                rideRequest.requiresSeatReleaseWhenCancelled();

        Trip trip = null;

        if (mustReleaseSeats) {
            trip = tripRepository.findById(rideRequest.tripId())
                    .orElseThrow(() -> new TripNotFoundException(
                            "No existe el viaje: %s"
                                    .formatted(rideRequest.tripId())
                    ));
        }

        Instant now = Instant.now(clock);

        rideRequest.cancel(command.requesterId(), now);

        if (mustReleaseSeats) {
            trip.releaseSeats(
                    new SeatCount(rideRequest.requestedSeats().value()),
                    now
            );

            tripRepository.save(trip);
        }

        rideRequestRepository.save(rideRequest);
    }
}