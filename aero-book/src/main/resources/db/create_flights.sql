-- ============================================================
-- CREATE: FLIGHT SCHEDULES
-- ============================================================
CREATE TABLE flight_schedules (
    id                    BIGSERIAL    PRIMARY KEY,
    flight_number_prefix  VARCHAR(20)  NOT NULL,
    airline_id            BIGINT       NOT NULL REFERENCES airlines(id),
    aircraft_id           BIGINT       NOT NULL REFERENCES aircraft(id),
    route_id              BIGINT       NOT NULL REFERENCES routes(id),
    departure_time        TIME         NOT NULL,
    arrival_time          TIME         NOT NULL,
    duration_minutes      INTEGER      NOT NULL,
    valid_from            DATE         NOT NULL,
    valid_until           DATE,
    active                BOOLEAN      NOT NULL DEFAULT TRUE,
    terminal              VARCHAR(10),
    gate                  VARCHAR(10),
    created_at            TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMP
);

-- ============================================================
-- CREATE: SCHEDULE DAYS
-- ============================================================
CREATE TABLE schedule_days (
    schedule_id  BIGINT      NOT NULL REFERENCES flight_schedules(id) ON DELETE CASCADE,
    day_of_week  VARCHAR(10) NOT NULL
);

-- ============================================================
-- CREATE: FLIGHTS
-- ============================================================
CREATE TABLE flights (
    id               BIGSERIAL    PRIMARY KEY,
    flight_number    VARCHAR(20)  NOT NULL,
    airline_id       BIGINT       NOT NULL REFERENCES airlines(id),
    aircraft_id      BIGINT       NOT NULL REFERENCES aircraft(id),
    route_id         BIGINT       NOT NULL REFERENCES routes(id),
    schedule_id      BIGINT       REFERENCES flight_schedules(id),
    departure_date   DATE         NOT NULL,
    departure_time   TIMESTAMP    NOT NULL,
    arrival_time     TIMESTAMP    NOT NULL,
    duration_minutes INTEGER      NOT NULL,
    delay_minutes    INTEGER,
    status           VARCHAR(20)  NOT NULL DEFAULT 'SCHEDULED',
    gate             VARCHAR(10),
    terminal         VARCHAR(10),
    created_at       TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP,
    CONSTRAINT uq_flights_number_date UNIQUE (flight_number, departure_date)
);

-- ============================================================
-- CREATE: FLIGHT FARES
-- ============================================================
CREATE TABLE flight_fares (
    id              BIGSERIAL       PRIMARY KEY,
    flight_id       BIGINT          NOT NULL REFERENCES flights(id) ON DELETE CASCADE,
    seat_class      VARCHAR(20)     NOT NULL,
    base_fare       DECIMAL(10,2)   NOT NULL,
    tax             DECIMAL(10,2)   NOT NULL,
    total_fare      DECIMAL(10,2)   NOT NULL,
    available_seats INTEGER         NOT NULL,
    CONSTRAINT uq_flight_fares_class UNIQUE (flight_id, seat_class)
);

-- ============================================================
-- INDEXES
-- ============================================================
CREATE INDEX idx_flights_number         ON flights(flight_number);
CREATE INDEX idx_flights_departure_date ON flights(departure_date);
CREATE INDEX idx_flights_status         ON flights(status);
CREATE INDEX idx_flights_route          ON flights(route_id);
CREATE INDEX idx_flights_airline        ON flights(airline_id);

-- ============================================================
-- SEED: FLIGHT SCHEDULES
-- ============================================================
INSERT INTO flight_schedules (
    flight_number_prefix, airline_id, aircraft_id, route_id,
    departure_time, arrival_time, duration_minutes,
    valid_from, valid_until, active, terminal, gate
) VALUES

-- AI-101: DEL → BOM (Air India, Boeing 787-8)
('AI-101',
 (SELECT id FROM airlines WHERE iata_code = 'AI'),
 (SELECT id FROM aircraft WHERE registration_number = 'VT-ANB'),
 (SELECT id FROM routes
  WHERE origin_airport_id      = (SELECT id FROM airports WHERE iata_code = 'DEL')
    AND destination_airport_id = (SELECT id FROM airports WHERE iata_code = 'BOM')),
 '06:00', '08:00', 120, '2026-01-01', '2026-12-31', TRUE, 'T3', 'G12'),

-- 6E-201: BOM → BLR (IndiGo, A320neo)
('6E-201',
 (SELECT id FROM airlines WHERE iata_code = '6E'),
 (SELECT id FROM aircraft WHERE registration_number = 'VT-IZA'),
 (SELECT id FROM routes
  WHERE origin_airport_id      = (SELECT id FROM airports WHERE iata_code = 'BOM')
    AND destination_airport_id = (SELECT id FROM airports WHERE iata_code = 'BLR')),
 '09:30', '11:05', 95, '2026-01-01', '2026-12-31', TRUE, 'T1', 'B4'),

