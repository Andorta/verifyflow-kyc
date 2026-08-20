package com.andorta.verifyflow.applicant;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApplicantService {

    private final ApplicantRepository applicantRepository;

    public ApplicantService(ApplicantRepository applicantRepository) {
        this.applicantRepository = applicantRepository;
    }

    @Transactional
    public Applicant createApplicant(
            String externalReference,
            String email,
            String countryCode
    ) {
        String normalizedReference = externalReference.trim();

        if (applicantRepository.existsByExternalReference(
                normalizedReference
        )) {
            throw new DuplicateApplicantException(normalizedReference);
        }

        Applicant applicant = new Applicant(
                normalizedReference,
                email,
                countryCode
        );

        try {
            return applicantRepository.saveAndFlush(applicant);
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateApplicantException(
                    normalizedReference,
                    exception
            );
        }
    }
}