package com.daimielcr.backend.application.service.ride_request;

import java.util.List;
import java.util.Objects;

import com.daimielcr.backend.application.port.in.ride_request.GetTripRideRequestsQuery;
import com.daimielcr.backend.application.port.in.ride_request.GetTripRideRequestsUseCase;
import com.daimielcr.backend.application.port.in.ride_request.RideRequestSummary;
import com.daimielcr.backend.application.port.out.ride_request.RideRequestRepositoryPort;
import com.daimielcr.backend.application.port.out.trip.TripRepositoryPort;
import com.daimielcr.backend.domain.exceptions.TripNotFoundException;
import com.daimielcr.backend.domain.exceptions.UnauthorizedTripActionException;
import com.daimielcr.backend.domain.model.trip.Trip;

public class GetTripRideRequestsService
        implements GetTripRideRequestsUseCase {

    private final RideRequestRepositoryPort rideRequestRepository;
    private final TripRepositoryPort tripRepository;

    public GetTripRideRequestsService(
            RideRequestRepositoryPort rideRequestRepository,
            TripRepositoryPort tripRepository
    ) {
        this.rideRequestRepository = Objects.requireNonNull(
                rideRequestRepository,
                "El repositorio de solicitudes es obligatorio"
        );
        this.tripRepository = Objects.requireNonNull(
                tripRepository,
                "El repositorio de viajes es obligatorio"
        );
    }

    @Override
    public List<RideRequestSummary> getForTrip(
            GetTripRideRequestsQuery query
    ) {
        Objects.requireNonNull(query, "La consulta es obligatoria");

        Trip trip = tripRepository.findById(query.tripId())
                .orElseThrow(() -> new TripNotFoundException(
                        "No existe el viaje: %s"
                                .formatted(query.tripId())
                ));

        if (!trip.isOwnedBy(query.requesterId())) {
            throw new UnauthorizedTripActionException(
                    "Solo el conductor puede ver las solicitudes de este viaje"
            );
        }

        return rideRequestRepository
                .findAllByTripId(query.tripId())
                .stream()
                .map(RideRequestSummary::from)
                .toList();
    }
}