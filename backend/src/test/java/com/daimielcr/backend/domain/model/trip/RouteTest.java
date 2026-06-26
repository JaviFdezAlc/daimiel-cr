package com.daimielcr.backend.domain.model.trip;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import com.daimielcr.backend.domain.exceptions.InvalidTripException;

class RouteTest {

    @Test
    void shouldCreateRouteBetweenDaimielAndCiudadReal() {
        Route route = new Route(
                TripLocation.DAIMIEL,
                TripLocation.CIUDAD_REAL
        );

        assertEquals(TripLocation.DAIMIEL, route.origin());
        assertEquals(TripLocation.CIUDAD_REAL, route.destination());
    }

    @Test
    void shouldRejectRouteWithSameOriginAndDestination() {
        assertThrows(
                InvalidTripException.class,
                () -> new Route(
                        TripLocation.DAIMIEL,
                        TripLocation.DAIMIEL
                )
        );
    }
}
