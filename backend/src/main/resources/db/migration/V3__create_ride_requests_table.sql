CREATE TABLE ride_requests (
    id UUID PRIMARY KEY,
    trip_id UUID NOT NULL,
    passenger_id UUID NOT NULL,
    requested_seats INTEGER NOT NULL,
    message VARCHAR(300),
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_ride_requests_trip
        FOREIGN KEY (trip_id)
        REFERENCES trips (id),

    CONSTRAINT fk_ride_requests_passenger
        FOREIGN KEY (passenger_id)
        REFERENCES users (id),

    CONSTRAINT chk_ride_requests_requested_seats
        CHECK (requested_seats > 0),

    CONSTRAINT chk_ride_requests_status
        CHECK (status IN (
            'PENDING',
            'ACCEPTED',
            'REJECTED',
            'CANCELLED'
        ))
);

CREATE INDEX idx_ride_requests_trip_id
    ON ride_requests (trip_id);

CREATE INDEX idx_ride_requests_passenger_id
    ON ride_requests (passenger_id);

CREATE INDEX idx_ride_requests_trip_status
    ON ride_requests (trip_id, status);

CREATE UNIQUE INDEX uq_ride_requests_pending_trip_passenger
    ON ride_requests (trip_id, passenger_id)
    WHERE status = 'PENDING';