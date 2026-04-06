-- ============================================================
-- CREATE: PASSENGERS
-- ============================================================
CREATE TABLE passengers (
    id               BIGSERIAL    PRIMARY KEY,
    booking_id       BIGINT       NOT NULL REFERENCES bookings(id),
    user_id          BIGINT       REFERENCES users(id),
    first_name       VARCHAR(50)  NOT NULL,
    last_name        VARCHAR(50)  NOT NULL,
    gender           VARCHAR(10),
    date_of_birth    DATE,
    passport_number  VARCHAR(20),
    passport_expiry  DATE,
    nationality      VARCHAR(50),
    email            VARCHAR(100),
    phone            VARCHAR(20),
    passenger_type   VARCHAR(10)  NOT NULL DEFAULT 'ADULT',
    created_at       TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- ============================================================
-- CREATE: TICKETS
-- ============================================================
CREATE TABLE tickets (
    id              BIGSERIAL       PRIMARY KEY,
    ticket_number   VARCHAR(50)     NOT NULL UNIQUE,
    passenger_id    BIGINT          NOT NULL REFERENCES passengers(id),
    booking_id      BIGINT          NOT NULL REFERENCES bookings(id),
    flight_id       BIGINT          NOT NULL REFERENCES flights(id),
    seat_class      VARCHAR(20)     NOT NULL,
    seat_number     VARCHAR(5),
    status          VARCHAR(20)     NOT NULL DEFAULT 'ISSUED',
    fare            DECIMAL(10,2)   NOT NULL,
    tax             DECIMAL(10,2)   NOT NULL,
    total_fare      DECIMAL(10,2)   NOT NULL,
    is_return_leg   BOOLEAN         NOT NULL DEFAULT FALSE,
    cancelled_at    TIMESTAMP,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP
);

-- ============================================================
-- INDEXES
-- ============================================================
CREATE INDEX idx_passengers_booking    ON passengers(booking_id);
CREATE INDEX idx_passengers_user       ON passengers(user_id);
CREATE INDEX idx_passengers_passport   ON passengers(passport_number);
CREATE INDEX idx_tickets_booking       ON tickets(booking_id);
CREATE INDEX idx_tickets_passenger     ON tickets(passenger_id);
CREATE INDEX idx_tickets_flight        ON tickets(flight_id);
CREATE INDEX idx_tickets_status        ON tickets(status);
CREATE INDEX idx_tickets_number        ON tickets(ticket_number);