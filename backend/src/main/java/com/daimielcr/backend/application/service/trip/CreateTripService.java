package com.daimielcr.backend.application.service.trip;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

import com.daimielcr.backend.application.port.in.trip.CreateTripCommand;
import com.daimielcr.backend.application.port.in.trip.CreateTripUseCase;
import com.daimielcr.backend.application.port.out.trip.TripRepositoryPort;
import com.daimielcr.backend.application.port.out.user.UserRepositoryPort;
import com.daimielcr.backend.domain.exceptions.UserNotFoundException;
import com.daimielcr.backend.domain.exceptions.UserPhoneNotVerifiedException;
import com.daimielcr.backend.domain.model.trip.ContributionAmount;
import com.daimielcr.backend.domain.model.trip.DepartureAt;
import com.daimielcr.backend.domain.model.trip.Route;
import com.daimielcr.backend.domain.model.trip.SeatCount;
import com.daimielcr.backend.domain.model.trip.Trip;
import com.daimielcr.backend.domain.model.trip.TripId;

public class CreateTripService implements CreateTripUseCase {

    private final UserRepositoryPort userRepository;
    private final TripRepositoryPort tripRepository;
    private final Clock clock;

    public CreateTripService(
            UserRepositoryPort userRepository,
            TripRepositoryPort tripRepository,
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
        this.clock = Objects.requireNonNull(
                clock,
                "El reloj es obligatorio"
        );
    }

    @Override
    public TripId create(CreateTripCommand command) {
        Objects.requireNonNull(command, "El comando es obligatorio");

        var driver = userRepository.findById(command.driverId())
                .orElseThrow(() -> new UserNotFoundException(
                        "No existe el usuario conductor: %s"
                                .formatted(command.driverId())
                ));

        if (!driver.isPhoneVerified()) {
            throw new UserPhoneNotVerifiedException(
                    "Debes verificar tu teléfono antes de publicar un viaje"
            );
        }

        Instant now = Instant.now(clock);

        Trip trip = Trip.create(
                TripId.newId(),
                driver.id(),
                new Route(command.origin(), command.destination()),
                new DepartureAt(command.departureAt()),
                new SeatCount(command.totalSeats()),
                new ContributionAmount(command.contributionAmount()),
                command.departurePoint(),
                command.arrivalPoint(),
                command.comment(),
                now
        );

        tripRepository.save(trip);

        return trip.id();
    }
}
