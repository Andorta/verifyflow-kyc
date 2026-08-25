package com.andorta.verifyflow.verification;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VerificationCaseRepository
        extends JpaRepository<VerificationCase, UUID> {

    List<VerificationCase>
    findByApplicant_IdOrderByCreatedAtDesc(UUID applicantId);

    Optional<VerificationCase>
    findByProviderAndProviderReference(
            String provider,
            String providerReference
    );
}