package com.daimielcr.backend.application.service.trip;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;

import com.daimielcr.backend.application.port.in.trip.SearchTripsQuery;
import com.daimielcr.backend.application.port.in.trip.SearchTripsResult;
import com.daimielcr.backend.application.port.in.trip.SearchTripsUseCase;
import com.daimielcr.backend.application.port.in.trip.TripSummary;
import com.daimielcr.backend.application.port.out.trip.TripRepositoryPort;
import com.daimielcr.backend.application.port.out.trip.TripSearchCriteria;
import com.daimielcr.backend.application.port.out.trip.TripSearchPage;

public class SearchTripsService implements SearchTripsUseCase {

    private final TripRepositoryPort tripRepository;
    private final Clock clock;
    private final ZoneId applicationZoneId;

    public SearchTripsService(
            TripRepositoryPort tripRepository,
            Clock clock,
            ZoneId applicationZoneId
    ) {
        this.tripRepository = Objects.requireNonNull(
                tripRepository,
                "El repositorio de viajes es obligatorio"
        );
        this.clock = Objects.requireNonNull(
                clock,
                "El reloj es obligatorio"
        );
        this.applicationZoneId = Objects.requireNonNull(
                applicationZoneId,
                "La zona horaria de la aplicación es obligatoria"
        );
    }

    @Override
    public SearchTripsResult search(SearchTripsQuery query) {
        Objects.requireNonNull(query, "La consulta de búsqueda es obligatoria");

        Instant now = Instant.now(clock);
        SearchWindow searchWindow = resolveSearchWindow(query.date(), now);

        if (searchWindow == null) {
            return emptyResult(query);
        }

        TripSearchCriteria criteria = new TripSearchCriteria(
                query.origin(),
                query.destination(),
                searchWindow.departureFromInclusive(),
                searchWindow.departureToExclusive(),
                query.requiredSeats(),
                query.sort(),
                query.page(),
                query.size()
        );

        TripSearchPage searchPage = tripRepository.search(criteria);

        List<TripSummary> trips = searchPage.trips()
                .stream()
                .map(TripSummary::from)
                .toList();

        return new SearchTripsResult(
                trips,
                query.page(),
                query.size(),
                searchPage.totalElements(),
                calculateTotalPages(searchPage.totalElements(), query.size())
        );
    }

    private SearchWindow resolveSearchWindow(
            LocalDate date,
            Instant now
    ) {
        if (date == null) {
            return new SearchWindow(now, null);
        }

        Instant startOfSelectedDay = date
                .atStartOfDay(applicationZoneId)
                .toInstant();

        Instant startOfNextDay = date
                .plusDays(1)
                .atStartOfDay(applicationZoneId)
                .toInstant();

        if (!startOfNextDay.isAfter(now)) {
            return null;
        }

        Instant departureFromInclusive = startOfSelectedDay.isAfter(now)
                ? startOfSelectedDay
                : now;

        return new SearchWindow(
                departureFromInclusive,
                startOfNextDay
        );
    }

    private SearchTripsResult emptyResult(SearchTripsQuery query) {
        return new SearchTripsResult(
                List.of(),
                query.page(),
                query.size(),
                0,
                0
        );
    }

    private int calculateTotalPages(long totalElements, int size) {
        if (totalElements == 0) {
            return 0;
        }

        long totalPages = totalElements / size;

        if (totalElements % size != 0) {
            totalPages++;
        }

        return Math.toIntExact(totalPages);
    }

    private record SearchWindow(
            Instant departureFromInclusive,
            Instant departureToExclusive
    ) {
    }
}