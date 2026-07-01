package com.daimielcr.backend.adapter.in.web.ride_request;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.daimielcr.backend.application.port.in.ride_request.CreateRideRequestUseCase;
import com.daimielcr.backend.application.port.in.ride_request.GetTripRideRequestsQuery;
import com.daimielcr.backend.application.port.in.ride_request.GetTripRideRequestsUseCase;
import com.daimielcr.backend.application.port.in.ride_request.RideRequestSummary;
import com.daimielcr.backend.config.SecurityConfig;
import com.daimielcr.backend.domain.exceptions.TripNotFoundException;
import com.daimielcr.backend.domain.exceptions.UnauthorizedTripActionException;
import com.daimielcr.backend.domain.model.ride_request.RideRequestId;
import com.daimielcr.backend.domain.model.ride_request.RideRequestStatus;
import com.daimielcr.backend.domain.model.trip.TripId;
import com.daimielcr.backend.domain.model.user.UserId;

@WebMvcTest(controllers = RideRequestController.class)
@Import(SecurityConfig.class)
class RideRequestControllerTest {

    private static final UUID TRIP_UUID = UUID.fromString(
            "11111111-1111-1111-1111-111111111111"
    );

    private static final UUID DRIVER_UUID = UUID.fromString(
            "22222222-2222-2222-2222-222222222222"
    );

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreateRideRequestUseCase createRideRequestUseCase;

    @MockitoBean
    private GetTripRideRequestsUseCase getTripRideRequestsUseCase;

    @Test
    void shouldReturnRideRequestsForTripDriver() throws Exception {
        RideRequestSummary firstRequest = summary(
                "33333333-3333-3333-3333-333333333333",
                "44444444-4444-4444-4444-444444444444",
                1,
                "¿Te queda sitio?",
                RideRequestStatus.PENDING
        );

        RideRequestSummary secondRequest = summary(
                "55555555-5555-5555-5555-555555555555",
                "66666666-6666-6666-6666-666666666666",
                2,
                "Viajo con un acompañante",
                RideRequestStatus.ACCEPTED
        );

        when(getTripRideRequestsUseCase.getForTrip(
                any(GetTripRideRequestsQuery.class)
        )).thenReturn(List.of(firstRequest, secondRequest));

        mockMvc.perform(get(
                        "/api/v1/trips/{tripId}/ride-requests",
                        TRIP_UUID
                )
                        .header("X-User-Id", DRIVER_UUID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id")
                        .value("33333333-3333-3333-3333-333333333333"))
                .andExpect(jsonPath("$[0].passengerId")
                        .value("44444444-4444-4444-4444-444444444444"))
                .andExpect(jsonPath("$[0].requestedSeats").value(1))
                .andExpect(jsonPath("$[0].message")
                        .value("¿Te queda sitio?"))
                .andExpect(jsonPath("$[0].status").value("PENDING"))
                .andExpect(jsonPath("$[1].requestedSeats").value(2))
                .andExpect(jsonPath("$[1].status").value("ACCEPTED"));

        var captor = org.mockito.ArgumentCaptor.forClass(
                GetTripRideRequestsQuery.class
        );

        verify(getTripRideRequestsUseCase).getForTrip(captor.capture());

        GetTripRideRequestsQuery query = captor.getValue();

        assertEquals(new TripId(TRIP_UUID), query.tripId());
        assertEquals(new UserId(DRIVER_UUID), query.requesterId());
    }

    @Test
    void shouldReturnEmptyListWhenTripHasNoRideRequests() throws Exception {
        when(getTripRideRequestsUseCase.getForTrip(
                any(GetTripRideRequestsQuery.class)
        )).thenReturn(List.of());

        mockMvc.perform(get(
                        "/api/v1/trips/{tripId}/ride-requests",
                        TRIP_UUID
                )
                        .header("X-User-Id", DRIVER_UUID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldReturnNotFoundWhenTripDoesNotExist() throws Exception {
        doThrow(new TripNotFoundException(
                "No existe el viaje: " + TRIP_UUID
        )).when(getTripRideRequestsUseCase)
                .getForTrip(any(GetTripRideRequestsQuery.class));

        mockMvc.perform(get(
                        "/api/v1/trips/{tripId}/ride-requests",
                        TRIP_UUID
                )
                        .header("X-User-Id", DRIVER_UUID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("TRIP_NOT_FOUND"));
    }

    @Test
    void shouldReturnForbiddenWhenRequesterIsNotTripDriver()
            throws Exception {

        UUID otherUserUuid = UUID.fromString(
                "77777777-7777-7777-7777-777777777777"
        );

        doThrow(new UnauthorizedTripActionException(
                "Solo el conductor puede ver las solicitudes de este viaje"
        )).when(getTripRideRequestsUseCase)
                .getForTrip(any(GetTripRideRequestsQuery.class));

        mockMvc.perform(get(
                        "/api/v1/trips/{tripId}/ride-requests",
                        TRIP_UUID
                )
                        .header("X-User-Id", otherUserUuid))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    private RideRequestSummary summary(
            String rideRequestId,
            String passengerId,
            int requestedSeats,
            String message,
            RideRequestStatus status
    ) {
        Instant createdAt = Instant.parse("2026-07-01T12:00:00Z");

        return new RideRequestSummary(
                new RideRequestId(UUID.fromString(rideRequestId)),
                new TripId(TRIP_UUID),
                new UserId(UUID.fromString(passengerId)),
                requestedSeats,
                message,
                status,
                createdAt,
                createdAt
        );
    }
}