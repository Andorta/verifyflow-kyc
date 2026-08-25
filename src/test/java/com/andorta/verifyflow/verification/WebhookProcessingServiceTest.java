package com.andorta.verifyflow.verification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WebhookProcessingServiceTest {

    @Mock
    private WebhookSignatureVerifier signatureVerifier;

    @Mock
    private WebhookEventStore webhookEventStore;

    @Mock
    private VerificationCaseRepository caseRepository;

    private WebhookProcessingService processingService;

    @BeforeEach
    void setUp() {
        processingService = new WebhookProcessingService(
                signatureVerifier,
                new ObjectMapper(),
                webhookEventStore,
                caseRepository
        );
    }

    @Test
    void processesApprovedWebhook() {
        String payload = """
                {
                  "eventId": "event-601",
                  "providerReference": "mock-session-601",
                  "status": "APPROVED"
                }
                """;

        UUID caseId = UUID.randomUUID();
        VerificationCase verificationCase =
                mock(VerificationCase.class);

        when(signatureVerifier.isValid(payload, "signature"))
                .thenReturn(true);
        when(webhookEventStore.register(
                any(),
                eq("MOCK"),
                any(),
                any(),
                any()
        )).thenReturn(true);
        when(caseRepository
                .findByProviderAndProviderReference(
                        "MOCK",
                        "mock-session-601"
                ))
                .thenReturn(Optional.of(verificationCase));
        when(verificationCase.getStatus())
                .thenReturn(VerificationStatus.PENDING);
        when(verificationCase.getId()).thenReturn(caseId);
        when(caseRepository.saveAndFlush(verificationCase))
                .thenReturn(verificationCase);
        when(webhookEventStore.markProcessed(
                eq("MOCK"),
                eq("event-601"),
                eq(caseId),
                any(Instant.class)
        )).thenReturn(true);

        WebhookProcessingResult result =
                processingService.process(
                        "mock",
                        "signature",
                        payload
                );

        assertThat(result)
                .isEqualTo(WebhookProcessingResult.PROCESSED);

        verify(verificationCase).beginReview();
        verify(verificationCase).approve();
    }

    @Test
    void returnsDuplicateWithoutApplyingEventAgain() {
        String payload = """
                {
                  "eventId": "event-602",
                  "providerReference": "mock-session-602",
                  "status": "APPROVED"
                }
                """;

        when(signatureVerifier.isValid(payload, "signature"))
                .thenReturn(true);
        when(webhookEventStore.register(
                any(),
                eq("MOCK"),
                any(),
                any(),
                any()
        )).thenReturn(false);

        WebhookProcessingResult result =
                processingService.process(
                        "mock",
                        "signature",
                        payload
                );

        assertThat(result)
                .isEqualTo(WebhookProcessingResult.DUPLICATE);

        verifyNoInteractions(caseRepository);
    }

    @Test
    void rejectsInvalidSignatureBeforeDatabaseAccess() {
        when(signatureVerifier.isValid(
                "{}",
                "invalid-signature"
        )).thenReturn(false);

        assertThatThrownBy(() -> processingService.process(
                "mock",
                "invalid-signature",
                "{}"
        )).isInstanceOf(
                InvalidWebhookSignatureException.class
        );

        verifyNoInteractions(
                webhookEventStore,
                caseRepository
        );
    }

    @Test
    void rejectsUnknownProviderReference() {
        String payload = """
                {
                  "eventId": "event-603",
                  "providerReference": "missing-session",
                  "status": "APPROVED"
                }
                """;

        when(signatureVerifier.isValid(payload, "signature"))
                .thenReturn(true);
        when(webhookEventStore.register(
                any(),
                eq("MOCK"),
                any(),
                any(),
                any()
        )).thenReturn(true);
        when(caseRepository
                .findByProviderAndProviderReference(
                        "MOCK",
                        "missing-session"
                ))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> processingService.process(
                "mock",
                "signature",
                payload
        )).isInstanceOf(
                VerificationCaseNotFoundException.class
        );
    }
}