package com.andorta.verifyflow.verification;

import java.util.UUID;

public interface VerificationProvider {

    String name();

    ProviderSession createSession(UUID applicantId);
}