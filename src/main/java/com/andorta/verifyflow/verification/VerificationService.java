package com.andorta.verifyflow.verification;

import com.andorta.verifyflow.applicant.Applicant;
import com.andorta.verifyflow.applicant.ApplicantNotFoundException;
import com.andorta.verifyflow.applicant.ApplicantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class VerificationService {

    private final ApplicantRepository applicantRepository;
    private final VerificationCaseRepository verificationCaseRepository;
    private final VerificationProvider verificationProvider;

    public VerificationService(
            ApplicantRepository applicantRepository,
            VerificationCaseRepository verificationCaseRepository,
            VerificationProvider verificationProvider
    ) {
        this.applicantRepository = applicantRepository;
        this.verificationCaseRepository =
                verificationCaseRepository;
        this.verificationProvider = verificationProvider;
    }

    @Transactional
    public StartVerificationResult startVerification(
            UUID applicantId
    ) {
        Applicant applicant = applicantRepository
                .findById(applicantId)
                .orElseThrow(
                        () -> new ApplicantNotFoundException(
                                applicantId
                        )
                );

        ProviderSession providerSession =
                verificationProvider.createSession(applicantId);

        VerificationCase verificationCase =
                new VerificationCase(
                        applicant,
                        verificationProvider.name()
                );

        verificationCase.submit(
                providerSession.reference()
        );

        VerificationCase savedCase =
                verificationCaseRepository.saveAndFlush(
                        verificationCase
                );

        return new StartVerificationResult(
                savedCase,
                providerSession.verificationUrl()
        );
    }
}