-- UK-301: DEL → HYD (Vistara, A320neo)
('UK-301',
 (SELECT id FROM airlines WHERE iata_code = 'UK'),
 (SELECT id FROM aircraft WHERE registration_number = 'VT-TVA'),
 (SELECT id FROM routes
  WHERE origin_airport_id      = (SELECT id FROM airports WHERE iata_code = 'DEL')
    AND destination_airport_id = (SELECT id FROM airports WHERE iata_code = 'HYD')),
 '14:00', '16:10', 130, '2026-01-01', '2026-12-31', TRUE, 'T2', 'D8'),

-- SG-401: MAA → DEL (SpiceJet, Boeing 737)
('SG-401',
 (SELECT id FROM airlines WHERE iata_code = 'SG'),
 (SELECT id FROM aircraft WHERE registration_number = 'VT-SGA'),
 (SELECT id FROM routes
  WHERE origin_airport_id      = (SELECT id FROM airports WHERE iata_code = 'MAA')
    AND destination_airport_id = (SELECT id FROM airports WHERE iata_code = 'DEL')),
 '07:30', '10:25', 175, '2026-01-01', '2026-12-31', TRUE, 'T1', 'A3'),

-- AI-202: BLR → CCU (Air India, Boeing 777)
('AI-202',
 (SELECT id FROM airlines WHERE iata_code = 'AI'),
 (SELECT id FROM aircraft WHERE registration_number = 'VT-ANC'),
 (SELECT id FROM routes
  WHERE origin_airport_id      = (SELECT id FROM airports WHERE iata_code = 'BLR')
    AND destination_airport_id = (SELECT id FROM airports WHERE iata_code = 'BOM')),
 '11:00', '12:35', 95, '2026-01-01', '2026-12-31', TRUE, 'T3', 'H5');

-- ============================================================
-- SEED: SCHEDULE DAYS
-- ============================================================

-- AI-101: Mon–Sat
INSERT INTO schedule_days (schedule_id, day_of_week)
SELECT s.id, d.day
FROM flight_schedules s,
     (VALUES ('MONDAY'),('TUESDAY'),('WEDNESDAY'),('THURSDAY'),('FRIDAY'),('SATURDAY')) AS d(day)
WHERE s.flight_number_prefix = 'AI-101';

-- 6E-201: Mon/Wed/Fri/Sat
INSERT INTO schedule_days (schedule_id, day_of_week)
SELECT s.id, d.day
FROM flight_schedules s,
     (VALUES ('MONDAY'),('WEDNESDAY'),('FRIDAY'),('SATURDAY')) AS d(day)
WHERE s.flight_number_prefix = '6E-201';

-- UK-301: Daily
INSERT INTO schedule_days (schedule_id, day_of_week)
SELECT s.id, d.day
FROM flight_schedules s,
     (VALUES ('MONDAY'),('TUESDAY'),('WEDNESDAY'),('THURSDAY'),('FRIDAY'),('SATURDAY'),('SUNDAY')) AS d(day)
WHERE s.flight_number_prefix = 'UK-301';

-- SG-401: Tue/Thu/Sat/Sun
INSERT INTO schedule_days (schedule_id, day_of_week)
SELECT s.id, d.day
FROM flight_schedules s,
     (VALUES ('TUESDAY'),('THURSDAY'),('SATURDAY'),('SUNDAY')) AS d(day)
WHERE s.flight_number_prefix = 'SG-401';

-- AI-202: Mon/Wed/Fri
INSERT INTO schedule_days (schedule_id, day_of_week)
SELECT s.id, d.day
FROM flight_schedules s,
     (VALUES ('MONDAY'),('WEDNESDAY'),('FRIDAY')) AS d(day)
WHERE s.flight_number_prefix = 'AI-202';

-- ============================================================
-- SEED: FLIGHTS (March & April 2026 instances)
-- ============================================================
INSERT INTO flights (
    flight_number, airline_id, aircraft_id, route_id,
    departure_date, departure_time, arrival_time,
    duration_minutes, status, terminal, gate
) VALUES

-- AI-101 DEL→BOM
('AI-101',
 (SELECT id FROM airlines WHERE iata_code = 'AI'),
 (SELECT id FROM aircraft WHERE registration_number = 'VT-ANB'),
 (SELECT id FROM routes WHERE origin_airport_id = (SELECT id FROM airports WHERE iata_code = 'DEL') AND destination_airport_id = (SELECT id FROM airports WHERE iata_code = 'BOM')),
 '2026-03-16', '2026-03-16 06:00:00', '2026-03-16 08:00:00', 120, 'SCHEDULED', 'T3', 'G12'),

