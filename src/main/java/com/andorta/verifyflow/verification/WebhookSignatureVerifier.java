package com.andorta.verifyflow.verification;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.HexFormat;

@Component
public class WebhookSignatureVerifier {

    private static final String ALGORITHM = "HmacSHA256";

    private final byte[] secret;

    public WebhookSignatureVerifier(
            @Value("${verifyflow.webhook.secret}") String secret
    ) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException(
                    "Webhook secret must be configured"
            );
        }

        this.secret = secret.getBytes(StandardCharsets.UTF_8);
    }

    public boolean isValid(
            String payload,
            String providedSignature
    ) {
        if (payload == null
                || providedSignature == null
                || providedSignature.isBlank()) {
            return false;
        }

        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(new SecretKeySpec(secret, ALGORITHM));

            byte[] expected = mac.doFinal(
                    payload.getBytes(StandardCharsets.UTF_8)
            );

            String normalizedSignature =
                    providedSignature.startsWith("sha256=")
                            ? providedSignature.substring(7)
                            : providedSignature;

            byte[] provided = HexFormat.of()
                    .parseHex(normalizedSignature);

            return MessageDigest.isEqual(expected, provided);
        } catch (
                GeneralSecurityException
                | IllegalArgumentException exception
        ) {
            return false;
        }
    }
}