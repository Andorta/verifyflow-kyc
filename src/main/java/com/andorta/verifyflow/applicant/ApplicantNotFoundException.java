package com.andorta.verifyflow.applicant;

import java.util.UUID;

public class ApplicantNotFoundException extends RuntimeException {

    public ApplicantNotFoundException(UUID id) {
        super("Applicant not found: " + id);
    }
}