('AI-101',
 (SELECT id FROM airlines WHERE iata_code = 'AI'),
 (SELECT id FROM aircraft WHERE registration_number = 'VT-ANB'),
 (SELECT id FROM routes WHERE origin_airport_id = (SELECT id FROM airports WHERE iata_code = 'DEL') AND destination_airport_id = (SELECT id FROM airports WHERE iata_code = 'BOM')),
 '2026-03-17', '2026-03-17 06:00:00', '2026-03-17 08:00:00', 120, 'SCHEDULED', 'T3', 'G12'),

('AI-101',
 (SELECT id FROM airlines WHERE iata_code = 'AI'),
 (SELECT id FROM aircraft WHERE registration_number = 'VT-ANB'),
 (SELECT id FROM routes WHERE origin_airport_id = (SELECT id FROM airports WHERE iata_code = 'DEL') AND destination_airport_id = (SELECT id FROM airports WHERE iata_code = 'BOM')),
 '2026-03-18', '2026-03-18 06:00:00', '2026-03-18 08:00:00', 120, 'SCHEDULED', 'T3', 'G12'),

('AI-101',
 (SELECT id FROM airlines WHERE iata_code = 'AI'),
 (SELECT id FROM aircraft WHERE registration_number = 'VT-ANB'),
 (SELECT id FROM routes WHERE origin_airport_id = (SELECT id FROM airports WHERE iata_code = 'DEL') AND destination_airport_id = (SELECT id FROM airports WHERE iata_code = 'BOM')),
 '2026-03-20', '2026-03-20 06:00:00', '2026-03-20 08:00:00', 120, 'DELAYED',   'T3', 'G12'),

-- 6E-201 BOM→BLR
('6E-201',
 (SELECT id FROM airlines WHERE iata_code = '6E'),
 (SELECT id FROM aircraft WHERE registration_number = 'VT-IZA'),
 (SELECT id FROM routes WHERE origin_airport_id = (SELECT id FROM airports WHERE iata_code = 'BOM') AND destination_airport_id = (SELECT id FROM airports WHERE iata_code = 'BLR')),
 '2026-03-16', '2026-03-16 09:30:00', '2026-03-16 11:05:00', 95, 'SCHEDULED', 'T1', 'B4'),

('6E-201',
 (SELECT id FROM airlines WHERE iata_code = '6E'),
 (SELECT id FROM aircraft WHERE registration_number = 'VT-IZA'),
 (SELECT id FROM routes WHERE origin_airport_id = (SELECT id FROM airports WHERE iata_code = 'BOM') AND destination_airport_id = (SELECT id FROM airports WHERE iata_code = 'BLR')),
 '2026-03-18', '2026-03-18 09:30:00', '2026-03-18 11:05:00', 95, 'SCHEDULED', 'T1', 'B4'),

-- UK-301 DEL→HYD
('UK-301',
 (SELECT id FROM airlines WHERE iata_code = 'UK'),
 (SELECT id FROM aircraft WHERE registration_number = 'VT-TVA'),
 (SELECT id FROM routes WHERE origin_airport_id = (SELECT id FROM airports WHERE iata_code = 'DEL') AND destination_airport_id = (SELECT id FROM airports WHERE iata_code = 'HYD')),
 '2026-03-16', '2026-03-16 14:00:00', '2026-03-16 16:10:00', 130, 'SCHEDULED', 'T2', 'D8'),

('UK-301',
 (SELECT id FROM airlines WHERE iata_code = 'UK'),
 (SELECT id FROM aircraft WHERE registration_number = 'VT-TVA'),
 (SELECT id FROM routes WHERE origin_airport_id = (SELECT id FROM airports WHERE iata_code = 'DEL') AND destination_airport_id = (SELECT id FROM airports WHERE iata_code = 'HYD')),
 '2026-03-17', '2026-03-17 14:00:00', '2026-03-17 16:10:00', 130, 'SCHEDULED', 'T2', 'D8'),

('UK-301',
 (SELECT id FROM airlines WHERE iata_code = 'UK'),
 (SELECT id FROM aircraft WHERE registration_number = 'VT-TVA'),
 (SELECT id FROM routes WHERE origin_airport_id = (SELECT id FROM airports WHERE iata_code = 'DEL') AND destination_airport_id = (SELECT id FROM airports WHERE iata_code = 'HYD')),
 '2026-03-19', '2026-03-19 14:00:00', '2026-03-19 16:10:00', 130, 'CANCELLED', 'T2', 'D8'),

-- SG-401 MAA→DEL
('SG-401',
 (SELECT id FROM airlines WHERE iata_code = 'SG'),
 (SELECT id FROM aircraft WHERE registration_number = 'VT-SGA'),
 (SELECT id FROM routes WHERE origin_airport_id = (SELECT id FROM airports WHERE iata_code = 'MAA') AND destination_airport_id = (SELECT id FROM airports WHERE iata_code = 'DEL')),
 '2026-03-17', '2026-03-17 07:30:00', '2026-03-17 10:25:00', 175, 'SCHEDULED', 'T1', 'A3'),

