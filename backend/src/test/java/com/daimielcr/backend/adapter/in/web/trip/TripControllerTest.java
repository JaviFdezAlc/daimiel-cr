package com.daimielcr.backend.adapter.in.web.trip;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.daimielcr.backend.application.port.in.trip.CancelTripCommand;
import com.daimielcr.backend.application.port.in.trip.CancelTripUseCase;
import com.daimielcr.backend.application.port.in.trip.CreateTripCommand;
import com.daimielcr.backend.application.port.in.trip.CreateTripUseCase;
import com.daimielcr.backend.application.port.in.trip.GetTripDetailUseCase;
import com.daimielcr.backend.application.port.in.trip.SearchTripsQuery;
import com.daimielcr.backend.application.port.in.trip.SearchTripsResult;
import com.daimielcr.backend.application.port.in.trip.SearchTripsUseCase;
import com.daimielcr.backend.application.port.in.trip.TripDetail;
import com.daimielcr.backend.application.port.in.trip.TripSort;
import com.daimielcr.backend.application.port.in.trip.TripSummary;
import com.daimielcr.backend.application.port.in.trip.UpdateTripCommand;
import com.daimielcr.backend.application.port.in.trip.UpdateTripUseCase;
import com.daimielcr.backend.config.SecurityConfig;
import com.daimielcr.backend.domain.exceptions.InvalidTripException;
import com.daimielcr.backend.domain.exceptions.TripNotAvailableException;
import com.daimielcr.backend.domain.exceptions.TripNotFoundException;
import com.daimielcr.backend.domain.exceptions.UnauthorizedTripActionException;
import com.daimielcr.backend.domain.exceptions.UserNotFoundException;
import com.daimielcr.backend.domain.exceptions.UserPhoneNotVerifiedException;
import com.daimielcr.backend.domain.model.trip.TripId;
import com.daimielcr.backend.domain.model.trip.TripLocation;
import com.daimielcr.backend.domain.model.trip.TripStatus;
import com.daimielcr.backend.domain.model.user.UserId;

@WebMvcTest(controllers = TripController.class)
@Import(SecurityConfig.class)
class TripControllerTest {

        private static final UserId DRIVER_ID = new UserId(UUID.fromString("11111111-1111-1111-1111-111111111111"));

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private CreateTripUseCase createTripUseCase;

        @MockitoBean
        private GetTripDetailUseCase getTripDetailUseCase;

        @MockitoBean
        private SearchTripsUseCase searchTripsUseCase;

        @MockitoBean
        private UpdateTripUseCase updateTripUseCase;

        @MockitoBean
        private CancelTripUseCase cancelTripUseCase;

