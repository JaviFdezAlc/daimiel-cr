package com.daimielcr.backend.adapter.in.web.ride_request;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.daimielcr.backend.application.port.in.ride_request.AcceptRideRequestCommand;
import com.daimielcr.backend.application.port.in.ride_request.AcceptRideRequestUseCase;
import com.daimielcr.backend.config.SecurityConfig;
import com.daimielcr.backend.domain.exceptions.InsufficientSeatsException;
import com.daimielcr.backend.domain.exceptions.InvalidRideRequestStateException;
import com.daimielcr.backend.domain.exceptions.RideRequestNotFoundException;
import com.daimielcr.backend.domain.exceptions.UnauthorizedTripActionException;
import com.daimielcr.backend.domain.model.ride_request.RideRequestId;
import com.daimielcr.backend.domain.model.user.UserId;

@WebMvcTest(controllers = RideRequestActionController.class)
@Import(SecurityConfig.class)
class RideRequestActionControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AcceptRideRequestUseCase acceptRideRequestUseCase;

    @Test
    void shouldAcceptRideRequest() throws Exception {
        UUID rideRequestUuid = UUID.fromString(
                "11111111-1111-1111-1111-111111111111"
        );

        UUID driverUuid = UUID.fromString(
                "22222222-2222-2222-2222-222222222222"
        );

        mockMvc.perform(post(
                        "/api/v1/ride-requests/{rideRequestId}/accept",
                        rideRequestUuid
                )
                        .header("X-User-Id", driverUuid))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        var commandCaptor = org.mockito.ArgumentCaptor.forClass(
                AcceptRideRequestCommand.class
        );

        verify(acceptRideRequestUseCase).accept(commandCaptor.capture());

        AcceptRideRequestCommand command = commandCaptor.getValue();

        assertEquals(
                new RideRequestId(rideRequestUuid),
                command.rideRequestId()
        );
        assertEquals(
                new UserId(driverUuid),
                command.requesterId()
        );
    }

    @Test
    void shouldReturnNotFoundWhenRideRequestDoesNotExist() throws Exception {
        UUID rideRequestUuid = UUID.fromString(
                "11111111-1111-1111-1111-111111111111"
        );

        UUID driverUuid = UUID.fromString(
                "22222222-2222-2222-2222-222222222222"
        );

        doThrow(new RideRequestNotFoundException(
                "No existe la solicitud: " + rideRequestUuid
        )).when(acceptRideRequestUseCase)
                .accept(any(AcceptRideRequestCommand.class));

        mockMvc.perform(post(
                        "/api/v1/ride-requests/{rideRequestId}/accept",
                        rideRequestUuid
                )
                        .header("X-User-Id", driverUuid))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code")
                        .value("RIDE_REQUEST_NOT_FOUND"));
    }

    @Test
    void shouldReturnForbiddenWhenRequesterIsNotTripDriver()
            throws Exception {

        UUID rideRequestUuid = UUID.fromString(
                "11111111-1111-1111-1111-111111111111"
        );

        UUID requesterUuid = UUID.fromString(
                "33333333-3333-3333-3333-333333333333"
        );

        doThrow(new UnauthorizedTripActionException(
                "Solo el conductor puede aceptar solicitudes de este viaje"
        )).when(acceptRideRequestUseCase)
                .accept(any(AcceptRideRequestCommand.class));

        mockMvc.perform(post(
                        "/api/v1/ride-requests/{rideRequestId}/accept",
                        rideRequestUuid
                )
                        .header("X-User-Id", requesterUuid))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void shouldReturnConflictWhenRideRequestIsNotPending()
            throws Exception {

        UUID rideRequestUuid = UUID.fromString(
                "11111111-1111-1111-1111-111111111111"
        );

        UUID driverUuid = UUID.fromString(
                "22222222-2222-2222-2222-222222222222"
        );

        doThrow(new InvalidRideRequestStateException(
                "Solo se puede realizar esta acción sobre una solicitud pendiente"
        )).when(acceptRideRequestUseCase)
                .accept(any(AcceptRideRequestCommand.class));

        mockMvc.perform(post(
                        "/api/v1/ride-requests/{rideRequestId}/accept",
                        rideRequestUuid
                )
                        .header("X-User-Id", driverUuid))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code")
                        .value("RIDE_REQUEST_CONFLICT"));
    }

    @Test
    void shouldReturnConflictWhenThereAreNotEnoughSeats()
            throws Exception {

        UUID rideRequestUuid = UUID.fromString(
                "11111111-1111-1111-1111-111111111111"
        );

        UUID driverUuid = UUID.fromString(
                "22222222-2222-2222-2222-222222222222"
        );

        doThrow(new InsufficientSeatsException(
                "No hay suficientes plazas disponibles"
        )).when(acceptRideRequestUseCase)
                .accept(any(AcceptRideRequestCommand.class));

        mockMvc.perform(post(
                        "/api/v1/ride-requests/{rideRequestId}/accept",
                        rideRequestUuid
                )
                        .header("X-User-Id", driverUuid))
                .andExpect(status().isConflict());
    }
}