('SG-401',
 (SELECT id FROM airlines WHERE iata_code = 'SG'),
 (SELECT id FROM aircraft WHERE registration_number = 'VT-SGA'),
 (SELECT id FROM routes WHERE origin_airport_id = (SELECT id FROM airports WHERE iata_code = 'MAA') AND destination_airport_id = (SELECT id FROM airports WHERE iata_code = 'DEL')),
 '2026-03-19', '2026-03-19 07:30:00', '2026-03-19 10:25:00', 175, 'SCHEDULED', 'T1', 'A3'),

-- AI-202 BLR→BOM
('AI-202',
 (SELECT id FROM airlines WHERE iata_code = 'AI'),
 (SELECT id FROM aircraft WHERE registration_number = 'VT-ANC'),
 (SELECT id FROM routes WHERE origin_airport_id = (SELECT id FROM airports WHERE iata_code = 'BLR') AND destination_airport_id = (SELECT id FROM airports WHERE iata_code = 'BOM')),
 '2026-03-16', '2026-03-16 11:00:00', '2026-03-16 12:35:00', 95, 'SCHEDULED', 'T3', 'H5'),

('AI-202',
 (SELECT id FROM airlines WHERE iata_code = 'AI'),
 (SELECT id FROM aircraft WHERE registration_number = 'VT-ANC'),
 (SELECT id FROM routes WHERE origin_airport_id = (SELECT id FROM airports WHERE iata_code = 'BLR') AND destination_airport_id = (SELECT id FROM airports WHERE iata_code = 'BOM')),
 '2026-03-18', '2026-03-18 11:00:00', '2026-03-18 12:35:00', 95, 'SCHEDULED', 'T3', 'H5');

-- ============================================================
-- SEED: FLIGHT FARES
-- ============================================================

-- AI-101 fares (all dates)
INSERT INTO flight_fares (flight_id, seat_class, base_fare, tax, total_fare, available_seats)
SELECT f.id, d.seat_class, d.base_fare, d.tax, d.total_fare, d.available_seats
FROM flights f,
     (VALUES
         ('ECONOMY',  4500.00,  675.00,  5175.00, 180),
         ('BUSINESS', 12000.00, 1800.00, 13800.00, 30),
         ('FIRST',    25000.00, 3750.00, 28750.00, 8)
     ) AS d(seat_class, base_fare, tax, total_fare, available_seats)
WHERE f.flight_number = 'AI-101';

-- 6E-201 fares (economy only)
INSERT INTO flight_fares (flight_id, seat_class, base_fare, tax, total_fare, available_seats)
SELECT f.id, 'ECONOMY', 2800.00, 420.00, 3220.00, 180
FROM flights f
WHERE f.flight_number = '6E-201';

-- UK-301 fares (all dates)
INSERT INTO flight_fares (flight_id, seat_class, base_fare, tax, total_fare, available_seats)
SELECT f.id, d.seat_class, d.base_fare, d.tax, d.total_fare, d.available_seats
FROM flights f,
     (VALUES
         ('ECONOMY',  5200.00,  780.00,  5980.00,  110),
         ('BUSINESS', 14000.00, 2100.00, 16100.00, 24),
         ('FIRST',    28000.00, 4200.00, 32200.00, 8)
     ) AS d(seat_class, base_fare, tax, total_fare, available_seats)
WHERE f.flight_number = 'UK-301';

-- SG-401 fares
INSERT INTO flight_fares (flight_id, seat_class, base_fare, tax, total_fare, available_seats)
SELECT f.id, d.seat_class, d.base_fare, d.tax, d.total_fare, d.available_seats
FROM flights f,
     (VALUES
         ('ECONOMY',  3500.00, 525.00,  4025.00, 165),
         ('BUSINESS', 9500.00, 1425.00, 10925.00, 18)
     ) AS d(seat_class, base_fare, tax, total_fare, available_seats)
WHERE f.flight_number = 'SG-401';

-- AI-202 fares
INSERT INTO flight_fares (flight_id, seat_class, base_fare, tax, total_fare, available_seats)
SELECT f.id, d.seat_class, d.base_fare, d.tax, d.total_fare, d.available_seats
FROM flights f,
     (VALUES
         ('ECONOMY',  4200.00,  630.00,  4830.00,  250),
         ('BUSINESS', 11000.00, 1650.00, 12650.00, 35),
         ('FIRST',    22000.00, 3300.00, 25300.00, 8)
     ) AS d(seat_class, base_fare, tax, total_fare, available_seats)
WHERE f.flight_number = 'AI-202';