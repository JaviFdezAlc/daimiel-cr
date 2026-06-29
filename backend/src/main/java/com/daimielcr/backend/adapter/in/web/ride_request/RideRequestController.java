package com.daimielcr.backend.adapter.in.web.ride_request;

import java.util.Objects;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.daimielcr.backend.application.port.in.ride_request.CreateRideRequestUseCase;
import com.daimielcr.backend.domain.model.ride_request.RideRequestId;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/trips/{tripId}/ride-requests")
public class RideRequestController {

    private final CreateRideRequestUseCase createRideRequestUseCase;

    public RideRequestController(
            CreateRideRequestUseCase createRideRequestUseCase
    ) {
        this.createRideRequestUseCase = Objects.requireNonNull(
                createRideRequestUseCase,
                "El caso de uso para crear solicitudes es obligatorio"
        );
    }

    @PostMapping
    public ResponseEntity<CreateRideRequestResponse> createRideRequest(
            @PathVariable UUID tripId,
            @RequestHeader("X-User-Id") UUID passengerId,
            @Valid @RequestBody CreateRideRequestRequest request
    ) {
        RideRequestId rideRequestId = createRideRequestUseCase.create(
                RideRequestWebMapper.toCommand(
                        tripId,
                        passengerId,
                        request
                )
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        RideRequestWebMapper.toResponse(rideRequestId)
                );
    }
}