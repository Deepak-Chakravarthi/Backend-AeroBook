-- ============================================================
-- CREATE: BOOKINGS
-- ============================================================
CREATE TABLE bookings (
    id                      BIGSERIAL       PRIMARY KEY,
    pnr                     VARCHAR(20)     NOT NULL UNIQUE,
    booking_type            VARCHAR(20)     NOT NULL,
    status                  VARCHAR(30)     NOT NULL DEFAULT 'PENDING',

    -- booker
    user_id                 BIGINT          NOT NULL REFERENCES users(id),

    -- outbound flight
    outbound_flight_id      BIGINT          NOT NULL REFERENCES flights(id),
    outbound_seat_class     VARCHAR(20)     NOT NULL,
    outbound_seat_number    VARCHAR(5),
    outbound_seat_hold_ref  VARCHAR(50),

    -- return flight
    return_flight_id        BIGINT          REFERENCES flights(id),
    return_seat_class       VARCHAR(20),
    return_seat_number      VARCHAR(5),
    return_seat_hold_ref    VARCHAR(50),

    -- passenger
    passenger_first_name    VARCHAR(50)     NOT NULL,
    passenger_last_name     VARCHAR(50)     NOT NULL,
    passenger_email         VARCHAR(100)    NOT NULL,
    passenger_phone         VARCHAR(20)     NOT NULL,
    passenger_dob           DATE,
    passport_number         VARCHAR(20),
    nationality             VARCHAR(50),

    -- fare
    base_fare               DECIMAL(10,2)   NOT NULL,
    tax                     DECIMAL(10,2)   NOT NULL,
    total_fare              DECIMAL(10,2)   NOT NULL,

    -- cancellation
    cancellation_reason     VARCHAR(30),
    cancellation_remarks    VARCHAR(500),
    cancelled_at            TIMESTAMP,

    -- seat hold TTL
    seat_hold_expires_at    TIMESTAMP,

    created_at              TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP
);

-- ============================================================
-- INDEXES
-- ============================================================
CREATE INDEX idx_bookings_pnr         ON bookings(pnr);
CREATE INDEX idx_bookings_user        ON bookings(user_id);
CREATE INDEX idx_bookings_status      ON bookings(status);
CREATE INDEX idx_bookings_outbound    ON bookings(outbound_flight_id);
CREATE INDEX idx_bookings_return      ON bookings(return_flight_id);
CREATE INDEX idx_bookings_hold_expiry ON bookings(seat_hold_expires_at)
    WHERE status IN ('PENDING', 'SEAT_LOCKED');
CREATE INDEX idx_bookings_created     ON bookings(created_at DESC);