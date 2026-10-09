CREATE TABLE storage_account (
    id UUID PRIMARY KEY,
    destination VARCHAR(32) NOT NULL,
    email VARCHAR(320) NOT NULL,
    display_name VARCHAR(255),
    refresh_token_encrypted TEXT NOT NULL,
    status VARCHAR(32) NOT NULL,
    quota_total_bytes BIGINT,
    quota_used_bytes BIGINT,
    last_synced_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_storage_account_destination_email UNIQUE (destination, email)
);
