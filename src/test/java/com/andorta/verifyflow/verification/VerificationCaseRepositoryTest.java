package com.andorta.verifyflow.verification;

import com.andorta.verifyflow.applicant.Applicant;
import com.andorta.verifyflow.applicant.ApplicantRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class VerificationCaseRepositoryTest {

    @Autowired
    private ApplicantRepository applicantRepository;

    @Autowired
    private VerificationCaseRepository verificationCaseRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void persistsAndFindsSubmittedVerificationCase() {
        Applicant applicant = applicantRepository.saveAndFlush(
                new Applicant(
                        "customer-" + UUID.randomUUID(),
                        "verification@example.com",
                        "GB"
                )
        );

        String providerReference =
                "provider-case-" + UUID.randomUUID();

        VerificationCase verificationCase =
                new VerificationCase(applicant, "mock");

        verificationCase =
                verificationCaseRepository.saveAndFlush(
                        verificationCase
                );

        verificationCase.submit(providerReference);

        verificationCaseRepository.saveAndFlush(
                verificationCase
        );

        UUID caseId = verificationCase.getId();

        entityManager.clear();

        Optional<VerificationCase> result =
                verificationCaseRepository
                        .findByProviderAndProviderReference(
                                "MOCK",
                                providerReference
                        );

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(caseId);
        assertThat(result.get().getStatus())
                .isEqualTo(VerificationStatus.PENDING);
        assertThat(result.get().getSubmittedAt()).isNotNull();
        assertThat(result.get().getApplicant().getId())
                .isEqualTo(applicant.getId());

        List<VerificationCase> applicantHistory =
                verificationCaseRepository
                        .findByApplicant_IdOrderByCreatedAtDesc(
                                applicant.getId()
                        );

        assertThat(applicantHistory)
                .extracting(VerificationCase::getId)
                .containsExactly(caseId);
    }
}