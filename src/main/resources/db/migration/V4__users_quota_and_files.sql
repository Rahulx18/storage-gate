ALTER TABLE app_user
    ADD COLUMN quota_bytes BIGINT NOT NULL DEFAULT 10737418240,
    ADD COLUMN used_bytes BIGINT NOT NULL DEFAULT 0;

ALTER TABLE storage_account
    ADD COLUMN owner_user_id UUID REFERENCES app_user (id) ON DELETE SET NULL;

ALTER TABLE signup_request
    DROP CONSTRAINT fk_signup_request_decided_by,
    ADD CONSTRAINT fk_signup_request_decided_by FOREIGN KEY (decided_by) REFERENCES app_user (id) ON DELETE SET NULL;

CREATE TABLE storage_file (
    id UUID PRIMARY KEY,
    owner_id UUID NOT NULL REFERENCES app_user (id),
    account_id UUID NOT NULL REFERENCES storage_account (id),
    remote_id VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    content_type VARCHAR(255) NOT NULL,
    size_bytes BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_storage_file_owner ON storage_file (owner_id);
CREATE INDEX idx_storage_file_account ON storage_file (account_id);
