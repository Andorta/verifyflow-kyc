package com.andorta.verifyflow.verification;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
import java.time.ZoneOffset;

@Repository
public class WebhookEventStore {

    private final JdbcClient jdbcClient;

    public WebhookEventStore(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public boolean register(
            UUID recordId,
            String provider,
            ProviderWebhookEvent event,
            String payloadSha256,
            Instant receivedAt
    ) {
        String normalizedProvider = provider
                .trim()
                .toUpperCase(Locale.ROOT);

        int insertedRows = jdbcClient.sql("""
                        INSERT INTO provider_webhook_events (
                            id,
                            provider,
                            event_id,
                            provider_reference,
                            event_status,
                            payload_sha256,
                            received_at
                        )
                        VALUES (
                            :id,
                            :provider,
                            :eventId,
                            :providerReference,
                            :eventStatus,
                            :payloadSha256,
                            :receivedAt
                        )
                        ON CONFLICT (provider, event_id)
                        DO NOTHING
                        """)
                .param("id", recordId)
                .param("provider", normalizedProvider)
                .param("eventId", event.eventId())
                .param(
                        "providerReference",
                        event.providerReference()
                )
                .param("eventStatus", event.status().name())
                .param("payloadSha256", payloadSha256)
                .param(
        "receivedAt",
        receivedAt.atOffset(ZoneOffset.UTC)
)
                .update();

        return insertedRows == 1;
    }

    public boolean markProcessed(
            String provider,
            String eventId,
            UUID verificationCaseId,
            Instant processedAt
    ) {
        int updatedRows = jdbcClient.sql("""
                        UPDATE provider_webhook_events
                        SET verification_case_id = :caseId,
                            processed_at = :processedAt
                        WHERE provider = :provider
                          AND event_id = :eventId
                          AND processed_at IS NULL
                        """)
                .param(
                        "provider",
                        provider.trim().toUpperCase(Locale.ROOT)
                )
                .param("eventId", eventId)
                .param("caseId", verificationCaseId)
                .param(
        "processedAt",
        processedAt.atOffset(ZoneOffset.UTC)
)
                .update();

        return updatedRows == 1;
    }
}