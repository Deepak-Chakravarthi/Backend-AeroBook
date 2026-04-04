-- ============================================================
-- CREATE: SEAT INVENTORY (class level)
-- ============================================================
CREATE TABLE seat_inventory (
    id              BIGSERIAL    PRIMARY KEY,
    flight_id       BIGINT       NOT NULL REFERENCES flights(id) ON DELETE CASCADE,
    seat_class      VARCHAR(20)  NOT NULL,
    total_seats     INTEGER      NOT NULL,
    available_seats INTEGER      NOT NULL,
    held_seats      INTEGER      NOT NULL DEFAULT 0,
    booked_seats    INTEGER      NOT NULL DEFAULT 0,
    blocked_seats   INTEGER      NOT NULL DEFAULT 0,
    version         BIGINT       NOT NULL DEFAULT 0,
    updated_at      TIMESTAMP,
    CONSTRAINT uq_seat_inventory_flight_class UNIQUE (flight_id, seat_class)
);

-- ============================================================
-- CREATE: SEATS (individual physical seats)
-- ============================================================
CREATE TABLE seats (
    id                   BIGSERIAL    PRIMARY KEY,
    flight_id            BIGINT       NOT NULL REFERENCES flights(id) ON DELETE CASCADE,
    seat_number          VARCHAR(5)   NOT NULL,
    row_number           INTEGER      NOT NULL,
    seat_letter          VARCHAR(2)   NOT NULL,
    seat_class           VARCHAR(20)  NOT NULL,
    seat_type            VARCHAR(10)  NOT NULL,
    status               VARCHAR(20)  NOT NULL DEFAULT 'AVAILABLE',
    held_by_booking_ref  VARCHAR(50),
    held_until           TIMESTAMP,
    version              BIGINT       NOT NULL DEFAULT 0,
    updated_at           TIMESTAMP,
    CONSTRAINT uq_seats_flight_number UNIQUE (flight_id, seat_number)
);

-- ============================================================
-- INDEXES
-- ============================================================
CREATE INDEX idx_seat_inventory_flight    ON seat_inventory(flight_id);
CREATE INDEX idx_seats_flight_class       ON seats(flight_id, seat_class);
CREATE INDEX idx_seats_status             ON seats(status);
CREATE INDEX idx_seats_booking_ref        ON seats(held_by_booking_ref);
CREATE INDEX idx_seats_held_until         ON seats(held_until);

-- ============================================================
-- SEED: SEAT INVENTORY
-- Populate from existing flight_fares available_seats
-- ============================================================
INSERT INTO seat_inventory (
    flight_id, seat_class, total_seats, available_seats,
    held_seats, booked_seats, blocked_seats, version
)
SELECT
    f.id,
    ff.seat_class,
    ff.available_seats  AS total_seats,
    ff.available_seats  AS available_seats,
    0                   AS held_seats,
    0                   AS booked_seats,
    0                   AS blocked_seats,
    0                   AS version
FROM flights f
JOIN flight_fares ff ON ff.flight_id = f.id;

-- ============================================================
-- SEED: PHYSICAL SEATS
-- Generate seat map for every flight based on inventory
-- ============================================================

-- ── AI-101 on 2026-03-16 ─────────────────────────────────────

-- ECONOMY (180 seats — rows 1-30, 6 seats per row A-F)
INSERT INTO seats (flight_id, seat_number, row_number, seat_letter, seat_class, seat_type, status, version)
SELECT
    f.id,
    r.row_num || l.letter,
    r.row_num,
    l.letter,
    'ECONOMY',
    CASE
        WHEN l.letter IN ('A','F') THEN 'WINDOW'
        WHEN l.letter IN ('B','E') THEN 'AISLE'
        ELSE 'MIDDLE'
    END,
    'AVAILABLE',
    0
FROM flights f,
     generate_series(1, 30) AS r(row_num),
     (VALUES ('A'),('B'),('C'),('D'),('E'),('F')) AS l(letter)
WHERE f.flight_number = 'AI-101'
  AND f.departure_date = '2026-03-16';

