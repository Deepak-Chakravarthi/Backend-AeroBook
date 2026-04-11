-- ============================================================
-- CREATE: NOTIFICATIONS
-- ============================================================
CREATE TABLE notifications (
    id             BIGSERIAL    PRIMARY KEY,
    user_id        BIGINT       REFERENCES users(id),
    type           VARCHAR(30)  NOT NULL,
    channel        VARCHAR(10)  NOT NULL DEFAULT 'EMAIL',
    status         VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    recipient      VARCHAR(100) NOT NULL,
    subject        VARCHAR(200),
    content        TEXT,
    reference_id   VARCHAR(100),
    retry_count    INTEGER      NOT NULL DEFAULT 0,
    failure_reason VARCHAR(500),
    sent_at        TIMESTAMP,
    created_at     TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- ============================================================
-- INDEXES
-- ============================================================
CREATE INDEX idx_notifications_user    ON notifications(user_id);
CREATE INDEX idx_notifications_type    ON notifications(type);
CREATE INDEX idx_notifications_status  ON notifications(status);
CREATE INDEX idx_notifications_created ON notifications(created_at DESC);