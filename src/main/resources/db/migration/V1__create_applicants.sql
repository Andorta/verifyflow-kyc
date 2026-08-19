CREATE TABLE applicants (
    id UUID PRIMARY KEY,
    external_reference VARCHAR(100) NOT NULL,
    email VARCHAR(254) NOT NULL,
    country_code CHAR(2) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uk_applicants_external_reference
        UNIQUE (external_reference),

    CONSTRAINT chk_applicants_country_code
        CHECK (country_code ~ '^[A-Z]{2}$')
);