-- BUSINESS (30 seats — rows 31-35, 6 seats per row)
INSERT INTO seats (flight_id, seat_number, row_number, seat_letter, seat_class, seat_type, status, version)
SELECT
    f.id,
    r.row_num || l.letter,
    r.row_num,
    l.letter,
    'BUSINESS',
    CASE
        WHEN l.letter IN ('A','F') THEN 'WINDOW'
        WHEN l.letter IN ('B','E') THEN 'AISLE'
        ELSE 'MIDDLE'
    END,
    'AVAILABLE',
    0
FROM flights f,
     generate_series(31, 35) AS r(row_num),
     (VALUES ('A'),('B'),('C'),('D'),('E'),('F')) AS l(letter)
WHERE f.flight_number = 'AI-101'
  AND f.departure_date = '2026-03-16';

-- FIRST (8 seats — rows 36-37, 4 seats per row A-D)
INSERT INTO seats (flight_id, seat_number, row_number, seat_letter, seat_class, seat_type, status, version)
SELECT
    f.id,
    r.row_num || l.letter,
    r.row_num,
    l.letter,
    'FIRST',
    CASE
        WHEN l.letter IN ('A','D') THEN 'WINDOW'
        ELSE 'AISLE'
    END,
    'AVAILABLE',
    0
FROM flights f,
     generate_series(36, 37) AS r(row_num),
     (VALUES ('A'),('B'),('C'),('D')) AS l(letter)
WHERE f.flight_number = 'AI-101'
  AND f.departure_date = '2026-03-16';

-- ── AI-101 on 2026-03-17 ─────────────────────────────────────

INSERT INTO seats (flight_id, seat_number, row_number, seat_letter, seat_class, seat_type, status, version)
SELECT
    f.id,
    r.row_num || l.letter,
    r.row_num,
    l.letter,
    'ECONOMY',
    CASE WHEN l.letter IN ('A','F') THEN 'WINDOW' WHEN l.letter IN ('B','E') THEN 'AISLE' ELSE 'MIDDLE' END,
    'AVAILABLE', 0
FROM flights f,
     generate_series(1, 30) AS r(row_num),
     (VALUES ('A'),('B'),('C'),('D'),('E'),('F')) AS l(letter)
WHERE f.flight_number = 'AI-101' AND f.departure_date = '2026-03-17';

INSERT INTO seats (flight_id, seat_number, row_number, seat_letter, seat_class, seat_type, status, version)
SELECT
    f.id, r.row_num || l.letter, r.row_num, l.letter, 'BUSINESS',
    CASE WHEN l.letter IN ('A','F') THEN 'WINDOW' WHEN l.letter IN ('B','E') THEN 'AISLE' ELSE 'MIDDLE' END,
    'AVAILABLE', 0
FROM flights f,
     generate_series(31, 35) AS r(row_num),
     (VALUES ('A'),('B'),('C'),('D'),('E'),('F')) AS l(letter)
WHERE f.flight_number = 'AI-101' AND f.departure_date = '2026-03-17';

INSERT INTO seats (flight_id, seat_number, row_number, seat_letter, seat_class, seat_type, status, version)
SELECT
    f.id, r.row_num || l.letter, r.row_num, l.letter, 'FIRST',
    CASE WHEN l.letter IN ('A','D') THEN 'WINDOW' ELSE 'AISLE' END,
    'AVAILABLE', 0
FROM flights f,
     generate_series(36, 37) AS r(row_num),
     (VALUES ('A'),('B'),('C'),('D')) AS l(letter)
WHERE f.flight_number = 'AI-101' AND f.departure_date = '2026-03-17';

-- ── AI-101 on 2026-03-18 ─────────────────────────────────────

INSERT INTO seats (flight_id, seat_number, row_number, seat_letter, seat_class, seat_type, status, version)
SELECT
    f.id, r.row_num || l.letter, r.row_num, l.letter, 'ECONOMY',
    CASE WHEN l.letter IN ('A','F') THEN 'WINDOW' WHEN l.letter IN ('B','E') THEN 'AISLE' ELSE 'MIDDLE' END,
    'AVAILABLE', 0
