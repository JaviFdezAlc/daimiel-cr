package com.daimielcr.backend.application.service.ride_request;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Captor;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.daimielcr.backend.application.port.in.ride_request.CreateRideRequestCommand;
import com.daimielcr.backend.application.port.out.ride_request.RideRequestRepositoryPort;
import com.daimielcr.backend.application.port.out.trip.TripRepositoryPort;
import com.daimielcr.backend.application.port.out.user.UserRepositoryPort;
import com.daimielcr.backend.domain.exceptions.DuplicateRideRequestException;
import com.daimielcr.backend.domain.exceptions.InvalidRideRequestException;
import com.daimielcr.backend.domain.exceptions.TripNotAvailableException;
import com.daimielcr.backend.domain.exceptions.TripNotFoundException;
import com.daimielcr.backend.domain.exceptions.UserNotFoundException;
import com.daimielcr.backend.domain.exceptions.UserPhoneNotVerifiedException;
import com.daimielcr.backend.domain.model.ride_request.RequestedSeats;
import com.daimielcr.backend.domain.model.ride_request.RideRequest;
import com.daimielcr.backend.domain.model.ride_request.RideRequestId;
import com.daimielcr.backend.domain.model.ride_request.RideRequestStatus;
import com.daimielcr.backend.domain.model.trip.ContributionAmount;
import com.daimielcr.backend.domain.model.trip.DepartureAt;
import com.daimielcr.backend.domain.model.trip.Route;
import com.daimielcr.backend.domain.model.trip.SeatCount;
import com.daimielcr.backend.domain.model.trip.Trip;
import com.daimielcr.backend.domain.model.trip.TripId;
import com.daimielcr.backend.domain.model.trip.TripLocation;
import com.daimielcr.backend.domain.model.user.User;
import com.daimielcr.backend.domain.model.user.UserId;

@ExtendWith(MockitoExtension.class)
class CreateRideRequestServiceTest {

    private static final Instant NOW =
            Instant.parse("2026-06-29T17:00:00Z");

    private static final TripId TRIP_ID =
            new TripId(UUID.fromString(
                    "11111111-1111-1111-1111-111111111111"
            ));

    private static final UserId DRIVER_ID =
            new UserId(UUID.fromString(
                    "22222222-2222-2222-2222-222222222222"
            ));

    private static final UserId PASSENGER_ID =
            new UserId(UUID.fromString(
                    "33333333-3333-3333-3333-333333333333"
            ));

    @Mock
    private UserRepositoryPort userRepository;

    @Mock
    private TripRepositoryPort tripRepository;

    @Mock
    private RideRequestRepositoryPort rideRequestRepository;

    @Mock
    private User passenger;

    @Captor
    private ArgumentCaptor<RideRequest> rideRequestCaptor;

    private CreateRideRequestService service;

    @BeforeEach
    void setUp() {
        service = new CreateRideRequestService(
                userRepository,
                tripRepository,
                rideRequestRepository,
                Clock.fixed(NOW, ZoneOffset.UTC)
        );
    }

    @Test
    void shouldCreatePendingRideRequestForVerifiedPassenger() {
        givenVerifiedPassenger(PASSENGER_ID);

        Trip trip = activeTrip();

        when(tripRepository.findById(TRIP_ID))
                .thenReturn(Optional.of(trip));

        when(rideRequestRepository.existsPendingByTripIdAndPassengerId(
                TRIP_ID,
                PASSENGER_ID
        )).thenReturn(false);

        RideRequestId result = service.create(
                new CreateRideRequestCommand(
                        TRIP_ID,
                        PASSENGER_ID,
                        2,
                        "  ¿Te quedan dos plazas?  "
                )
        );

        verify(rideRequestRepository).save(rideRequestCaptor.capture());

        RideRequest savedRequest = rideRequestCaptor.getValue();

        assertEquals(result, savedRequest.id());
        assertEquals(TRIP_ID, savedRequest.tripId());
        assertEquals(PASSENGER_ID, savedRequest.passengerId());
        assertEquals(new RequestedSeats(2), savedRequest.requestedSeats());
        assertEquals("¿Te quedan dos plazas?", savedRequest.message());
        assertEquals(RideRequestStatus.PENDING, savedRequest.status());
        assertTrue(savedRequest.isPending());
        assertEquals(NOW, savedRequest.createdAt());
        assertEquals(NOW, savedRequest.updatedAt());

        // Crear una solicitud pendiente no debe reservar plazas todavía.
        assertEquals(3, trip.availableSeats().value());
    }

