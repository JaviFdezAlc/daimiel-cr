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
import com.daimielcr.backend.application.port.in.ride_request.CancelRideRequestUseCase;
import com.daimielcr.backend.application.port.in.ride_request.RejectRideRequestUseCase;

@RestController
@RequestMapping("/api/v1/ride-requests")
public class RideRequestActionController {

        private final AcceptRideRequestUseCase acceptRideRequestUseCase;
        private final RejectRideRequestUseCase rejectRideRequestUseCase;
        private final CancelRideRequestUseCase cancelRideRequestUseCase;

        public RideRequestActionController(
                        AcceptRideRequestUseCase acceptRideRequestUseCase,
                        RejectRideRequestUseCase rejectRideRequestUseCase,
                        CancelRideRequestUseCase cancelRideRequestUseCase) {
                this.acceptRideRequestUseCase = Objects.requireNonNull(
                                acceptRideRequestUseCase,
                                "El caso de uso para aceptar solicitudes es obligatorio");
                this.rejectRideRequestUseCase = Objects.requireNonNull(
                                rejectRideRequestUseCase,
                                "El caso de uso para rechazar solicitudes es obligatorio");
                this.cancelRideRequestUseCase = Objects.requireNonNull(
                                cancelRideRequestUseCase,
                                "El caso de uso para cancelar solicitudes es obligatorio");
        }

        @PostMapping("/{rideRequestId}/accept")
        public ResponseEntity<Void> acceptRideRequest(
                        @PathVariable UUID rideRequestId,
                        @RequestHeader("X-User-Id") UUID requesterId) {
                acceptRideRequestUseCase.accept(
                                RideRequestWebMapper.toAcceptCommand(
                                                rideRequestId,
                                                requesterId));

                return ResponseEntity.noContent().build();
        }

        @PostMapping("/{rideRequestId}/reject")
        public ResponseEntity<Void> rejectRideRequest(
                        @PathVariable UUID rideRequestId,
                        @RequestHeader("X-User-Id") UUID requesterId) {
                rejectRideRequestUseCase.reject(
                                RideRequestWebMapper.toRejectCommand(
                                                rideRequestId,
                                                requesterId));

                return ResponseEntity.noContent().build();
        }

        @PostMapping("/{rideRequestId}/cancel")
        public ResponseEntity<Void> cancelRideRequest(
                        @PathVariable UUID rideRequestId,
                        @RequestHeader("X-User-Id") UUID requesterId) {
                cancelRideRequestUseCase.cancel(
                                RideRequestWebMapper.toCancelCommand(
                                                rideRequestId,
                                                requesterId));

                return ResponseEntity.noContent().build();
        }
}