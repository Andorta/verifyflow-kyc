package com.andorta.verifyflow.verification;

import java.util.UUID;

public class VerificationCaseNotFoundException
                extends RuntimeException {

        public VerificationCaseNotFoundException(UUID caseId) {
                super("Verification case not found: " + caseId);
        }

        public VerificationCaseNotFoundException(
                        String provider,
                        String providerReference) {
                super(
                                "Verification case not found for provider "
                                                + provider
                                                + " and reference "
                                                + providerReference);
        }
}