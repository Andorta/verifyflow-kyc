package com.andorta.verifyflow.verification;

import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.UUID;

@Component
public class SimulatedVerificationProvider
        implements VerificationProvider {

    @Override
    public String name() {
        return "MOCK";
    }

    @Override
    public ProviderSession createSession(UUID applicantId) {
        String reference = "mock-" + UUID.randomUUID();

        URI verificationUrl = URI.create(
                "https://sandbox.verifyflow.example/sessions/"
                        + reference
        );

        return new ProviderSession(
                reference,
                verificationUrl
        );
    }
}