package com.daimielcr.backend.application.service.trip;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.daimielcr.backend.application.port.in.trip.CreateTripCommand;
import com.daimielcr.backend.application.port.out.trip.TripRepositoryPort;
import com.daimielcr.backend.application.port.out.user.UserRepositoryPort;
import com.daimielcr.backend.domain.exceptions.InvalidTripException;
import com.daimielcr.backend.domain.exceptions.UserNotFoundException;
import com.daimielcr.backend.domain.exceptions.UserPhoneNotVerifiedException;
import com.daimielcr.backend.domain.model.trip.Trip;
import com.daimielcr.backend.domain.model.trip.TripId;
import com.daimielcr.backend.domain.model.trip.TripLocation;
import com.daimielcr.backend.domain.model.trip.TripStatus;
import com.daimielcr.backend.domain.model.user.DisplayName;
import com.daimielcr.backend.domain.model.user.PhoneNumber;
import com.daimielcr.backend.domain.model.user.User;
import com.daimielcr.backend.domain.model.user.UserId;

@ExtendWith(MockitoExtension.class)
class CreateTripServiceTest {

    private static final Instant NOW = Instant.parse("2026-06-26T10:00:00Z");
    private static final Instant DEPARTURE_AT = Instant.parse("2026-06-27T07:30:00Z");

    private static final UserId DRIVER_ID =
            new UserId(UUID.fromString("11111111-1111-1111-1111-111111111111"));

    @Mock
    private UserRepositoryPort userRepository;

    @Mock
    private TripRepositoryPort tripRepository;

    @Captor
    private ArgumentCaptor<Trip> tripCaptor;

    private CreateTripService service;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(NOW, ZoneOffset.UTC);

        service = new CreateTripService(
                userRepository,
                tripRepository,
                fixedClock
        );
    }

    @Test
    void shouldCreateAndSaveTripWhenDriverExistsAndPhoneIsVerified() {
        User verifiedDriver = verifiedDriver();
        CreateTripCommand command = validCommand();

        when(userRepository.findById(DRIVER_ID))
                .thenReturn(Optional.of(verifiedDriver));

        TripId tripId = service.create(command);

        assertNotNull(tripId);

        verify(tripRepository).save(tripCaptor.capture());

        Trip savedTrip = tripCaptor.getValue();

        assertEquals(tripId, savedTrip.id());
        assertEquals(DRIVER_ID, savedTrip.driverId());
        assertEquals(TripLocation.DAIMIEL, savedTrip.route().origin());
        assertEquals(TripLocation.CIUDAD_REAL, savedTrip.route().destination());
        assertEquals(DEPARTURE_AT, savedTrip.departureAt().value());
        assertEquals(3, savedTrip.totalSeats().value());
        assertEquals(3, savedTrip.availableSeats().value());
        assertEquals(new BigDecimal("3.00"), savedTrip.contributionAmount().value());
        assertEquals("Plaza de España, Daimiel", savedTrip.departurePoint());
        assertEquals("Estación de autobuses, Ciudad Real", savedTrip.arrivalPoint());
        assertEquals("Salgo puntual", savedTrip.comment());
        assertEquals(TripStatus.ACTIVE, savedTrip.status());
        assertEquals(NOW, savedTrip.createdAt());
        assertEquals(NOW, savedTrip.updatedAt());
    }

    @Test
    void shouldThrowWhenDriverDoesNotExist() {
        when(userRepository.findById(DRIVER_ID))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> service.create(validCommand())
        );

        verify(tripRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldThrowWhenDriverPhoneIsNotVerified() {
        User unverifiedDriver = User.register(
                DRIVER_ID,
                new PhoneNumber("+34600111222"),
                new DisplayName("Javier Fernández"),
                NOW
        );

        when(userRepository.findById(DRIVER_ID))
                .thenReturn(Optional.of(unverifiedDriver));

        assertThrows(
                UserPhoneNotVerifiedException.class,
                () -> service.create(validCommand())
        );

        verify(tripRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldThrowWhenRouteHasSameOriginAndDestination() {
        when(userRepository.findById(DRIVER_ID))
                .thenReturn(Optional.of(verifiedDriver()));

        CreateTripCommand invalidCommand = new CreateTripCommand(
                DRIVER_ID,
                TripLocation.DAIMIEL,
                TripLocation.DAIMIEL,
                DEPARTURE_AT,
                3,
                new BigDecimal("3.00"),
                "Plaza de España, Daimiel",
                "Otra zona de Daimiel",
                null
        );

        assertThrows(
                InvalidTripException.class,
                () -> service.create(invalidCommand)
        );

        verify(tripRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldThrowWhenDepartureIsNotInTheFuture() {
        when(userRepository.findById(DRIVER_ID))
                .thenReturn(Optional.of(verifiedDriver()));

        CreateTripCommand invalidCommand = new CreateTripCommand(
                DRIVER_ID,
                TripLocation.DAIMIEL,
                TripLocation.CIUDAD_REAL,
                NOW,
                3,
                new BigDecimal("3.00"),
                "Plaza de España, Daimiel",
                "Estación de autobuses, Ciudad Real",
                null
        );

        assertThrows(
                InvalidTripException.class,
                () -> service.create(invalidCommand)
        );

        verify(tripRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    private User verifiedDriver() {
        User user = User.register(
                DRIVER_ID,
                new PhoneNumber("+34600111222"),
                new DisplayName("Javier Fernández"),
                NOW
        );

        user.verifyPhone(NOW);

        return user;
    }

    private CreateTripCommand validCommand() {
        return new CreateTripCommand(
                DRIVER_ID,
                TripLocation.DAIMIEL,
                TripLocation.CIUDAD_REAL,
                DEPARTURE_AT,
                3,
                new BigDecimal("3.00"),
                "Plaza de España, Daimiel",
                "Estación de autobuses, Ciudad Real",
                "Salgo puntual"
        );
    }
}