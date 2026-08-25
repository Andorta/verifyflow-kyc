package com.andorta.verifyflow.verification;

import java.net.URI;
import java.util.Objects;

public record ProviderSession(
        String reference,
        URI verificationUrl
) {

    public ProviderSession {
        if (reference == null || reference.isBlank()) {
            throw new IllegalArgumentException(
                    "Provider reference is required"
            );
        }

        Objects.requireNonNull(
                verificationUrl,
                "Verification URL is required"
        );
    }
}