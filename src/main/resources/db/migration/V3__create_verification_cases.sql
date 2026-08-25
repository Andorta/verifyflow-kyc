CREATE TABLE verification_cases (
    id UUID PRIMARY KEY,
    applicant_id UUID NOT NULL,
    provider VARCHAR(50) NOT NULL,
    provider_reference VARCHAR(100),
    status VARCHAR(32) NOT NULL,
    submitted_at TIMESTAMPTZ,
    decided_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT fk_verification_cases_applicant
        FOREIGN KEY (applicant_id)
        REFERENCES applicants (id)
        ON DELETE RESTRICT,

    CONSTRAINT uk_verification_cases_provider_reference
        UNIQUE (provider, provider_reference),

    CONSTRAINT chk_verification_cases_status
        CHECK (
            status IN (
                'CREATED',
                'PENDING',
                'IN_REVIEW',
                'APPROVED',
                'REJECTED',
                'MORE_INFO_REQUIRED'
            )
        )
);

CREATE INDEX idx_verification_cases_applicant
    ON verification_cases (applicant_id);

CREATE INDEX idx_verification_cases_status
    ON verification_cases (status);