CREATE TABLE trips (
    id UUID PRIMARY KEY,
    driver_id UUID NOT NULL,

    origin VARCHAR(20) NOT NULL,
    destination VARCHAR(20) NOT NULL,
    departure_at TIMESTAMPTZ NOT NULL,

    total_seats INTEGER NOT NULL,
    available_seats INTEGER NOT NULL,

    contribution_amount NUMERIC(10, 2) NOT NULL,

    departure_point VARCHAR(120),
    arrival_point VARCHAR(120),
    comment VARCHAR(500),

    status VARCHAR(16) NOT NULL,

    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_trips_driver
        FOREIGN KEY (driver_id)
        REFERENCES users (id)
        ON DELETE RESTRICT,

    CONSTRAINT chk_trips_origin
        CHECK (origin IN ('DAIMIEL', 'CIUDAD_REAL')),

    CONSTRAINT chk_trips_destination
        CHECK (destination IN ('DAIMIEL', 'CIUDAD_REAL')),

    CONSTRAINT chk_trips_different_route_endpoints
        CHECK (origin <> destination),

    CONSTRAINT chk_trips_total_seats_positive
        CHECK (total_seats > 0),

    CONSTRAINT chk_trips_available_seats_range
        CHECK (available_seats BETWEEN 0 AND total_seats),

    CONSTRAINT chk_trips_contribution_non_negative
        CHECK (contribution_amount >= 0),

    CONSTRAINT chk_trips_status
        CHECK (status IN ('ACTIVE', 'FULL', 'CANCELLED', 'FINISHED')),

    CONSTRAINT chk_trips_status_matches_capacity
        CHECK (
            (status = 'ACTIVE' AND available_seats > 0)
            OR (status = 'FULL' AND available_seats = 0)
            OR status IN ('CANCELLED', 'FINISHED')
        )
);

CREATE INDEX idx_trips_driver_id
    ON trips (driver_id);

CREATE INDEX idx_trips_route_departure
    ON trips (origin, destination, departure_at);