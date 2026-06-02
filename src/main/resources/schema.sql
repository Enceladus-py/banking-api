CREATE SCHEMA IF NOT EXISTS events_schema;

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
