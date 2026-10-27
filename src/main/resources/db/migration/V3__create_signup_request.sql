CREATE TABLE signup_request (
    id UUID PRIMARY KEY,
    email VARCHAR(320) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    status VARCHAR(16) NOT NULL,
    requested_ip VARCHAR(45),
    decided_by UUID,
    decided_ip VARCHAR(45),
    decided_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_signup_request_email UNIQUE (email),
    CONSTRAINT fk_signup_request_decided_by FOREIGN KEY (decided_by) REFERENCES app_user (id)
);
