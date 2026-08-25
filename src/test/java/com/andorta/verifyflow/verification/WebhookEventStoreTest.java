package com.andorta.verifyflow.verification;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
@Import(WebhookEventStore.class)
class WebhookEventStoreTest {

    @Autowired
    private WebhookEventStore webhookEventStore;

    @Autowired
    private JdbcClient jdbcClient;

    @Test
    void atomicallyRejectsDuplicateProviderEvent() {
        ProviderWebhookEvent event =
                new ProviderWebhookEvent(
                        "event-" + UUID.randomUUID(),
                        "provider-case-501",
                        ProviderWebhookStatus.APPROVED
                );

        boolean firstRegistration =
                webhookEventStore.register(
                        UUID.randomUUID(),
                        "mock",
                        event,
                        "a".repeat(64),
                        Instant.now()
                );

        boolean duplicateRegistration =
                webhookEventStore.register(
                        UUID.randomUUID(),
                        "MOCK",
                        event,
                        "a".repeat(64),
                        Instant.now()
                );

        Long storedEvents = jdbcClient.sql("""
                        SELECT COUNT(*)
                        FROM provider_webhook_events
                        WHERE provider = :provider
                          AND event_id = :eventId
                        """)
                .param("provider", "MOCK")
                .param("eventId", event.eventId())
                .query(Long.class)
                .single();

        assertThat(firstRegistration).isTrue();
        assertThat(duplicateRegistration).isFalse();
        assertThat(storedEvents).isEqualTo(1L);
    }
}