FROM flights f,
     generate_series(1, 30) AS r(row_num),
     (VALUES ('A'),('B'),('C'),('D'),('E'),('F')) AS l(letter)
WHERE f.flight_number = 'AI-101' AND f.departure_date = '2026-03-18';

INSERT INTO seats (flight_id, seat_number, row_number, seat_letter, seat_class, seat_type, status, version)
SELECT
    f.id, r.row_num || l.letter, r.row_num, l.letter, 'BUSINESS',
    CASE WHEN l.letter IN ('A','F') THEN 'WINDOW' WHEN l.letter IN ('B','E') THEN 'AISLE' ELSE 'MIDDLE' END,
    'AVAILABLE', 0
FROM flights f,
     generate_series(31, 35) AS r(row_num),
     (VALUES ('A'),('B'),('C'),('D'),('E'),('F')) AS l(letter)
WHERE f.flight_number = 'AI-101' AND f.departure_date = '2026-03-18';

INSERT INTO seats (flight_id, seat_number, row_number, seat_letter, seat_class, seat_type, status, version)
SELECT
    f.id, r.row_num || l.letter, r.row_num, l.letter, 'FIRST',
    CASE WHEN l.letter IN ('A','D') THEN 'WINDOW' ELSE 'AISLE' END,
    'AVAILABLE', 0
FROM flights f,
     generate_series(36, 37) AS r(row_num),
     (VALUES ('A'),('B'),('C'),('D')) AS l(letter)
WHERE f.flight_number = 'AI-101' AND f.departure_date = '2026-03-18';

-- ── 6E-201 on 2026-03-16 (Economy only — 180 seats) ──────────

INSERT INTO seats (flight_id, seat_number, row_number, seat_letter, seat_class, seat_type, status, version)
SELECT
    f.id, r.row_num || l.letter, r.row_num, l.letter, 'ECONOMY',
    CASE WHEN l.letter IN ('A','F') THEN 'WINDOW' WHEN l.letter IN ('B','E') THEN 'AISLE' ELSE 'MIDDLE' END,
    'AVAILABLE', 0
FROM flights f,
     generate_series(1, 30) AS r(row_num),
     (VALUES ('A'),('B'),('C'),('D'),('E'),('F')) AS l(letter)
WHERE f.flight_number = '6E-201' AND f.departure_date = '2026-03-16';

-- ── 6E-201 on 2026-03-18 ─────────────────────────────────────

INSERT INTO seats (flight_id, seat_number, row_number, seat_letter, seat_class, seat_type, status, version)
SELECT
    f.id, r.row_num || l.letter, r.row_num, l.letter, 'ECONOMY',
    CASE WHEN l.letter IN ('A','F') THEN 'WINDOW' WHEN l.letter IN ('B','E') THEN 'AISLE' ELSE 'MIDDLE' END,
    'AVAILABLE', 0
FROM flights f,
     generate_series(1, 30) AS r(row_num),
     (VALUES ('A'),('B'),('C'),('D'),('E'),('F')) AS l(letter)
WHERE f.flight_number = '6E-201' AND f.departure_date = '2026-03-18';

-- ── UK-301 on 2026-03-16 ─────────────────────────────────────

-- ECONOMY (110 seats — rows 1-19)
INSERT INTO seats (flight_id, seat_number, row_number, seat_letter, seat_class, seat_type, status, version)
SELECT
    f.id, r.row_num || l.letter, r.row_num, l.letter, 'ECONOMY',
    CASE WHEN l.letter IN ('A','F') THEN 'WINDOW' WHEN l.letter IN ('B','E') THEN 'AISLE' ELSE 'MIDDLE' END,
    'AVAILABLE', 0
FROM flights f,
     generate_series(1, 18) AS r(row_num),
     (VALUES ('A'),('B'),('C'),('D'),('E'),('F')) AS l(letter)
