package com.daimielcr.backend.adapter.in.web.ride_request;

import java.util.Objects;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.daimielcr.backend.application.port.in.ride_request.AcceptRideRequestUseCase;

@RestController
@RequestMapping("/api/v1/ride-requests")
public class RideRequestActionController {

    private final AcceptRideRequestUseCase acceptRideRequestUseCase;

    public RideRequestActionController(
            AcceptRideRequestUseCase acceptRideRequestUseCase
    ) {
        this.acceptRideRequestUseCase = Objects.requireNonNull(
                acceptRideRequestUseCase,
                "El caso de uso para aceptar solicitudes es obligatorio"
        );
    }

    @PostMapping("/{rideRequestId}/accept")
    public ResponseEntity<Void> acceptRideRequest(
            @PathVariable UUID rideRequestId,
            @RequestHeader("X-User-Id") UUID requesterId
    ) {
        acceptRideRequestUseCase.accept(
                RideRequestWebMapper.toAcceptCommand(
                        rideRequestId,
                        requesterId
                )
        );

        return ResponseEntity.noContent().build();
    }
}