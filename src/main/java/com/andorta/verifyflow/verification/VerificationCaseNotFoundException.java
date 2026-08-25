package com.andorta.verifyflow.verification;

public class VerificationCaseNotFoundException
        extends RuntimeException {

    public VerificationCaseNotFoundException(
            String provider,
            String providerReference
    ) {
        super(
                "Verification case not found for provider "
                        + provider
                        + " and reference "
                        + providerReference
        );
    }
}