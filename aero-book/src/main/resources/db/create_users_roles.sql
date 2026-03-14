-- ============================================================
-- ROLES
-- ============================================================
CREATE TABLE roles (
    id   BIGSERIAL    PRIMARY KEY,
    name VARCHAR(30)  NOT NULL UNIQUE
);

-- ============================================================
-- USERS
-- ============================================================
CREATE TABLE users (
    id           BIGSERIAL    PRIMARY KEY,
    username     VARCHAR(50)  NOT NULL UNIQUE,
    email        VARCHAR(100) NOT NULL UNIQUE,
    password     VARCHAR(255) NOT NULL,
    first_name   VARCHAR(50),
    last_name    VARCHAR(50),
    phone_number VARCHAR(20),
    status       VARCHAR(30)  NOT NULL,
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP
);

-- ============================================================
-- USER_ROLES join table
-- ============================================================
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES roles(id),
    PRIMARY KEY (user_id, role_id)
);

CREATE INDEX idx_users_email  ON users(email);
CREATE INDEX idx_users_status ON users(status);

-- ============================================================
-- SEED: ROLES
-- ============================================================
INSERT INTO roles (name) VALUES
('PASSENGER'),
('AGENT'),
('AIRLINE_ADMIN'),
('SUPER_ADMIN');

-- ============================================================
-- SEED: USERS
-- All passwords are BCrypt encoded value of 'Password@123'
-- ============================================================
INSERT INTO users (username, email, password, first_name, last_name, phone_number, status) VALUES
('super_admin',    'superadmin@aerobook.com',   '$2a$12$8nJGMNBbHkRZZSFuO0O4SugF5WtLGCW6JBtRbSoZBnpHfGvSGFXlS', 'Super',   'Admin',   '9000000001', 'ACTIVE'),
('airline_admin',  'airlineadmin@aerobook.com', '$2a$12$8nJGMNBbHkRZZSFuO0O4SugF5WtLGCW6JBtRbSoZBnpHfGvSGFXlS', 'Airline', 'Admin',   '9000000002', 'ACTIVE'),
('agent_one',      'agent1@aerobook.com',       '$2a$12$8nJGMNBbHkRZZSFuO0O4SugF5WtLGCW6JBtRbSoZBnpHfGvSGFXlS', 'Agent',   'One',     '9000000003', 'ACTIVE'),
('agent_two',      'agent2@aerobook.com',       '$2a$12$8nJGMNBbHkRZZSFuO0O4SugF5WtLGCW6JBtRbSoZBnpHfGvSGFXlS', 'Agent',   'Two',     '9000000004', 'ACTIVE'),
('passenger_one',  'passenger1@aerobook.com',   '$2a$12$8nJGMNBbHkRZZSFuO0O4SugF5WtLGCW6JBtRbSoZBnpHfGvSGFXlS', 'Rahul',   'Sharma',  '9000000005', 'ACTIVE'),
('passenger_two',  'passenger2@aerobook.com',   '$2a$12$8nJGMNBbHkRZZSFuO0O4SugF5WtLGCW6JBtRbSoZBnpHfGvSGFXlS', 'Priya',   'Mehta',   '9000000006', 'ACTIVE'),
('passenger_three','passenger3@aerobook.com',   '$2a$12$8nJGMNBbHkRZZSFuO0O4SugF5WtLGCW6JBtRbSoZBnpHfGvSGFXlS', 'Arjun',   'Nair',    '9000000007', 'ACTIVE'),
('suspended_user', 'suspended@aerobook.com',    '$2a$12$8nJGMNBbHkRZZSFuO0O4SugF5WtLGCW6JBtRbSoZBnpHfGvSGFXlS', 'Test',    'Suspended','9000000008','SUSPENDED');

-- ============================================================
-- SEED: USER_ROLES assignments
-- ============================================================
INSERT INTO user_roles (user_id, role_id) VALUES
-- super_admin → SUPER_ADMIN
((SELECT id FROM users WHERE username = 'super_admin'),
 (SELECT id FROM roles WHERE name = 'SUPER_ADMIN')),

-- airline_admin → AIRLINE_ADMIN
((SELECT id FROM users WHERE username = 'airline_admin'),
 (SELECT id FROM roles WHERE name = 'AIRLINE_ADMIN')),

-- agent_one, agent_two → AGENT
((SELECT id FROM users WHERE username = 'agent_one'),
 (SELECT id FROM roles WHERE name = 'AGENT')),

((SELECT id FROM users WHERE username = 'agent_two'),
 (SELECT id FROM roles WHERE name = 'AGENT')),

-- passengers → PASSENGER
((SELECT id FROM users WHERE username = 'passenger_one'),
 (SELECT id FROM roles WHERE name = 'PASSENGER')),

((SELECT id FROM users WHERE username = 'passenger_two'),
 (SELECT id FROM roles WHERE name = 'PASSENGER')),

((SELECT id FROM users WHERE username = 'passenger_three'),
 (SELECT id FROM roles WHERE name = 'PASSENGER')),

-- suspended user → PASSENGER
((SELECT id FROM users WHERE username = 'suspended_user'),
 (SELECT id FROM roles WHERE name = 'PASSENGER'));