    @Test
    void shouldThrowWhenPassengerDoesNotExist() {
        when(userRepository.findById(PASSENGER_ID))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> service.create(validCommand())
        );

        verifyNoInteractions(tripRepository, rideRequestRepository);
    }

    @Test
    void shouldThrowWhenPassengerPhoneIsNotVerified() {
        when(userRepository.findById(PASSENGER_ID))
                .thenReturn(Optional.of(passenger));

        when(passenger.isPhoneVerified()).thenReturn(false);

        assertThrows(
                UserPhoneNotVerifiedException.class,
                () -> service.create(validCommand())
        );

        verifyNoInteractions(tripRepository, rideRequestRepository);
    }

    @Test
    void shouldThrowWhenTripDoesNotExist() {
        givenVerifiedPassenger(PASSENGER_ID);

        when(tripRepository.findById(TRIP_ID))
                .thenReturn(Optional.empty());

        assertThrows(
                TripNotFoundException.class,
                () -> service.create(validCommand())
        );

        verifyNoInteractions(rideRequestRepository);
    }

    @Test
    void shouldThrowWhenTripDoesNotAcceptRequests() {
        givenVerifiedPassenger(PASSENGER_ID);

        Trip cancelledTrip = activeTrip();
        cancelledTrip.cancel(DRIVER_ID, NOW);

        when(tripRepository.findById(TRIP_ID))
                .thenReturn(Optional.of(cancelledTrip));

        assertThrows(
                TripNotAvailableException.class,
                () -> service.create(validCommand())
        );

        verifyNoInteractions(rideRequestRepository);
    }

    @Test
    void shouldThrowWhenPassengerRequestsSeatInOwnTrip() {
        givenVerifiedPassenger(DRIVER_ID);

        when(tripRepository.findById(TRIP_ID))
                .thenReturn(Optional.of(activeTrip()));

        assertThrows(
                InvalidRideRequestException.class,
                () -> service.create(
                        new CreateRideRequestCommand(
                                TRIP_ID,
                                DRIVER_ID,
                                1,
                                "Quiero apuntarme"
                        )
                )
        );

        verifyNoInteractions(rideRequestRepository);
    }

    @Test
    void shouldThrowWhenPassengerAlreadyHasPendingRequestForTrip() {
        givenVerifiedPassenger(PASSENGER_ID);

        when(tripRepository.findById(TRIP_ID))
                .thenReturn(Optional.of(activeTrip()));

        when(rideRequestRepository.existsPendingByTripIdAndPassengerId(
                TRIP_ID,
                PASSENGER_ID
        )).thenReturn(true);

        assertThrows(
                DuplicateRideRequestException.class,
                () -> service.create(validCommand())
        );

        verify(rideRequestRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenRequestedSeatsAreNotPositive() {
        givenVerifiedPassenger(PASSENGER_ID);

        when(tripRepository.findById(TRIP_ID))
                .thenReturn(Optional.of(activeTrip()));

        assertThrows(
                InvalidRideRequestException.class,
                () -> service.create(
                        new CreateRideRequestCommand(
                                TRIP_ID,
                                PASSENGER_ID,
                                0,
                                "¿Hay sitio?"
                        )
                )
        );

        verifyNoInteractions(rideRequestRepository);
    }

    private void givenVerifiedPassenger(UserId passengerId) {
        when(userRepository.findById(passengerId))
                .thenReturn(Optional.of(passenger));

        when(passenger.id()).thenReturn(passengerId);
        when(passenger.isPhoneVerified()).thenReturn(true);
    }

    private CreateRideRequestCommand validCommand() {
        return new CreateRideRequestCommand(
                TRIP_ID,
                PASSENGER_ID,
                1,
                "¿Hay sitio?"
        );
    }

    private Trip activeTrip() {
        return Trip.create(
                TRIP_ID,
                DRIVER_ID,
                new Route(
                        TripLocation.DAIMIEL,
                        TripLocation.CIUDAD_REAL
                ),
                new DepartureAt(
                        Instant.parse("2030-07-01T08:00:00Z")
                ),
                new SeatCount(3),
                new ContributionAmount(new BigDecimal("3.00")),
                "Plaza de España, Daimiel",
                "Estación de autobuses, Ciudad Real",
                "Salgo puntual",
                NOW
        );
    }
}