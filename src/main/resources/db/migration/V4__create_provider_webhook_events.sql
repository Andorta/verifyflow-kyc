CREATE TABLE provider_webhook_events (
    id UUID PRIMARY KEY,
    provider VARCHAR(50) NOT NULL,
    event_id VARCHAR(100) NOT NULL,
    provider_reference VARCHAR(100) NOT NULL,
    event_status VARCHAR(32) NOT NULL,
    payload_sha256 VARCHAR(64) NOT NULL,
    verification_case_id UUID,
    received_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMPTZ,

    CONSTRAINT uk_provider_webhook_events
        UNIQUE (provider, event_id),

    CONSTRAINT fk_webhook_event_verification_case
        FOREIGN KEY (verification_case_id)
        REFERENCES verification_cases (id)
        ON DELETE RESTRICT
);

CREATE INDEX idx_webhook_events_provider_reference
    ON provider_webhook_events (
        provider,
        provider_reference
    );

CREATE INDEX idx_webhook_events_unprocessed
    ON provider_webhook_events (received_at)
    WHERE processed_at IS NULL;