package com.andorta.verifyflow.verification;

import java.net.URI;

public record StartVerificationResult(
        VerificationCase verificationCase,
        URI verificationUrl
) {
}