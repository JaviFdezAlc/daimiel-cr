package com.daimielcr.backend.adapter.in.web.trip;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.daimielcr.backend.application.port.in.trip.CreateTripCommand;
import com.daimielcr.backend.application.port.in.trip.CreateTripUseCase;
import com.daimielcr.backend.config.SecurityConfig;
import com.daimielcr.backend.domain.exceptions.InvalidTripException;
import com.daimielcr.backend.domain.exceptions.UserNotFoundException;
import com.daimielcr.backend.domain.exceptions.UserPhoneNotVerifiedException;
import com.daimielcr.backend.domain.model.trip.TripId;
import com.daimielcr.backend.domain.model.trip.TripLocation;
import com.daimielcr.backend.domain.model.user.UserId;

@WebMvcTest(controllers = TripController.class)
@Import(SecurityConfig.class)
class TripControllerTest {

    private static final UserId DRIVER_ID =
            new UserId(UUID.fromString("11111111-1111-1111-1111-111111111111"));

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreateTripUseCase createTripUseCase;

    @Test
    void shouldCreateTripAndReturnCreatedResponse() throws Exception {
        UUID createdTripUuid =
                UUID.fromString("22222222-2222-2222-2222-222222222222");

        TripId createdTripId = new TripId(createdTripUuid);

        Instant departureAt = Instant.now().plus(2, ChronoUnit.DAYS);

        when(createTripUseCase.create(any(CreateTripCommand.class)))
                .thenReturn(createdTripId);

        mockMvc.perform(post("/api/v1/trips")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", DRIVER_ID.value())
                        .content(validRequest(departureAt, "3.00")))
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        "Location",
                        containsString("/api/v1/trips/" + createdTripUuid)
                ))
                .andExpect(jsonPath("$.tripId").value(createdTripUuid.toString()));

        ArgumentCaptor<CreateTripCommand> commandCaptor =
                ArgumentCaptor.forClass(CreateTripCommand.class);

        verify(createTripUseCase).create(commandCaptor.capture());

        CreateTripCommand command = commandCaptor.getValue();

        assertEquals(DRIVER_ID, command.driverId());
        assertEquals(TripLocation.DAIMIEL, command.origin());
        assertEquals(TripLocation.CIUDAD_REAL, command.destination());
        assertEquals(departureAt, command.departureAt());
        assertEquals(3, command.totalSeats());
        assertEquals(
                0,
                new BigDecimal("3.00")
                        .compareTo(command.contributionAmount())
        );
        assertEquals("Plaza de España, Daimiel", command.departurePoint());
        assertEquals("Estación de autobuses, Ciudad Real", command.arrivalPoint());
        assertEquals("Salgo puntual", command.comment());
    }

    @Test
    void shouldUseZeroContributionWhenItIsOmitted() throws Exception {
        Instant departureAt = Instant.now().plus(2, ChronoUnit.DAYS);

        when(createTripUseCase.create(any(CreateTripCommand.class)))
                .thenReturn(TripId.newId());

        mockMvc.perform(post("/api/v1/trips")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", DRIVER_ID.value())
                        .content(requestWithoutContribution(departureAt)))
                .andExpect(status().isCreated());

        ArgumentCaptor<CreateTripCommand> commandCaptor =
                ArgumentCaptor.forClass(CreateTripCommand.class);

        verify(createTripUseCase).create(commandCaptor.capture());

        assertEquals(
                0,
                BigDecimal.ZERO.compareTo(
                        commandCaptor.getValue().contributionAmount()
                )
        );
    }

    @Test
    void shouldReturnBadRequestWhenRequestHasValidationErrors() throws Exception {
        Instant departureAt = Instant.now().plus(2, ChronoUnit.DAYS);

        mockMvc.perform(post("/api/v1/trips")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", DRIVER_ID.value())
                        .content("""
                                {
                                  "destination": "CIUDAD_REAL",
                                  "departureAt": "%s",
                                  "totalSeats": 0,
                                  "contributionAmount": -1.00
                                }
                                """.formatted(departureAt)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.origin")
                        .value("El origen es obligatorio"))
                .andExpect(jsonPath("$.fieldErrors.totalSeats")
                        .value("Debe ofrecerse al menos una plaza"))
                .andExpect(jsonPath("$.fieldErrors.contributionAmount")
                        .value("La contribución no puede ser negativa"));

        verify(createTripUseCase, never()).create(any());
    }

    @Test
    void shouldReturnBadRequestWhenRouteIsInvalid() throws Exception {
        Instant departureAt = Instant.now().plus(2, ChronoUnit.DAYS);

        when(createTripUseCase.create(any(CreateTripCommand.class)))
                .thenThrow(new InvalidTripException(
                        "El origen y el destino deben ser distintos"
                ));

        mockMvc.perform(post("/api/v1/trips")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", DRIVER_ID.value())
                        .content("""
                                {
                                  "origin": "DAIMIEL",
                                  "destination": "DAIMIEL",
                                  "departureAt": "%s",
                                  "totalSeats": 2,
                                  "contributionAmount": 3.00
                                }
                                """.formatted(departureAt)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_TRIP"))
                .andExpect(jsonPath("$.message")
                        .value("El origen y el destino deben ser distintos"));
    }

    @Test
    void shouldReturnNotFoundWhenDriverDoesNotExist() throws Exception {
        Instant departureAt = Instant.now().plus(2, ChronoUnit.DAYS);

        when(createTripUseCase.create(any(CreateTripCommand.class)))
                .thenThrow(new UserNotFoundException(
                        "No existe el usuario conductor"
                ));

        mockMvc.perform(post("/api/v1/trips")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", DRIVER_ID.value())
                        .content(validRequest(departureAt, "3.00")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"))
                .andExpect(jsonPath("$.message")
                        .value("No existe el usuario conductor"));
    }

    @Test
    void shouldReturnForbiddenWhenDriverPhoneIsNotVerified() throws Exception {
        Instant departureAt = Instant.now().plus(2, ChronoUnit.DAYS);

        when(createTripUseCase.create(any(CreateTripCommand.class)))
                .thenThrow(new UserPhoneNotVerifiedException(
                        "Debes verificar tu teléfono antes de publicar un viaje"
                ));

        mockMvc.perform(post("/api/v1/trips")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", DRIVER_ID.value())
                        .content(validRequest(departureAt, "3.00")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("PHONE_NOT_VERIFIED"))
                .andExpect(jsonPath("$.message")
                        .value("Debes verificar tu teléfono antes de publicar un viaje"));
    }

    @Test
    void shouldReturnBadRequestWhenUserHeaderIsMissing() throws Exception {
        Instant departureAt = Instant.now().plus(2, ChronoUnit.DAYS);

        mockMvc.perform(post("/api/v1/trips")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest(departureAt, "3.00")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MISSING_HEADER"))
                .andExpect(jsonPath("$.message")
                        .value("Falta la cabecera obligatoria: X-User-Id"));

        verify(createTripUseCase, never()).create(any());
    }

    private String validRequest(Instant departureAt, String contributionAmount) {
        return """
                {
                  "origin": "DAIMIEL",
                  "destination": "CIUDAD_REAL",
                  "departureAt": "%s",
                  "totalSeats": 3,
                  "contributionAmount": %s,
                  "departurePoint": "Plaza de España, Daimiel",
                  "arrivalPoint": "Estación de autobuses, Ciudad Real",
                  "comment": "Salgo puntual"
                }
                """.formatted(departureAt, contributionAmount);
    }

    private String requestWithoutContribution(Instant departureAt) {
        return """
                {
                  "origin": "DAIMIEL",
                  "destination": "CIUDAD_REAL",
                  "departureAt": "%s",
                  "totalSeats": 3,
                  "departurePoint": "Plaza de España, Daimiel",
                  "arrivalPoint": "Estación de autobuses, Ciudad Real",
                  "comment": "Salgo puntual"
                }
                """.formatted(departureAt);
    }
}