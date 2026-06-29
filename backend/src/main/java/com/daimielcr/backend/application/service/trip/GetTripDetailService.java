package com.daimielcr.backend.application.service.trip;

import java.util.Objects;

import com.daimielcr.backend.application.port.in.trip.GetTripDetailUseCase;
import com.daimielcr.backend.application.port.in.trip.TripDetail;
import com.daimielcr.backend.application.port.out.trip.TripRepositoryPort;
import com.daimielcr.backend.domain.exceptions.TripNotFoundException;
import com.daimielcr.backend.domain.model.trip.TripId;

public class GetTripDetailService implements GetTripDetailUseCase {

    private final TripRepositoryPort tripRepository;

    public GetTripDetailService(TripRepositoryPort tripRepository) {
        this.tripRepository = Objects.requireNonNull(
                tripRepository,
                "El repositorio de viajes es obligatorio"
        );
    }

    @Override
    public TripDetail getById(TripId tripId) {
        Objects.requireNonNull(tripId, "El id del viaje es obligatorio");

        return tripRepository.findById(tripId)
                .map(TripDetail::from)
                .orElseThrow(() -> new TripNotFoundException(
                        "No existe el viaje: %s".formatted(tripId)
                ));
    }
}