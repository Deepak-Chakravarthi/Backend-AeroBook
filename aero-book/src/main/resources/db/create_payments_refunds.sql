-- ============================================================
-- CREATE: PAYMENTS
-- ============================================================
CREATE TABLE payments (
    id                       BIGSERIAL       PRIMARY KEY,
    idempotency_key          VARCHAR(255)    NOT NULL UNIQUE,
    payment_reference        VARCHAR(50)     UNIQUE,
    booking_id               BIGINT          NOT NULL REFERENCES bookings(id),
    user_id                  BIGINT          NOT NULL REFERENCES users(id),
    status                   VARCHAR(30)     NOT NULL DEFAULT 'INITIATED',
    payment_method           VARCHAR(20)     NOT NULL,
    amount                   DECIMAL(10,2)   NOT NULL,
    currency                 VARCHAR(3)      NOT NULL DEFAULT 'INR',
    gateway_transaction_id   VARCHAR(100),
    gateway_response_code    VARCHAR(20),
    gateway_response_message VARCHAR(500),
    failure_reason           VARCHAR(500),
    paid_at                  TIMESTAMP,
    created_at               TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at               TIMESTAMP
);

-- ============================================================
-- CREATE: REFUNDS
-- ============================================================
CREATE TABLE refunds (
    id               BIGSERIAL       PRIMARY KEY,
    refund_reference VARCHAR(50)     NOT NULL UNIQUE,
    payment_id       BIGINT          NOT NULL REFERENCES payments(id),
    booking_id       BIGINT          NOT NULL REFERENCES bookings(id),
    status           VARCHAR(20)     NOT NULL DEFAULT 'INITIATED',
    reason           VARCHAR(30)     NOT NULL,
    amount           DECIMAL(10,2)   NOT NULL,
    remarks          VARCHAR(500),
    gateway_refund_id VARCHAR(100),
    processed_at     TIMESTAMP,
    created_at       TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP
);

-- ============================================================
-- CREATE: IDEMPOTENCY RECORDS
-- ============================================================
CREATE TABLE idempotency_records (
    id               BIGSERIAL       PRIMARY KEY,
    idempotency_key  VARCHAR(255)    NOT NULL UNIQUE,
    response_body    TEXT            NOT NULL,
    http_status      INTEGER         NOT NULL,
    request_path     VARCHAR(255)    NOT NULL,
    created_at       TIMESTAMP       NOT NULL DEFAULT NOW(),
    expires_at       TIMESTAMP       NOT NULL
);

-- ============================================================
-- INDEXES
-- ============================================================
CREATE INDEX idx_payments_booking        ON payments(booking_id);
CREATE INDEX idx_payments_user           ON payments(user_id);
CREATE INDEX idx_payments_status         ON payments(status);
CREATE INDEX idx_payments_reference      ON payments(payment_reference);
CREATE INDEX idx_payments_idempotency    ON payments(idempotency_key);
CREATE INDEX idx_refunds_payment         ON refunds(payment_id);
CREATE INDEX idx_refunds_booking         ON refunds(booking_id);
CREATE INDEX idx_refunds_status          ON refunds(status);
CREATE INDEX idx_idempotency_key         ON idempotency_records(idempotency_key);
CREATE INDEX idx_idempotency_expires     ON idempotency_records(expires_at);