WHERE f.flight_number = 'UK-301' AND f.departure_date = '2026-03-16'
UNION ALL
SELECT f.id, '19' || l.letter, 19, l.letter, 'ECONOMY',
    CASE WHEN l.letter IN ('A','D') THEN 'WINDOW' ELSE 'AISLE' END,
    'AVAILABLE', 0
FROM flights f,
     (VALUES ('A'),('B'),('C'),('D')) AS l(letter)
WHERE f.flight_number = 'UK-301' AND f.departure_date = '2026-03-16';

-- BUSINESS (24 seats — rows 20-23, 6 per row)
INSERT INTO seats (flight_id, seat_number, row_number, seat_letter, seat_class, seat_type, status, version)
SELECT
    f.id, r.row_num || l.letter, r.row_num, l.letter, 'BUSINESS',
    CASE WHEN l.letter IN ('A','F') THEN 'WINDOW' WHEN l.letter IN ('B','E') THEN 'AISLE' ELSE 'MIDDLE' END,
    'AVAILABLE', 0
FROM flights f,
     generate_series(20, 23) AS r(row_num),
     (VALUES ('A'),('B'),('C'),('D'),('E'),('F')) AS l(letter)
WHERE f.flight_number = 'UK-301' AND f.departure_date = '2026-03-16';

-- FIRST (8 seats — rows 24-25, 4 per row)
INSERT INTO seats (flight_id, seat_number, row_number, seat_letter, seat_class, seat_type, status, version)
SELECT
    f.id, r.row_num || l.letter, r.row_num, l.letter, 'FIRST',
    CASE WHEN l.letter IN ('A','D') THEN 'WINDOW' ELSE 'AISLE' END,
    'AVAILABLE', 0
FROM flights f,
     generate_series(24, 25) AS r(row_num),
     (VALUES ('A'),('B'),('C'),('D')) AS l(letter)
WHERE f.flight_number = 'UK-301' AND f.departure_date = '2026-03-16';

-- ── UK-301 on 2026-03-17 ─────────────────────────────────────

INSERT INTO seats (flight_id, seat_number, row_number, seat_letter, seat_class, seat_type, status, version)
SELECT f.id, r.row_num || l.letter, r.row_num, l.letter, 'ECONOMY',
    CASE WHEN l.letter IN ('A','F') THEN 'WINDOW' WHEN l.letter IN ('B','E') THEN 'AISLE' ELSE 'MIDDLE' END,
    'AVAILABLE', 0
FROM flights f, generate_series(1, 18) AS r(row_num),
     (VALUES ('A'),('B'),('C'),('D'),('E'),('F')) AS l(letter)
WHERE f.flight_number = 'UK-301' AND f.departure_date = '2026-03-17';

INSERT INTO seats (flight_id, seat_number, row_number, seat_letter, seat_class, seat_type, status, version)
SELECT f.id, r.row_num || l.letter, r.row_num, l.letter, 'BUSINESS',
    CASE WHEN l.letter IN ('A','F') THEN 'WINDOW' WHEN l.letter IN ('B','E') THEN 'AISLE' ELSE 'MIDDLE' END,
    'AVAILABLE', 0
FROM flights f, generate_series(20, 23) AS r(row_num),
     (VALUES ('A'),('B'),('C'),('D'),('E'),('F')) AS l(letter)
WHERE f.flight_number = 'UK-301' AND f.departure_date = '2026-03-17';

INSERT INTO seats (flight_id, seat_number, row_number, seat_letter, seat_class, seat_type, status, version)
SELECT f.id, r.row_num || l.letter, r.row_num, l.letter, 'FIRST',
    CASE WHEN l.letter IN ('A','D') THEN 'WINDOW' ELSE 'AISLE' END,
    'AVAILABLE', 0
FROM flights f, generate_series(24, 25) AS r(row_num),
     (VALUES ('A'),('B'),('C'),('D')) AS l(letter)
WHERE f.flight_number = 'UK-301' AND f.departure_date = '2026-03-17';

-- ── SG-401 on 2026-03-17 ─────────────────────────────────────

