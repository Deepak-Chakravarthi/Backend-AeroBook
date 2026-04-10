-- ============================================================
-- CREATE: LOYALTY ACCOUNTS
-- ============================================================
CREATE TABLE loyalty_accounts (
    id                    BIGSERIAL    PRIMARY KEY,
    user_id               BIGINT       NOT NULL UNIQUE REFERENCES users(id),
    membership_number     VARCHAR(20)  NOT NULL UNIQUE,
    tier                  VARCHAR(20)  NOT NULL DEFAULT 'BLUE',
    total_miles           BIGINT       NOT NULL DEFAULT 0,
    available_miles       BIGINT       NOT NULL DEFAULT 0,
    tier_qualifying_miles BIGINT       NOT NULL DEFAULT 0,
    flights_completed     INTEGER      NOT NULL DEFAULT 0,
    tier_upgraded_at      TIMESTAMP,
    last_activity_at      TIMESTAMP,
    created_at            TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMP
);

-- ============================================================
-- CREATE: MILE TRANSACTIONS
-- ============================================================
CREATE TABLE mile_transactions (
    id                  BIGSERIAL    PRIMARY KEY,
    loyalty_account_id  BIGINT       NOT NULL REFERENCES loyalty_accounts(id),
    type                VARCHAR(20)  NOT NULL,
    miles               BIGINT       NOT NULL,
    description         VARCHAR(500) NOT NULL,
    reference_id        VARCHAR(100),
    balance_after       BIGINT       NOT NULL,
    created_at          TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- ============================================================
-- INDEXES
-- ============================================================
CREATE INDEX idx_loyalty_user          ON loyalty_accounts(user_id);
CREATE INDEX idx_loyalty_tier          ON loyalty_accounts(tier);
CREATE INDEX idx_loyalty_membership    ON loyalty_accounts(membership_number);
CREATE INDEX idx_mile_txn_account      ON mile_transactions(loyalty_account_id);
CREATE INDEX idx_mile_txn_type         ON mile_transactions(type);
CREATE INDEX idx_mile_txn_created      ON mile_transactions(created_at DESC);

-- ============================================================
-- SEED: LOYALTY ACCOUNTS for existing users
-- ============================================================
INSERT INTO loyalty_accounts (
    user_id, membership_number, tier,
    total_miles, available_miles, tier_qualifying_miles,
    flights_completed, last_activity_at
)
SELECT
    u.id,
    'AERO-FF-' || UPPER(SUBSTRING(MD5(u.id::TEXT), 1, 6)),
    'BLUE',
    0, 0, 0, 0,
    NOW()
FROM users u
WHERE u.status = 'ACTIVE';