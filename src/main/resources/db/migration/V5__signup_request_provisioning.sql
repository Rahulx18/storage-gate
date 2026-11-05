ALTER TABLE signup_request
    ADD COLUMN permission VARCHAR(32),
    ADD COLUMN provisioned_at TIMESTAMPTZ;

UPDATE signup_request SET provisioned_at = COALESCE(decided_at, now()) WHERE status = 'APPROVED';