-- ECONOMY (165 seats — rows 1-27 full + row 28 with 3 seats)
INSERT INTO seats (flight_id, seat_number, row_number, seat_letter, seat_class, seat_type, status, version)
SELECT f.id, r.row_num || l.letter, r.row_num, l.letter, 'ECONOMY',
    CASE WHEN l.letter IN ('A','F') THEN 'WINDOW' WHEN l.letter IN ('B','E') THEN 'AISLE' ELSE 'MIDDLE' END,
    'AVAILABLE', 0
FROM flights f, generate_series(1, 27) AS r(row_num),
     (VALUES ('A'),('B'),('C'),('D'),('E'),('F')) AS l(letter)
WHERE f.flight_number = 'SG-401' AND f.departure_date = '2026-03-17'
UNION ALL
SELECT f.id, '28' || l.letter, 28, l.letter, 'ECONOMY',
    CASE WHEN l.letter = 'A' THEN 'WINDOW' ELSE 'AISLE' END,
    'AVAILABLE', 0
FROM flights f, (VALUES ('A'),('B'),('C')) AS l(letter)
WHERE f.flight_number = 'SG-401' AND f.departure_date = '2026-03-17';

-- BUSINESS (18 seats — rows 29-31, 6 per row)
INSERT INTO seats (flight_id, seat_number, row_number, seat_letter, seat_class, seat_type, status, version)
SELECT f.id, r.row_num || l.letter, r.row_num, l.letter, 'BUSINESS',
    CASE WHEN l.letter IN ('A','F') THEN 'WINDOW' WHEN l.letter IN ('B','E') THEN 'AISLE' ELSE 'MIDDLE' END,
    'AVAILABLE', 0
FROM flights f, generate_series(29, 31) AS r(row_num),
     (VALUES ('A'),('B'),('C'),('D'),('E'),('F')) AS l(letter)
WHERE f.flight_number = 'SG-401' AND f.departure_date = '2026-03-17';

-- ── SG-401 on 2026-03-19 ─────────────────────────────────────

INSERT INTO seats (flight_id, seat_number, row_number, seat_letter, seat_class, seat_type, status, version)
SELECT f.id, r.row_num || l.letter, r.row_num, l.letter, 'ECONOMY',
    CASE WHEN l.letter IN ('A','F') THEN 'WINDOW' WHEN l.letter IN ('B','E') THEN 'AISLE' ELSE 'MIDDLE' END,
    'AVAILABLE', 0
FROM flights f, generate_series(1, 27) AS r(row_num),
     (VALUES ('A'),('B'),('C'),('D'),('E'),('F')) AS l(letter)
WHERE f.flight_number = 'SG-401' AND f.departure_date = '2026-03-19';

INSERT INTO seats (flight_id, seat_number, row_number, seat_letter, seat_class, seat_type, status, version)
SELECT f.id, r.row_num || l.letter, r.row_num, l.letter, 'BUSINESS',
    CASE WHEN l.letter IN ('A','F') THEN 'WINDOW' WHEN l.letter IN ('B','E') THEN 'AISLE' ELSE 'MIDDLE' END,
    'AVAILABLE', 0
FROM flights f, generate_series(29, 31) AS r(row_num),
     (VALUES ('A'),('B'),('C'),('D'),('E'),('F')) AS l(letter)
WHERE f.flight_number = 'SG-401' AND f.departure_date = '2026-03-19';

-- ── AI-202 on 2026-03-16 ─────────────────────────────────────

-- ECONOMY (250 seats — rows 1-41 full + row 42 with 4 seats)
INSERT INTO seats (flight_id, seat_number, row_number, seat_letter, seat_class, seat_type, status, version)
SELECT f.id, r.row_num || l.letter, r.row_num, l.letter, 'ECONOMY',
    CASE WHEN l.letter IN ('A','F') THEN 'WINDOW' WHEN l.letter IN ('B','E') THEN 'AISLE' ELSE 'MIDDLE' END,
    'AVAILABLE', 0
FROM flights f, generate_series(1, 41) AS r(row_num),
     (VALUES ('A'),('B'),('C'),('D'),('E'),('F')) AS l(letter)
