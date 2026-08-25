package com.andorta.verifyflow.verification;

import java.util.Objects;

public record ProviderWebhookEvent(
        String eventId,
        String providerReference,
        ProviderWebhookStatus status
) {

    public ProviderWebhookEvent {
        if (eventId == null || eventId.isBlank()) {
            throw new IllegalArgumentException(
                    "Event ID is required"
            );
        }

        if (providerReference == null
                || providerReference.isBlank()) {
            throw new IllegalArgumentException(
                    "Provider reference is required"
            );
        }

        Objects.requireNonNull(
                status,
                "Webhook status is required"
        );
    }
}