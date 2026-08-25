package com.andorta.verifyflow.verification;

import java.net.URI;
import java.time.Instant;
import java.util.UUID;

public record StartVerificationResponse(
        UUID caseId,
        UUID applicantId,
        String provider,
        String providerReference,
        VerificationStatus status,
        URI verificationUrl,
        Instant submittedAt
) {

    public static StartVerificationResponse from(
            UUID applicantId,
            StartVerificationResult result
    ) {
        VerificationCase verificationCase =
                result.verificationCase();

        return new StartVerificationResponse(
                verificationCase.getId(),
                applicantId,
                verificationCase.getProvider(),
                verificationCase.getProviderReference(),
                verificationCase.getStatus(),
                result.verificationUrl(),
                verificationCase.getSubmittedAt()
        );
    }
}