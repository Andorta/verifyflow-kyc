package com.andorta.verifyflow.applicant;

import java.time.Instant;
import java.util.UUID;

public record ApplicantResponse(
        UUID id,
        String externalReference,
        String email,
        String countryCode,
        Instant createdAt
) {

    public static ApplicantResponse from(Applicant applicant) {
        return new ApplicantResponse(
                applicant.getId(),
                applicant.getExternalReference(),
                applicant.getEmail(),
                applicant.getCountryCode(),
                applicant.getCreatedAt()
        );
    }
}