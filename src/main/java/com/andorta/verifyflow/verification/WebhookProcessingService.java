package com.andorta.verifyflow.verification;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Locale;
import java.util.UUID;

@Service
public class WebhookProcessingService {

    private final WebhookSignatureVerifier signatureVerifier;
    private final ObjectMapper objectMapper;
    private final WebhookEventStore webhookEventStore;
    private final VerificationCaseRepository caseRepository;

    public WebhookProcessingService(
            WebhookSignatureVerifier signatureVerifier,
            ObjectMapper objectMapper,
            WebhookEventStore webhookEventStore,
            VerificationCaseRepository caseRepository
    ) {
        this.signatureVerifier = signatureVerifier;
        this.objectMapper = objectMapper;
        this.webhookEventStore = webhookEventStore;
        this.caseRepository = caseRepository;
    }

    @Transactional
    public WebhookProcessingResult process(
            String provider,
            String signature,
            String rawPayload
    ) {
        if (!signatureVerifier.isValid(
                rawPayload,
                signature
        )) {
            throw new InvalidWebhookSignatureException();
        }

        String normalizedProvider =
                normalizeProvider(provider);

        ProviderWebhookEvent event =
                parsePayload(rawPayload);

        boolean registered = webhookEventStore.register(
                UUID.randomUUID(),
                normalizedProvider,
                event,
                sha256(rawPayload),
                Instant.now()
        );

        if (!registered) {
            return WebhookProcessingResult.DUPLICATE;
        }

        VerificationCase verificationCase =
                caseRepository
                        .findByProviderAndProviderReference(
                                normalizedProvider,
                                event.providerReference()
                        )
                        .orElseThrow(
                                () -> new VerificationCaseNotFoundException(
                                        normalizedProvider,
                                        event.providerReference()
                                )
                        );

        applyStatus(
                verificationCase,
                event.status()
        );

        caseRepository.saveAndFlush(verificationCase);

        boolean markedProcessed =
                webhookEventStore.markProcessed(
                        normalizedProvider,
                        event.eventId(),
                        verificationCase.getId(),
                        Instant.now()
                );

        if (!markedProcessed) {
            throw new IllegalStateException(
                    "Webhook event could not be marked processed"
            );
        }

        return WebhookProcessingResult.PROCESSED;
    }

    private ProviderWebhookEvent parsePayload(
            String rawPayload
    ) {
        try {
            return objectMapper.readValue(
                    rawPayload,
                    ProviderWebhookEvent.class
            );
        } catch (
                JacksonException
                | IllegalArgumentException exception
        ) {
            throw new MalformedWebhookPayloadException(
                    exception
            );
        }
    }

    private void applyStatus(
            VerificationCase verificationCase,
            ProviderWebhookStatus webhookStatus
    ) {
        VerificationStatus target =
                VerificationStatus.valueOf(
                        webhookStatus.name()
                );

        if (verificationCase.getStatus() == target) {
            return;
        }

        switch (webhookStatus) {
            case IN_REVIEW ->
                    moveToReview(verificationCase);

            case APPROVED -> {
                moveToReview(verificationCase);
                verificationCase.approve();
            }

            case REJECTED -> {
                moveToReview(verificationCase);
                verificationCase.reject();
            }

            case MORE_INFO_REQUIRED -> {
                moveToReview(verificationCase);
                verificationCase.requestMoreInfo();
            }
        }
    }

    private void moveToReview(
            VerificationCase verificationCase
    ) {
        if (verificationCase.getStatus()
                == VerificationStatus.PENDING) {
            verificationCase.beginReview();
            return;
        }

        if (verificationCase.getStatus()
                != VerificationStatus.IN_REVIEW) {
            throw new IllegalStateException(
                    "Cannot apply provider decision while case is "
                            + verificationCase.getStatus()
            );
        }
    }

    private String normalizeProvider(String provider) {
        if (provider == null || provider.isBlank()) {
            throw new IllegalArgumentException(
                    "Provider is required"
            );
        }

        return provider
                .trim()
                .toUpperCase(Locale.ROOT);
    }

    private String sha256(String payload) {
        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            return HexFormat.of().formatHex(
                    digest.digest(
                            payload.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    )
            );
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 is unavailable",
                    exception
            );
        }
    }
}