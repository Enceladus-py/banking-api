CREATE SCHEMA IF NOT EXISTS events_schema;

-- Outbox events table: used by OutboxEventPublisherAdapter to write events atomically
-- with business state changes. OutboxEventScheduler polls this table.
CREATE TABLE IF NOT EXISTS events_schema.outbox_events (
    id           UUID PRIMARY KEY,
    aggregate_type VARCHAR(255) NOT NULL,
    aggregate_id   VARCHAR(255) NOT NULL,
    event_type     VARCHAR(255) NOT NULL,
    payload        TEXT NOT NULL,
    created_at     TIMESTAMP NOT NULL,
    status         VARCHAR(255) NOT NULL DEFAULT 'PENDING',
    retry_count    INT NOT NULL DEFAULT 0
);

-- Processed events table: provides idempotency guard in SpringTransactionEventListener.
-- The PRIMARY KEY constraint on 'id' already enforces uniqueness; the explicit
-- UNIQUE constraint makes the intent clear and survives schema round-trips.
CREATE TABLE IF NOT EXISTS events_schema.processed_events (
    id           UUID PRIMARY KEY,
    processed_at TIMESTAMP NOT NULL,
    CONSTRAINT uq_processed_event_id UNIQUE (id)
);

-- Create transaction_records table if it doesn't exist (primarily for tests / H2 database setup)
CREATE TABLE IF NOT EXISTS transaction_records (
    id UUID PRIMARY KEY,
    source_account_number VARCHAR(255),
    target_account_number VARCHAR(255),
    amount DECIMAL(38,2) NOT NULL,
    type VARCHAR(255) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    status VARCHAR(255) NOT NULL DEFAULT 'PENDING',
    failure_reason VARCHAR(255)
);

-- Ensure transaction_records table has the status and failure_reason columns in case the table existed previously without them
ALTER TABLE transaction_records ADD COLUMN IF NOT EXISTS status VARCHAR(255) DEFAULT 'PENDING';
ALTER TABLE transaction_records ADD COLUMN IF NOT EXISTS failure_reason VARCHAR(255);

-- Ensure account table exists with unique account_number
CREATE TABLE IF NOT EXISTS account (
    id UUID PRIMARY KEY,
    account_number VARCHAR(255) NOT NULL,
    balance DECIMAL(38,2),
    owner_id VARCHAR(255) NOT NULL,
    version BIGINT,
    CONSTRAINT uq_account_number UNIQUE (account_number)
);

-- Available Account Numbers pool
CREATE TABLE IF NOT EXISTS available_account_numbers (
    account_number VARCHAR(255) PRIMARY KEY
);

-- Ensure users table exists
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    surname VARCHAR(255) NOT NULL,
    version BIGINT
);

CREATE SCHEMA IF NOT EXISTS keycloak;
