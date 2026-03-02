CREATE TABLE airlines (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    iata_code   VARCHAR(3)   NOT NULL UNIQUE,
    icao_code   VARCHAR(4),
    logo_url    VARCHAR(500),
    country     VARCHAR(100),
    status      VARCHAR(20)  NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP
);

CREATE TABLE aircraft (
    id                    BIGSERIAL PRIMARY KEY,
    registration_number   VARCHAR(20)  NOT NULL UNIQUE,
    model                 VARCHAR(100) NOT NULL,
    manufacturer          VARCHAR(100) NOT NULL,
    total_seats           INTEGER      NOT NULL,
    status                VARCHAR(30)  NOT NULL,
    airline_id            BIGINT       NOT NULL REFERENCES airlines(id),
    created_at            TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMP
);

CREATE TABLE aircraft_seat_config (
    id            BIGSERIAL PRIMARY KEY,
    aircraft_id   BIGINT      NOT NULL REFERENCES aircraft(id) ON DELETE CASCADE,
    seat_class    VARCHAR(20) NOT NULL,
    seat_count    INTEGER     NOT NULL,
    rows          INTEGER,
    seats_per_row INTEGER,
    UNIQUE (aircraft_id, seat_class)
);

CREATE TABLE airports (
    id         BIGSERIAL PRIMARY KEY,
    iata_code  VARCHAR(3)   NOT NULL UNIQUE,
    name       VARCHAR(200) NOT NULL,
    city       VARCHAR(100) NOT NULL,
    country    VARCHAR(100) NOT NULL,
    timezone   VARCHAR(50)  NOT NULL,
    latitude   DECIMAL(9,6),
    longitude  DECIMAL(9,6),
    created_at TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE routes (
    id                          BIGSERIAL PRIMARY KEY,
    origin_airport_id           BIGINT      NOT NULL REFERENCES airports(id),
    destination_airport_id      BIGINT      NOT NULL REFERENCES airports(id),
    distance_km                 INTEGER,
    estimated_duration_minutes  INTEGER,
    status                      VARCHAR(20) NOT NULL,
    created_at                  TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at                  TIMESTAMP,
    UNIQUE (origin_airport_id, destination_airport_id)
);

-- Indexes for common query patterns
CREATE INDEX idx_aircraft_airline_id ON aircraft(airline_id);
CREATE INDEX idx_routes_origin ON routes(origin_airport_id);
CREATE INDEX idx_routes_destination ON routes(destination_airport_id);
CREATE INDEX idx_routes_status ON routes(status);