package com.daimielcr.backend.application.service.trip;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

import com.daimielcr.backend.application.port.in.trip.CancelTripCommand;
import com.daimielcr.backend.application.port.in.trip.CancelTripUseCase;
import com.daimielcr.backend.application.port.out.trip.TripRepositoryPort;
import com.daimielcr.backend.domain.exceptions.TripNotFoundException;
import com.daimielcr.backend.domain.model.trip.Trip;

public class CancelTripService implements CancelTripUseCase {

    private final TripRepositoryPort tripRepository;
    private final Clock clock;

    public CancelTripService(
            TripRepositoryPort tripRepository,
            Clock clock
    ) {
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
    public void cancel(CancelTripCommand command) {
        Objects.requireNonNull(command, "El comando es obligatorio");

        Trip trip = tripRepository.findById(command.tripId())
                .orElseThrow(() -> new TripNotFoundException(
                        "No existe el viaje: %s".formatted(command.tripId())
                ));

        trip.cancel(
                command.requesterId(),
                Instant.now(clock)
        );

        tripRepository.save(trip);
    }
}