WHERE f.flight_number = 'AI-202' AND f.departure_date = '2026-03-16'
UNION ALL
SELECT f.id, '42' || l.letter, 42, l.letter, 'ECONOMY',
    CASE WHEN l.letter IN ('A','D') THEN 'WINDOW' ELSE 'AISLE' END,
    'AVAILABLE', 0
FROM flights f, (VALUES ('A'),('B'),('C'),('D')) AS l(letter)
WHERE f.flight_number = 'AI-202' AND f.departure_date = '2026-03-16';

-- BUSINESS (35 seats — rows 43-47, 7 per row A-G)
INSERT INTO seats (flight_id, seat_number, row_number, seat_letter, seat_class, seat_type, status, version)
SELECT f.id, r.row_num || l.letter, r.row_num, l.letter, 'BUSINESS',
    CASE WHEN l.letter IN ('A','G') THEN 'WINDOW' WHEN l.letter IN ('B','F') THEN 'AISLE' ELSE 'MIDDLE' END,
    'AVAILABLE', 0
FROM flights f, generate_series(43, 47) AS r(row_num),
     (VALUES ('A'),('B'),('C'),('D'),('E'),('F'),('G')) AS l(letter)
WHERE f.flight_number = 'AI-202' AND f.departure_date = '2026-03-16';

-- FIRST (8 seats — rows 48-49, 4 per row)
INSERT INTO seats (flight_id, seat_number, row_number, seat_letter, seat_class, seat_type, status, version)
SELECT f.id, r.row_num || l.letter, r.row_num, l.letter, 'FIRST',
    CASE WHEN l.letter IN ('A','D') THEN 'WINDOW' ELSE 'AISLE' END,
    'AVAILABLE', 0
FROM flights f, generate_series(48, 49) AS r(row_num),
     (VALUES ('A'),('B'),('C'),('D')) AS l(letter)
WHERE f.flight_number = 'AI-202' AND f.departure_date = '2026-03-16';

-- ── AI-202 on 2026-03-18 ─────────────────────────────────────

INSERT INTO seats (flight_id, seat_number, row_number, seat_letter, seat_class, seat_type, status, version)
SELECT f.id, r.row_num || l.letter, r.row_num, l.letter, 'ECONOMY',
    CASE WHEN l.letter IN ('A','F') THEN 'WINDOW' WHEN l.letter IN ('B','E') THEN 'AISLE' ELSE 'MIDDLE' END,
    'AVAILABLE', 0
FROM flights f, generate_series(1, 41) AS r(row_num),
     (VALUES ('A'),('B'),('C'),('D'),('E'),('F')) AS l(letter)
WHERE f.flight_number = 'AI-202' AND f.departure_date = '2026-03-18';

INSERT INTO seats (flight_id, seat_number, row_number, seat_letter, seat_class, seat_type, status, version)
SELECT f.id, r.row_num || l.letter, r.row_num, l.letter, 'BUSINESS',
    CASE WHEN l.letter IN ('A','G') THEN 'WINDOW' WHEN l.letter IN ('B','F') THEN 'AISLE' ELSE 'MIDDLE' END,
    'AVAILABLE', 0
FROM flights f, generate_series(43, 47) AS r(row_num),
     (VALUES ('A'),('B'),('C'),('D'),('E'),('F'),('G')) AS l(letter)
WHERE f.flight_number = 'AI-202' AND f.departure_date = '2026-03-18';

INSERT INTO seats (flight_id, seat_number, row_number, seat_letter, seat_class, seat_type, status, version)
SELECT f.id, r.row_num || l.letter, r.row_num, l.letter, 'FIRST',
    CASE WHEN l.letter IN ('A','D') THEN 'WINDOW' ELSE 'AISLE' END,
    'AVAILABLE', 0
FROM flights f, generate_series(48, 49) AS r(row_num),
     (VALUES ('A'),('B'),('C'),('D')) AS l(letter)
WHERE f.flight_number = 'AI-202' AND f.departure_date = '2026-03-18';