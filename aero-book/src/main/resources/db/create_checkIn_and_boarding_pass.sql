-- ============================================================
-- CREATE: CHECK-INS
-- ============================================================
CREATE TABLE check_ins (
    id             BIGSERIAL    PRIMARY KEY,
    ticket_id      BIGINT       NOT NULL UNIQUE REFERENCES tickets(id),
    booking_id     BIGINT       NOT NULL REFERENCES bookings(id),
    passenger_id   BIGINT       NOT NULL REFERENCES passengers(id),
    flight_id      BIGINT       NOT NULL REFERENCES flights(id),
    seat_number    VARCHAR(5)   NOT NULL,
    seat_class     VARCHAR(20)  NOT NULL,
    status         VARCHAR(30)  NOT NULL DEFAULT 'NOT_CHECKED_IN',
    checked_in_at  TIMESTAMP,
    boarding_group VARCHAR(5),
    created_at     TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP
);

-- ============================================================
-- CREATE: BOARDING PASSES
-- ============================================================
CREATE TABLE boarding_passes (
    id                   BIGSERIAL    PRIMARY KEY,
    boarding_pass_number VARCHAR(50)  NOT NULL UNIQUE,
    check_in_id          BIGINT       NOT NULL UNIQUE REFERENCES check_ins(id),
    ticket_id            BIGINT       NOT NULL REFERENCES tickets(id),
    passenger_id         BIGINT       NOT NULL REFERENCES passengers(id),
    flight_id            BIGINT       NOT NULL REFERENCES flights(id),
    passenger_name       VARCHAR(100) NOT NULL,
    flight_number        VARCHAR(20)  NOT NULL,
    origin_code          VARCHAR(3)   NOT NULL,
    destination_code     VARCHAR(3)   NOT NULL,
    departure_time       TIMESTAMP    NOT NULL,
    seat_number          VARCHAR(5)   NOT NULL,
    seat_class           VARCHAR(20)  NOT NULL,
    gate                 VARCHAR(10),
    terminal             VARCHAR(10),
    boarding_group       VARCHAR(5),
    boarding_time        TIMESTAMP    NOT NULL,
    barcode              VARCHAR(100) NOT NULL,
    status               VARCHAR(20)  NOT NULL DEFAULT 'ISSUED',
    pdf_path             VARCHAR(500),
    created_at           TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- ============================================================
-- INDEXES
-- ============================================================
CREATE INDEX idx_checkin_ticket     ON check_ins(ticket_id);
CREATE INDEX idx_checkin_booking    ON check_ins(booking_id);
CREATE INDEX idx_checkin_flight     ON check_ins(flight_id);
CREATE INDEX idx_checkin_passenger  ON check_ins(passenger_id);
CREATE INDEX idx_checkin_status     ON check_ins(status);
CREATE INDEX idx_bp_flight          ON boarding_passes(flight_id);
CREATE INDEX idx_bp_passenger       ON boarding_passes(passenger_id);
CREATE INDEX idx_bp_status          ON boarding_passes(status);
CREATE INDEX idx_bp_number          ON boarding_passes(boarding_pass_number);