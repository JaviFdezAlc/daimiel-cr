package com.daimielcr.backend.adapter.in.web.trip;

import java.net.URI;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.daimielcr.backend.application.port.in.trip.CreateTripUseCase;
import com.daimielcr.backend.application.port.in.trip.GetTripDetailUseCase;
import com.daimielcr.backend.application.port.in.trip.SearchTripsQuery;
import com.daimielcr.backend.application.port.in.trip.SearchTripsResult;
import com.daimielcr.backend.application.port.in.trip.SearchTripsUseCase;
import com.daimielcr.backend.application.port.in.trip.TripDetail;
import com.daimielcr.backend.application.port.in.trip.TripSort;
import com.daimielcr.backend.application.port.in.trip.UpdateTripUseCase;
import com.daimielcr.backend.domain.model.trip.TripId;
import com.daimielcr.backend.domain.model.trip.TripLocation;
import com.daimielcr.backend.domain.model.user.UserId;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/trips")
public class TripController {

        private final CreateTripUseCase createTripUseCase;
        private final GetTripDetailUseCase getTripDetailUseCase;
        private final SearchTripsUseCase searchTripsUseCase;
        private final UpdateTripUseCase updateTripUseCase;

        public TripController(
                        CreateTripUseCase createTripUseCase,
                        GetTripDetailUseCase getTripDetailUseCase,
                        SearchTripsUseCase searchTripsUseCase,
                        UpdateTripUseCase updateTripUseCase) {
                this.createTripUseCase = Objects.requireNonNull(
                                createTripUseCase,
                                "El caso de uso para crear viajes es obligatorio");
                this.getTripDetailUseCase = Objects.requireNonNull(
                                getTripDetailUseCase,
                                "El caso de uso para obtener el detalle del viaje es obligatorio");
                this.searchTripsUseCase = Objects.requireNonNull(
                                searchTripsUseCase,
                                "El caso de uso para buscar viajes es obligatorio");
                this.updateTripUseCase = Objects.requireNonNull(
                                updateTripUseCase,
                                "El caso de uso para actualizar viajes es obligatorio");
        }

        @PostMapping
        public ResponseEntity<CreateTripResponse> createTrip(
                        @RequestHeader(name = "X-User-Id") UUID userId,
                        @Valid @RequestBody CreateTripRequest request) {
                TripId tripId = createTripUseCase.create(
                                TripWebMapper.toCommand(
                                                request,
                                                new UserId(userId)));

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

        @GetMapping("{tripId}")
        public ResponseEntity<TripDetailResponse> getTripById(@PathVariable UUID tripId) {
                TripDetail detail = getTripDetailUseCase.getById(
                                new TripId(tripId));

                return ResponseEntity.ok(
                                TripWebMapper.toResponse(detail));
        }

        @GetMapping
        public ResponseEntity<SearchTripsResponse> searchTrips(
                        @RequestParam(name = "origin") TripLocation origin,
                        @RequestParam(name = "destination") TripLocation destination,
                        @RequestParam(name = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                        @RequestParam(name = "requiredSeats", defaultValue = "1") int requiredSeats,
                        @RequestParam(name = "sort", defaultValue = "DEPARTURE_ASC") TripSort sort,
                        @RequestParam(name = "page", defaultValue = "0") int page,
                        @RequestParam(name = "size", defaultValue = "20") int size) {
                SearchTripsResult result = searchTripsUseCase.search(
                                new SearchTripsQuery(
                                                origin,
                                                destination,
                                                date,
                                                requiredSeats,
                                                sort,
                                                page,
                                                size));

                return ResponseEntity.ok(
                                TripWebMapper.toResponse(result));
        }

        @PutMapping("/{tripId}")
        public ResponseEntity<TripDetailResponse> updateTrip(
                        @PathVariable UUID tripId,
                        @RequestHeader("X-User-Id") UUID requesterId,
                        @Valid @RequestBody UpdateTripRequest request) {
                TripDetail updatedTrip = updateTripUseCase.update(
                                TripWebMapper.toCommand(
                                                tripId,
                                                requesterId,
                                                request));

                return ResponseEntity.ok(
                                TripWebMapper.toResponse(updatedTrip));
        }
}