        @Test
        void shouldCreateTripAndReturnCreatedResponse() throws Exception {
                UUID createdTripUuid = UUID.fromString("22222222-2222-2222-2222-222222222222");

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
                                                containsString("/api/v1/trips/" + createdTripUuid)))
                                .andExpect(jsonPath("$.tripId").value(createdTripUuid.toString()));

                ArgumentCaptor<CreateTripCommand> commandCaptor = ArgumentCaptor.forClass(CreateTripCommand.class);

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
                                                .compareTo(command.contributionAmount()));
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

                ArgumentCaptor<CreateTripCommand> commandCaptor = ArgumentCaptor.forClass(CreateTripCommand.class);

                verify(createTripUseCase).create(commandCaptor.capture());

                assertEquals(
                                0,
                                BigDecimal.ZERO.compareTo(
                                                commandCaptor.getValue().contributionAmount()));
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
                                                "El origen y el destino deben ser distintos"));

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
                                                "No existe el usuario conductor"));

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
                                                "Debes verificar tu teléfono antes de publicar un viaje"));

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

        @Test
        void shouldReturnTripDetailWhenTripExists() throws Exception {
                UUID tripUuid = UUID.fromString("22222222-2222-2222-2222-222222222222");

                Instant departureAt = Instant.parse("2030-06-27T07:30:00Z");
                Instant createdAt = Instant.parse("2026-06-26T10:00:00Z");
                Instant updatedAt = Instant.parse("2026-06-26T10:15:00Z");

                TripDetail detail = new TripDetail(
                                new TripId(tripUuid),
                                DRIVER_ID,
                                TripLocation.DAIMIEL,
                                TripLocation.CIUDAD_REAL,
                                departureAt,
                                3,
                                2,
                                new BigDecimal("3.00"),
                                "Plaza de España, Daimiel",
                                "Estación de autobuses, Ciudad Real",
                                "Salgo puntual",
                                TripStatus.ACTIVE,
                                createdAt,
                                updatedAt);

                when(getTripDetailUseCase.getById(new TripId(tripUuid)))
                                .thenReturn(detail);

                mockMvc.perform(get("/api/v1/trips/{tripId}", tripUuid))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(tripUuid.toString()))
                                .andExpect(jsonPath("$.driverId").value(DRIVER_ID.value().toString()))
                                .andExpect(jsonPath("$.origin").value("DAIMIEL"))
                                .andExpect(jsonPath("$.destination").value("CIUDAD_REAL"))
                                .andExpect(jsonPath("$.departureAt").value(departureAt.toString()))
                                .andExpect(jsonPath("$.totalSeats").value(3))
                                .andExpect(jsonPath("$.availableSeats").value(2))
                                .andExpect(jsonPath("$.contributionAmount").value(3.0))
                                .andExpect(jsonPath("$.departurePoint")
                                                .value("Plaza de España, Daimiel"))
                                .andExpect(jsonPath("$.arrivalPoint")
                                                .value("Estación de autobuses, Ciudad Real"))
                                .andExpect(jsonPath("$.comment").value("Salgo puntual"))
                                .andExpect(jsonPath("$.status").value("ACTIVE"))
                                .andExpect(jsonPath("$.createdAt").value(createdAt.toString()))
                                .andExpect(jsonPath("$.updatedAt").value(updatedAt.toString()));

                verify(getTripDetailUseCase).getById(new TripId(tripUuid));
        }

        @Test
        void shouldReturnNotFoundWhenTripDoesNotExist() throws Exception {
                UUID tripUuid = UUID.fromString("33333333-3333-3333-3333-333333333333");

                when(getTripDetailUseCase.getById(new TripId(tripUuid)))
                                .thenThrow(new TripNotFoundException(
                                                "No existe el viaje: " + tripUuid));

                mockMvc.perform(get("/api/v1/trips/{tripId}", tripUuid))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.code").value("TRIP_NOT_FOUND"))
                                .andExpect(jsonPath("$.message")
                                                .value("No existe el viaje: " + tripUuid))
                                .andExpect(jsonPath("$.path")
                                                .value("/api/v1/trips/" + tripUuid));

                verify(getTripDetailUseCase).getById(new TripId(tripUuid));
        }

        @Test
        void shouldReturnBadRequestWhenTripIdIsNotValidUuid() throws Exception {
                mockMvc.perform(get("/api/v1/trips/not-a-valid-uuid"))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.code").value("INVALID_PARAMETER"))
                                .andExpect(jsonPath("$.message")
                                                .value("Uno de los parámetros de la petición tiene un formato inválido"))
                                .andExpect(jsonPath("$.path")
                                                .value("/api/v1/trips/not-a-valid-uuid"));

                verifyNoInteractions(getTripDetailUseCase);
        }

        @Test
        void shouldSearchTripsWithProvidedFilters() throws Exception {
                UUID tripUuid = UUID.fromString("55555555-5555-5555-5555-555555555555");

                Instant departureAt = Instant.parse("2030-06-27T07:30:00Z");

                SearchTripsResult result = new SearchTripsResult(
                                List.of(
                                                new TripSummary(
                                                                new TripId(tripUuid),
                                                                TripLocation.DAIMIEL,
                                                                TripLocation.CIUDAD_REAL,
                                                                departureAt,
                                                                "Plaza de España, Daimiel",
                                                                "Estación de autobuses, Ciudad Real",
                                                                3,
                                                                new BigDecimal("3.00"))),
                                1,
                                10,
                                25,
                                3);

                when(searchTripsUseCase.search(any(SearchTripsQuery.class)))
                                .thenReturn(result);

                mockMvc.perform(get("/api/v1/trips")
                                .param("origin", "DAIMIEL")
                                .param("destination", "CIUDAD_REAL")
                                .param("date", "2030-06-27")
                                .param("requiredSeats", "2")
                                .param("sort", "CONTRIBUTION_ASC")
                                .param("page", "1")
                                .param("size", "10"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.page").value(1))
                                .andExpect(jsonPath("$.size").value(10))
                                .andExpect(jsonPath("$.totalElements").value(25))
                                .andExpect(jsonPath("$.totalPages").value(3))
                                .andExpect(jsonPath("$.trips.length()").value(1))
                                .andExpect(jsonPath("$.trips[0].id").value(tripUuid.toString()))
                                .andExpect(jsonPath("$.trips[0].origin").value("DAIMIEL"))
                                .andExpect(jsonPath("$.trips[0].destination").value("CIUDAD_REAL"))
                                .andExpect(jsonPath("$.trips[0].departureAt")
                                                .value(departureAt.toString()))
                                .andExpect(jsonPath("$.trips[0].departurePoint")
                                                .value("Plaza de España, Daimiel"))
                                .andExpect(jsonPath("$.trips[0].arrivalPoint")
                                                .value("Estación de autobuses, Ciudad Real"))
                                .andExpect(jsonPath("$.trips[0].availableSeats").value(3))
                                .andExpect(jsonPath("$.trips[0].contributionAmount").value(3.0));

                ArgumentCaptor<SearchTripsQuery> queryCaptor = ArgumentCaptor.forClass(SearchTripsQuery.class);

                verify(searchTripsUseCase).search(queryCaptor.capture());

                SearchTripsQuery query = queryCaptor.getValue();

                assertEquals(TripLocation.DAIMIEL, query.origin());
                assertEquals(TripLocation.CIUDAD_REAL, query.destination());
                assertEquals(LocalDate.of(2030, 6, 27), query.date());
                assertEquals(2, query.requiredSeats());
                assertEquals(TripSort.CONTRIBUTION_ASC, query.sort());
                assertEquals(1, query.page());
                assertEquals(10, query.size());
        }

        @Test
        void shouldUseDefaultSearchValuesWhenOptionalFiltersAreOmitted() throws Exception {
                when(searchTripsUseCase.search(any(SearchTripsQuery.class)))
                                .thenReturn(new SearchTripsResult(
                                                List.of(),
                                                0,
                                                20,
                                                0,
                                                0));

                mockMvc.perform(get("/api/v1/trips")
                                .param("origin", "DAIMIEL")
                                .param("destination", "CIUDAD_REAL"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.trips.length()").value(0))
                                .andExpect(jsonPath("$.page").value(0))
                                .andExpect(jsonPath("$.size").value(20))
                                .andExpect(jsonPath("$.totalElements").value(0))
                                .andExpect(jsonPath("$.totalPages").value(0));

                ArgumentCaptor<SearchTripsQuery> queryCaptor = ArgumentCaptor.forClass(SearchTripsQuery.class);

                verify(searchTripsUseCase).search(queryCaptor.capture());

                SearchTripsQuery query = queryCaptor.getValue();

                assertNull(query.date());
                assertEquals(1, query.requiredSeats());
                assertEquals(TripSort.DEPARTURE_ASC, query.sort());
                assertEquals(0, query.page());
                assertEquals(20, query.size());
        }

        @Test
        void shouldReturnBadRequestWhenOriginAndDestinationAreEqual() throws Exception {
                mockMvc.perform(get("/api/v1/trips")
                                .param("origin", "DAIMIEL")
                                .param("destination", "DAIMIEL"))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.code").value("INVALID_SEARCH_QUERY"))
                                .andExpect(jsonPath("$.message")
                                                .value("El origen y el destino deben ser distintos"));

                verifyNoInteractions(searchTripsUseCase);
        }

        @Test
        void shouldReturnBadRequestWhenRequiredSeatsIsZero() throws Exception {
                mockMvc.perform(get("/api/v1/trips")
                                .param("origin", "DAIMIEL")
                                .param("destination", "CIUDAD_REAL")
                                .param("requiredSeats", "0"))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.code").value("INVALID_SEARCH_QUERY"))
                                .andExpect(jsonPath("$.message")
                                                .value("Debe solicitarse al menos una plaza"));

                verifyNoInteractions(searchTripsUseCase);
        }

        @Test
        void shouldReturnBadRequestWhenOriginIsMissing() throws Exception {
                mockMvc.perform(get("/api/v1/trips")
                                .param("destination", "CIUDAD_REAL"))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.code").value("MISSING_PARAMETER"))
                                .andExpect(jsonPath("$.message")
                                                .value("Falta el parámetro obligatorio: origin"));

                verifyNoInteractions(searchTripsUseCase);
        }

        @Test
        void shouldUpdateTripWhenRequestIsValid() throws Exception {
                UUID tripUuid = UUID.fromString(
                                "55555555-5555-5555-5555-555555555555");

                UUID driverUuid = UUID.fromString(
                                "11111111-1111-1111-1111-111111111111");

                Instant departureAt = Instant.parse("2030-07-02T15:30:00Z");

                TripDetail updatedTrip = new TripDetail(
                                new TripId(tripUuid),
                                new UserId(driverUuid),
                                TripLocation.DAIMIEL,
                                TripLocation.CIUDAD_REAL,
                                departureAt,
                                4,
                                4,
                                new BigDecimal("3.50"),
                                "Plaza de España, Daimiel",
                                "Estación de autobuses, Ciudad Real",
                                "Salgo después de clase",
                                TripStatus.ACTIVE,
                                Instant.parse("2030-06-01T10:00:00Z"),
                                Instant.parse("2030-06-29T17:00:00Z"));

                when(updateTripUseCase.update(any(UpdateTripCommand.class)))
                                .thenReturn(updatedTrip);

                mockMvc.perform(put("/api/v1/trips/{tripId}", tripUuid)
                                .header("X-User-Id", driverUuid)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                  "origin": "DAIMIEL",
                                                  "destination": "CIUDAD_REAL",
                                                  "departureAt": "2030-07-02T15:30:00Z",
                                                  "totalSeats": 4,
                                                  "contributionAmount": 3.50,
                                                  "departurePoint": "Plaza de España, Daimiel",
                                                  "arrivalPoint": "Estación de autobuses, Ciudad Real",
                                                  "comment": "Salgo después de clase"
                                                }
                                                """))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(tripUuid.toString()))
                                .andExpect(jsonPath("$.driverId").value(driverUuid.toString()))
                                .andExpect(jsonPath("$.origin").value("DAIMIEL"))
                                .andExpect(jsonPath("$.destination").value("CIUDAD_REAL"))
                                .andExpect(jsonPath("$.departureAt").value(departureAt.toString()))
                                .andExpect(jsonPath("$.totalSeats").value(4))
                                .andExpect(jsonPath("$.availableSeats").value(4))
                                .andExpect(jsonPath("$.contributionAmount").value(3.5))
                                .andExpect(jsonPath("$.status").value("ACTIVE"));

                ArgumentCaptor<UpdateTripCommand> commandCaptor = ArgumentCaptor.forClass(UpdateTripCommand.class);

                verify(updateTripUseCase).update(commandCaptor.capture());

                UpdateTripCommand command = commandCaptor.getValue();

                assertEquals(new TripId(tripUuid), command.tripId());
                assertEquals(new UserId(driverUuid), command.requesterId());
                assertEquals(TripLocation.DAIMIEL, command.origin());
                assertEquals(TripLocation.CIUDAD_REAL, command.destination());
                assertEquals(departureAt, command.departureAt());
                assertEquals(4, command.totalSeats());
                assertEquals(new BigDecimal("3.50"), command.contributionAmount());
        }

        @Test
        void shouldReturnNotFoundWhenUpdatingUnknownTrip() throws Exception {
                UUID tripUuid = UUID.fromString(
                                "55555555-5555-5555-5555-555555555555");

                UUID driverUuid = UUID.fromString(
                                "11111111-1111-1111-1111-111111111111");

                when(updateTripUseCase.update(any(UpdateTripCommand.class)))
                                .thenThrow(new TripNotFoundException(
                                                "No existe el viaje: " + tripUuid));

                mockMvc.perform(put("/api/v1/trips/{tripId}", tripUuid)
                                .header("X-User-Id", driverUuid)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(validUpdateTripJson()))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.code").value("TRIP_NOT_FOUND"));
        }

        @Test
        void shouldReturnForbiddenWhenRequesterIsNotTripDriver() throws Exception {
                UUID tripUuid = UUID.fromString(
                                "55555555-5555-5555-5555-555555555555");

                UUID requesterUuid = UUID.fromString(
                                "33333333-3333-3333-3333-333333333333");

                when(updateTripUseCase.update(any(UpdateTripCommand.class)))
                                .thenThrow(new UnauthorizedTripActionException(
                                                "No tienes permiso para modificar este viaje"));

                mockMvc.perform(put("/api/v1/trips/{tripId}", tripUuid)
                                .header("X-User-Id", requesterUuid)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(validUpdateTripJson()))
                                .andExpect(status().isForbidden())
                                .andExpect(jsonPath("$.code")
                                                .value("FORBIDDEN"));
        }

        @Test
        void shouldReturnBadRequestWhenUpdateTripRequestIsInvalid() throws Exception {
                UUID tripUuid = UUID.fromString(
                                "55555555-5555-5555-5555-555555555555");

                UUID driverUuid = UUID.fromString(
                                "11111111-1111-1111-1111-111111111111");

                mockMvc.perform(put("/api/v1/trips/{tripId}", tripUuid)
                                .header("X-User-Id", driverUuid)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                  "origin": "DAIMIEL",
                                                  "destination": "CIUDAD_REAL",
                                                  "departureAt": "2030-07-02T15:30:00Z",
                                                  "totalSeats": 0,
                                                  "contributionAmount": 3.50,
                                                  "departurePoint": "Plaza de España, Daimiel",
                                                  "arrivalPoint": "Estación de autobuses, Ciudad Real"
                                                }
                                                """))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

                verifyNoInteractions(updateTripUseCase);
        }

        private String validUpdateTripJson() {
                return """
                                {
                                  "origin": "DAIMIEL",
                                  "destination": "CIUDAD_REAL",
                                  "departureAt": "2030-07-02T15:30:00Z",
                                  "totalSeats": 4,
                                  "contributionAmount": 3.50,
                                  "departurePoint": "Plaza de España, Daimiel",
                                  "arrivalPoint": "Estación de autobuses, Ciudad Real",
                                  "comment": "Salgo después de clase"
                                }
                                """;
        }

        @Test
        void shouldCancelTripWhenRequesterIsDriver() throws Exception {
                UUID tripUuid = UUID.fromString(
                                "55555555-5555-5555-5555-555555555555");

                UUID driverUuid = UUID.fromString(
                                "11111111-1111-1111-1111-111111111111");

                mockMvc.perform(delete("/api/v1/trips/{tripId}", tripUuid)
                                .header("X-User-Id", driverUuid))
                                .andExpect(status().isNoContent())
                                .andExpect(content().string(""));

                ArgumentCaptor<CancelTripCommand> commandCaptor = ArgumentCaptor.forClass(CancelTripCommand.class);

                verify(cancelTripUseCase).cancel(commandCaptor.capture());

                CancelTripCommand command = commandCaptor.getValue();

                assertEquals(new TripId(tripUuid), command.tripId());
                assertEquals(new UserId(driverUuid), command.requesterId());
        }

        @Test
        void shouldReturnNotFoundWhenCancellingUnknownTrip() throws Exception {
                UUID tripUuid = UUID.fromString(
                                "55555555-5555-5555-5555-555555555555");

                UUID driverUuid = UUID.fromString(
                                "11111111-1111-1111-1111-111111111111");

                doThrow(new TripNotFoundException(
                                "No existe el viaje: " + tripUuid)).when(cancelTripUseCase)
                                .cancel(any(CancelTripCommand.class));

                mockMvc.perform(delete("/api/v1/trips/{tripId}", tripUuid)
                                .header("X-User-Id", driverUuid))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.code").value("TRIP_NOT_FOUND"));
        }

        @Test
        void shouldReturnForbiddenWhenRequesterIsNotTripDriverOnCancel()
                        throws Exception {

                UUID tripUuid = UUID.fromString(
                                "55555555-5555-5555-5555-555555555555");

                UUID requesterUuid = UUID.fromString(
                                "33333333-3333-3333-3333-333333333333");

                doThrow(new UnauthorizedTripActionException(
                                "No tienes permiso para cancelar este viaje")).when(cancelTripUseCase)
                                .cancel(any(CancelTripCommand.class));

                mockMvc.perform(delete("/api/v1/trips/{tripId}", tripUuid)
                                .header("X-User-Id", requesterUuid))
                                .andExpect(status().isForbidden())
                                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
        }

        @Test
        void shouldReturnConflictWhenTripCannotBeCancelled() throws Exception {
                UUID tripUuid = UUID.fromString(
                                "55555555-5555-5555-5555-555555555555");

                UUID driverUuid = UUID.fromString(
                                "11111111-1111-1111-1111-111111111111");

                doThrow(new TripNotAvailableException(
                                "El viaje ya no puede cancelarse")).when(cancelTripUseCase)
                                .cancel(any(CancelTripCommand.class));

                mockMvc.perform(delete("/api/v1/trips/{tripId}", tripUuid)
                                .header("X-User-Id", driverUuid))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.code").value("TRIP_CONFLICT"));
        }
}
