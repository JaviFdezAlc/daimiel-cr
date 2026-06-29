package com.daimielcr.backend.application.service.trip;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

import com.daimielcr.backend.application.port.in.trip.TripDetail;
import com.daimielcr.backend.application.port.in.trip.UpdateTripCommand;
import com.daimielcr.backend.application.port.in.trip.UpdateTripUseCase;
import com.daimielcr.backend.application.port.out.trip.TripRepositoryPort;
import com.daimielcr.backend.domain.exceptions.TripNotFoundException;
import com.daimielcr.backend.domain.model.trip.ContributionAmount;
import com.daimielcr.backend.domain.model.trip.DepartureAt;
import com.daimielcr.backend.domain.model.trip.Route;
import com.daimielcr.backend.domain.model.trip.SeatCount;
import com.daimielcr.backend.domain.model.trip.Trip;

public class UpdateTripService implements UpdateTripUseCase {

    private final TripRepositoryPort tripRepository;
    private final Clock clock;

    public UpdateTripService(
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
    public TripDetail update(UpdateTripCommand command) {
        Objects.requireNonNull(command, "El comando es obligatorio");

        Trip trip = tripRepository.findById(command.tripId())
                .orElseThrow(() -> new TripNotFoundException(
                        "No existe el viaje: %s".formatted(command.tripId())
                ));

        Instant now = Instant.now(clock);

        trip.updateDetails(
                command.requesterId(),
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

        return TripDetail.from(trip);
    }
}
