package com.andorta.verifyflow.verification;

import java.time.Instant;
import java.util.UUID;

public record VerificationCaseResponse(
        UUID caseId,
        UUID applicantId,
        String provider,
        String providerReference,
        VerificationStatus status,
        Instant submittedAt,
        Instant decidedAt,
        Instant createdAt,
        Instant updatedAt
) {

    public static VerificationCaseResponse from(
            VerificationCase verificationCase
    ) {
        return new VerificationCaseResponse(
                verificationCase.getId(),
                verificationCase.getApplicant().getId(),
                verificationCase.getProvider(),
                verificationCase.getProviderReference(),
                verificationCase.getStatus(),
                verificationCase.getSubmittedAt(),
                verificationCase.getDecidedAt(),
                verificationCase.getCreatedAt(),
                verificationCase.getUpdatedAt()
        );
    }
}