package com.daimielcr.backend.adapter.out.persistence.trip;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import com.daimielcr.backend.application.port.out.trip.TripRepositoryPort;
import com.daimielcr.backend.domain.model.trip.ContributionAmount;
import com.daimielcr.backend.domain.model.trip.DepartureAt;
import com.daimielcr.backend.domain.model.trip.Route;
import com.daimielcr.backend.domain.model.trip.SeatCount;
import com.daimielcr.backend.domain.model.trip.Trip;
import com.daimielcr.backend.domain.model.trip.TripId;
import com.daimielcr.backend.domain.model.trip.TripLocation;
import com.daimielcr.backend.domain.model.user.UserId;

@SpringBootTest
@Testcontainers
class TripOptimisticLockingIntegrationTest {

    private static final Instant NOW = Instant.parse("2026-06-29T17:00:00Z");

    private static final TripId TRIP_ID = new TripId(UUID.fromString(
            "11111111-1111-1111-1111-111111111111"));

    private static final UserId DRIVER_ID = new UserId(UUID.fromString(
            "22222222-2222-2222-2222-222222222222"));

    @Container
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16-alpine")
            .withDatabaseName("daimiel_cr_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(
            DynamicPropertyRegistry registry) {
        registry.add(
                "spring.datasource.url",
                POSTGRES::getJdbcUrl);
        registry.add(
                "spring.datasource.username",
                POSTGRES::getUsername);
        registry.add(
                "spring.datasource.password",
                POSTGRES::getPassword);
        registry.add(
                "spring.datasource.driver-class-name",
                POSTGRES::getDriverClassName);

        // Evita que Spring intente levantar el docker-compose local.
        registry.add(
                "spring.docker.compose.enabled",
                () -> "false");
    }

    @Autowired
    private TripRepositoryPort tripRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute(
                "TRUNCATE TABLE ride_requests, trips, users CASCADE");

        jdbcTemplate.update("""
                INSERT INTO users (
                    id,
                    phone_number,
                    display_name,
                    phone_verification_status,
                    role,
                    created_at,
                    updated_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """,
                DRIVER_ID.value(),
                "+34600000001",
                "Conductor de prueba",
                "VERIFIED",
                "USER",
                Timestamp.from(NOW),
                Timestamp.from(NOW));
    }

    @Test
    void shouldRejectStaleTripUpdateWithOptimisticLocking() {
        Trip trip = Trip.create(
                TRIP_ID,
                DRIVER_ID,
                new Route(
                        TripLocation.DAIMIEL,
                        TripLocation.CIUDAD_REAL),
                new DepartureAt(
                        Instant.parse("2030-07-10T07:30:00Z")),
                new SeatCount(3),
                new ContributionAmount(new BigDecimal("3.00")),
                "Plaza de España, Daimiel",
                "Estación de autobuses, Ciudad Real",
                "Prueba de concurrencia",
                NOW);

        tripRepository.save(trip);

        Trip firstCopy = tripRepository.findById(TRIP_ID)
                .orElseThrow();

        Trip staleCopy = tripRepository.findById(TRIP_ID)
                .orElseThrow();

        firstCopy.reserveSeats(new SeatCount(1), NOW);
        tripRepository.save(firstCopy);

        staleCopy.reserveSeats(new SeatCount(1), NOW);

        assertThrows(
                OptimisticLockingFailureException.class,
                () -> tripRepository.save(staleCopy));

        Trip persistedTrip = tripRepository.findById(TRIP_ID)
                .orElseThrow();

        assertEquals(2, persistedTrip.availableSeats().value());
    }
}