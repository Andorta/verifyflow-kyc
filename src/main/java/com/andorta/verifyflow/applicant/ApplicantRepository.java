package com.andorta.verifyflow.applicant;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ApplicantRepository
        extends JpaRepository<Applicant, UUID> {

    Optional<Applicant> findByExternalReference(String externalReference);

    boolean existsByExternalReference(String externalReference);
}