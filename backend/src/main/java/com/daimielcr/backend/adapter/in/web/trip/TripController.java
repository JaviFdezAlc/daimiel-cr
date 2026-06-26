package com.daimielcr.backend.adapter.in.web.trip;

import java.net.URI;
import java.util.Objects;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.daimielcr.backend.application.port.in.trip.CreateTripUseCase;
import com.daimielcr.backend.domain.model.trip.TripId;
import com.daimielcr.backend.domain.model.user.UserId;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/trips")
public class TripController {

    private final CreateTripUseCase createTripUseCase;

    public TripController(CreateTripUseCase createTripUseCase) {
        this.createTripUseCase = Objects.requireNonNull(
                createTripUseCase,
                "El caso de uso para crear viajes es obligatorio"
        );
    }

    @PostMapping
    public ResponseEntity<CreateTripResponse> createTrip(
            @RequestHeader(name = "X-User-Id") UUID userId,
            @Valid @RequestBody CreateTripRequest request
    ) {
        TripId tripId = createTripUseCase.create(
                TripWebMapper.toCommand(
                        request,
                        new UserId(userId)
                )
        );

        CreateTripResponse response = TripWebMapper.toResponse(tripId);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequestUri()
                .path("/{tripId}")
                .buildAndExpand(tripId.value())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(response);
    }
}