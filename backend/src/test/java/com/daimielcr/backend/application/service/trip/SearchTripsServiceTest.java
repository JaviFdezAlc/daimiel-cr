package com.daimielcr.backend.application.service.trip;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Captor;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.daimielcr.backend.application.port.in.trip.SearchTripsQuery;
import com.daimielcr.backend.application.port.in.trip.SearchTripsResult;
import com.daimielcr.backend.application.port.in.trip.TripSort;
import com.daimielcr.backend.application.port.out.trip.TripRepositoryPort;
import com.daimielcr.backend.application.port.out.trip.TripSearchCriteria;
import com.daimielcr.backend.application.port.out.trip.TripSearchPage;
import com.daimielcr.backend.domain.model.trip.ContributionAmount;
import com.daimielcr.backend.domain.model.trip.DepartureAt;
import com.daimielcr.backend.domain.model.trip.Route;
import com.daimielcr.backend.domain.model.trip.SeatCount;
import com.daimielcr.backend.domain.model.trip.Trip;
import com.daimielcr.backend.domain.model.trip.TripId;
import com.daimielcr.backend.domain.model.trip.TripLocation;
import com.daimielcr.backend.domain.model.user.UserId;

@ExtendWith(MockitoExtension.class)
class SearchTripsServiceTest {

    private static final Instant NOW =
            Instant.parse("2026-06-26T10:00:00Z");

    private static final ZoneId APPLICATION_ZONE =
            ZoneId.of("Europe/Madrid");

    private static final UserId DRIVER_ID =
            new UserId(UUID.fromString(
                    "11111111-1111-1111-1111-111111111111"
            ));

    @Mock
    private TripRepositoryPort tripRepository;

    @Captor
    private ArgumentCaptor<TripSearchCriteria> criteriaCaptor;

    private SearchTripsService service;

    @BeforeEach
    void setUp() {
        service = new SearchTripsService(
                tripRepository,
                Clock.fixed(NOW, ZoneOffset.UTC),
                APPLICATION_ZONE
        );
    }

    @Test
    void shouldSearchFromNowWithoutDateLimitWhenDateIsNotSpecified() {
        Trip firstTrip = trip(
                "22222222-2222-2222-2222-222222222222",
                Instant.parse("2026-06-26T12:00:00Z"),
                3,
                "3.00"
        );

        Trip secondTrip = trip(
                "33333333-3333-3333-3333-333333333333",
                Instant.parse("2026-06-27T07:30:00Z"),
                2,
                "2.50"
        );

        when(tripRepository.search(any(TripSearchCriteria.class)))
                .thenReturn(new TripSearchPage(
                        List.of(firstTrip, secondTrip),
                        25
                ));

        SearchTripsQuery query = new SearchTripsQuery(
                TripLocation.DAIMIEL,
                TripLocation.CIUDAD_REAL,
                null,
                2,
                TripSort.AVAILABLE_SEATS_DESC,
                1,
                10
        );

        SearchTripsResult result = service.search(query);

        verify(tripRepository).search(criteriaCaptor.capture());

        TripSearchCriteria criteria = criteriaCaptor.getValue();

        assertEquals(TripLocation.DAIMIEL, criteria.origin());
        assertEquals(TripLocation.CIUDAD_REAL, criteria.destination());
        assertEquals(NOW, criteria.departureFromInclusive());
        assertNull(criteria.departureToExclusive());
        assertEquals(2, criteria.requiredSeats());
        assertEquals(TripSort.AVAILABLE_SEATS_DESC, criteria.sort());
        assertEquals(1, criteria.page());
        assertEquals(10, criteria.size());

        assertEquals(2, result.trips().size());
        assertEquals(firstTrip.id(), result.trips().get(0).id());
        assertEquals(secondTrip.id(), result.trips().get(1).id());
        assertEquals(3, result.trips().get(0).availableSeats());
        assertEquals(new BigDecimal("3.00"), result.trips().get(0).contributionAmount());

        assertEquals(1, result.page());
        assertEquals(10, result.size());
        assertEquals(25, result.totalElements());
        assertEquals(3, result.totalPages());
    }

    @Test
    void shouldUseSelectedFutureDayAsSearchWindow() {
        when(tripRepository.search(any(TripSearchCriteria.class)))
                .thenReturn(new TripSearchPage(List.of(), 0));

        LocalDate selectedDate = LocalDate.of(2026, 6, 28);

        SearchTripsQuery query = new SearchTripsQuery(
                TripLocation.DAIMIEL,
                TripLocation.CIUDAD_REAL,
                selectedDate,
                1,
                TripSort.DEPARTURE_ASC,
                0,
                20
        );

        service.search(query);

        verify(tripRepository).search(criteriaCaptor.capture());

        TripSearchCriteria criteria = criteriaCaptor.getValue();

        assertEquals(
                Instant.parse("2026-06-27T22:00:00Z"),
                criteria.departureFromInclusive()
        );
        assertEquals(
                Instant.parse("2026-06-28T22:00:00Z"),
                criteria.departureToExclusive()
        );
        assertTrue(criteria.hasDepartureEnd());
    }

    @Test
    void shouldUseNowAsStartWhenSelectedDateIsToday() {
        when(tripRepository.search(any(TripSearchCriteria.class)))
                .thenReturn(new TripSearchPage(List.of(), 0));

        SearchTripsQuery query = new SearchTripsQuery(
                TripLocation.DAIMIEL,
                TripLocation.CIUDAD_REAL,
                LocalDate.of(2026, 6, 26),
                1,
                TripSort.DEPARTURE_ASC,
                0,
                20
        );

        service.search(query);

        verify(tripRepository).search(criteriaCaptor.capture());

        TripSearchCriteria criteria = criteriaCaptor.getValue();

        assertEquals(NOW, criteria.departureFromInclusive());
        assertEquals(
                Instant.parse("2026-06-26T22:00:00Z"),
                criteria.departureToExclusive()
        );
    }

    @Test
    void shouldReturnEmptyResultWithoutQueryingRepositoryForPastDate() {
        SearchTripsQuery query = new SearchTripsQuery(
                TripLocation.DAIMIEL,
                TripLocation.CIUDAD_REAL,
                LocalDate.of(2026, 6, 25),
                1,
                TripSort.DEPARTURE_ASC,
                2,
                10
        );

        SearchTripsResult result = service.search(query);

        assertTrue(result.isEmpty());
        assertEquals(2, result.page());
        assertEquals(10, result.size());
        assertEquals(0, result.totalElements());
        assertEquals(0, result.totalPages());

        verifyNoInteractions(tripRepository);
    }

    private Trip trip(
            String tripId,
            Instant departureAt,
            int seats,
            String contributionAmount
    ) {
        return Trip.create(
                new TripId(UUID.fromString(tripId)),
                DRIVER_ID,
                new Route(
                        TripLocation.DAIMIEL,
                        TripLocation.CIUDAD_REAL
                ),
                new DepartureAt(departureAt),
                new SeatCount(seats),
                new ContributionAmount(
                        new BigDecimal(contributionAmount)
                ),
                "Plaza de España, Daimiel",
                "Estación de autobuses, Ciudad Real",
                "Salgo puntual",
                NOW
        );
    }
}