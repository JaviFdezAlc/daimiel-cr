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
import com.daimielcr.backend.application.port.in.ride_request.CancelRideRequestCommand;
import com.daimielcr.backend.application.port.in.ride_request.CancelRideRequestUseCase;
import com.daimielcr.backend.application.port.in.ride_request.RejectRideRequestCommand;
import com.daimielcr.backend.application.port.in.ride_request.RejectRideRequestUseCase;
import com.daimielcr.backend.config.SecurityConfig;
import com.daimielcr.backend.domain.exceptions.InsufficientSeatsException;
import com.daimielcr.backend.domain.exceptions.InvalidRideRequestStateException;
import com.daimielcr.backend.domain.exceptions.RideRequestNotFoundException;
import com.daimielcr.backend.domain.exceptions.UnauthorizedRideRequestActionException;
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

        @MockitoBean
        private RejectRideRequestUseCase rejectRideRequestUseCase;

        @MockitoBean
        private CancelRideRequestUseCase cancelRideRequestUseCase;

        @Test
        void shouldAcceptRideRequest() throws Exception {
                UUID rideRequestUuid = UUID.fromString(
                                "11111111-1111-1111-1111-111111111111");

                UUID driverUuid = UUID.fromString(
                                "22222222-2222-2222-2222-222222222222");

                mockMvc.perform(post(
                                "/api/v1/ride-requests/{rideRequestId}/accept",
                                rideRequestUuid)
                                .header("X-User-Id", driverUuid))
                                .andExpect(status().isNoContent())
                                .andExpect(content().string(""));

                var commandCaptor = org.mockito.ArgumentCaptor.forClass(
                                AcceptRideRequestCommand.class);

                verify(acceptRideRequestUseCase).accept(commandCaptor.capture());

                AcceptRideRequestCommand command = commandCaptor.getValue();

                assertEquals(
                                new RideRequestId(rideRequestUuid),
                                command.rideRequestId());
                assertEquals(
                                new UserId(driverUuid),
                                command.requesterId());
        }

        @Test
        void shouldReturnNotFoundWhenRideRequestDoesNotExist() throws Exception {
                UUID rideRequestUuid = UUID.fromString(
                                "11111111-1111-1111-1111-111111111111");

                UUID driverUuid = UUID.fromString(
                                "22222222-2222-2222-2222-222222222222");

                doThrow(new RideRequestNotFoundException(
                                "No existe la solicitud: " + rideRequestUuid)).when(acceptRideRequestUseCase)
                                .accept(any(AcceptRideRequestCommand.class));

                mockMvc.perform(post(
                                "/api/v1/ride-requests/{rideRequestId}/accept",
                                rideRequestUuid)
                                .header("X-User-Id", driverUuid))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.code")
                                                .value("RIDE_REQUEST_NOT_FOUND"));
        }

        @Test
        void shouldReturnForbiddenWhenRequesterIsNotTripDriver()
                        throws Exception {

                UUID rideRequestUuid = UUID.fromString(
                                "11111111-1111-1111-1111-111111111111");

                UUID requesterUuid = UUID.fromString(
                                "33333333-3333-3333-3333-333333333333");

                doThrow(new UnauthorizedTripActionException(
                                "Solo el conductor puede aceptar solicitudes de este viaje"))
                                .when(acceptRideRequestUseCase)
                                .accept(any(AcceptRideRequestCommand.class));

                mockMvc.perform(post(
                                "/api/v1/ride-requests/{rideRequestId}/accept",
                                rideRequestUuid)
                                .header("X-User-Id", requesterUuid))
                                .andExpect(status().isForbidden())
                                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
        }

        @Test
        void shouldReturnConflictWhenRideRequestIsNotPending()
                        throws Exception {

                UUID rideRequestUuid = UUID.fromString(
                                "11111111-1111-1111-1111-111111111111");

                UUID driverUuid = UUID.fromString(
                                "22222222-2222-2222-2222-222222222222");

                doThrow(new InvalidRideRequestStateException(
                                "Solo se puede realizar esta acción sobre una solicitud pendiente"))
                                .when(acceptRideRequestUseCase)
                                .accept(any(AcceptRideRequestCommand.class));

                mockMvc.perform(post(
                                "/api/v1/ride-requests/{rideRequestId}/accept",
                                rideRequestUuid)
                                .header("X-User-Id", driverUuid))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.code")
                                                .value("RIDE_REQUEST_CONFLICT"));
        }

        @Test
        void shouldReturnConflictWhenThereAreNotEnoughSeats()
                        throws Exception {

                UUID rideRequestUuid = UUID.fromString(
                                "11111111-1111-1111-1111-111111111111");

                UUID driverUuid = UUID.fromString(
                                "22222222-2222-2222-2222-222222222222");

                doThrow(new InsufficientSeatsException(
                                "No hay suficientes plazas disponibles")).when(acceptRideRequestUseCase)
                                .accept(any(AcceptRideRequestCommand.class));

                mockMvc.perform(post(
                                "/api/v1/ride-requests/{rideRequestId}/accept",
                                rideRequestUuid)
                                .header("X-User-Id", driverUuid))
                                .andExpect(status().isConflict());
        }

        @Test
        void shouldRejectRideRequest() throws Exception {
                UUID rideRequestUuid = UUID.fromString(
                                "11111111-1111-1111-1111-111111111111");

                UUID driverUuid = UUID.fromString(
                                "22222222-2222-2222-2222-222222222222");

                mockMvc.perform(post(
                                "/api/v1/ride-requests/{rideRequestId}/reject",
                                rideRequestUuid)
                                .header("X-User-Id", driverUuid))
                                .andExpect(status().isNoContent())
                                .andExpect(content().string(""));

                var commandCaptor = org.mockito.ArgumentCaptor.forClass(
                                RejectRideRequestCommand.class);

                verify(rejectRideRequestUseCase).reject(commandCaptor.capture());

                RejectRideRequestCommand command = commandCaptor.getValue();

                assertEquals(
                                new RideRequestId(rideRequestUuid),
                                command.rideRequestId());
                assertEquals(
                                new UserId(driverUuid),
                                command.requesterId());
        }

        @Test
        void shouldReturnNotFoundWhenRejectingUnknownRideRequest()
                        throws Exception {

                UUID rideRequestUuid = UUID.fromString(
                                "11111111-1111-1111-1111-111111111111");

                UUID driverUuid = UUID.fromString(
                                "22222222-2222-2222-2222-222222222222");

                doThrow(new RideRequestNotFoundException(
                                "No existe la solicitud: " + rideRequestUuid)).when(rejectRideRequestUseCase)
                                .reject(any(RejectRideRequestCommand.class));

                mockMvc.perform(post(
                                "/api/v1/ride-requests/{rideRequestId}/reject",
                                rideRequestUuid)
                                .header("X-User-Id", driverUuid))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.code")
                                                .value("RIDE_REQUEST_NOT_FOUND"));
        }

        @Test
        void shouldReturnForbiddenWhenRequesterIsNotTripDriverOnReject()
                        throws Exception {

                UUID rideRequestUuid = UUID.fromString(
                                "11111111-1111-1111-1111-111111111111");

                UUID requesterUuid = UUID.fromString(
                                "33333333-3333-3333-3333-333333333333");

                doThrow(new UnauthorizedTripActionException(
                                "Solo el conductor puede rechazar solicitudes de este viaje"))
                                .when(rejectRideRequestUseCase)
                                .reject(any(RejectRideRequestCommand.class));

                mockMvc.perform(post(
                                "/api/v1/ride-requests/{rideRequestId}/reject",
                                rideRequestUuid)
                                .header("X-User-Id", requesterUuid))
                                .andExpect(status().isForbidden())
                                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
        }

        @Test
        void shouldReturnConflictWhenRejectingNonPendingRideRequest()
                        throws Exception {

                UUID rideRequestUuid = UUID.fromString(
                                "11111111-1111-1111-1111-111111111111");

                UUID driverUuid = UUID.fromString(
                                "22222222-2222-2222-2222-222222222222");

                doThrow(new InvalidRideRequestStateException(
                                "Solo se puede realizar esta acción sobre una solicitud pendiente"))
                                .when(rejectRideRequestUseCase)
                                .reject(any(RejectRideRequestCommand.class));

                mockMvc.perform(post(
                                "/api/v1/ride-requests/{rideRequestId}/reject",
                                rideRequestUuid)
                                .header("X-User-Id", driverUuid))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.code")
                                                .value("RIDE_REQUEST_CONFLICT"));
        }

        @Test
        void shouldCancelRideRequest() throws Exception {
                UUID rideRequestUuid = UUID.fromString(
                                "11111111-1111-1111-1111-111111111111");

                UUID passengerUuid = UUID.fromString(
                                "44444444-4444-4444-4444-444444444444");

                mockMvc.perform(post(
                                "/api/v1/ride-requests/{rideRequestId}/cancel",
                                rideRequestUuid)
                                .header("X-User-Id", passengerUuid))
                                .andExpect(status().isNoContent())
                                .andExpect(content().string(""));

                var commandCaptor = org.mockito.ArgumentCaptor.forClass(
                                CancelRideRequestCommand.class);

                verify(cancelRideRequestUseCase).cancel(commandCaptor.capture());

                CancelRideRequestCommand command = commandCaptor.getValue();

                assertEquals(
                                new RideRequestId(rideRequestUuid),
                                command.rideRequestId());
                assertEquals(
                                new UserId(passengerUuid),
                                command.requesterId());
        }

        @Test
        void shouldReturnForbiddenWhenRequesterIsNotPassengerOnCancel()
                        throws Exception {

                UUID rideRequestUuid = UUID.fromString(
                                "11111111-1111-1111-1111-111111111111");

                UUID otherUserUuid = UUID.fromString(
                                "55555555-5555-5555-5555-555555555555");

                doThrow(new UnauthorizedRideRequestActionException(
                                "Solo el pasajero puede cancelar esta solicitud")).when(cancelRideRequestUseCase)
                                .cancel(any(CancelRideRequestCommand.class));

                mockMvc.perform(post(
                                "/api/v1/ride-requests/{rideRequestId}/cancel",
                                rideRequestUuid)
                                .header("X-User-Id", otherUserUuid))
                                .andExpect(status().isForbidden())
                                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
        }

        @Test
        void shouldReturnConflictWhenCancellingResolvedRideRequest()
                        throws Exception {

                UUID rideRequestUuid = UUID.fromString(
                                "11111111-1111-1111-1111-111111111111");

                UUID passengerUuid = UUID.fromString(
                                "44444444-4444-4444-4444-444444444444");

                doThrow(new InvalidRideRequestStateException(
                                "La solicitud no puede cancelarse en su estado actual")).when(cancelRideRequestUseCase)
                                .cancel(any(CancelRideRequestCommand.class));

                mockMvc.perform(post(
                                "/api/v1/ride-requests/{rideRequestId}/cancel",
                                rideRequestUuid)
                                .header("X-User-Id", passengerUuid))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.code")
                                                .value("RIDE_REQUEST_CONFLICT"));
        }
}