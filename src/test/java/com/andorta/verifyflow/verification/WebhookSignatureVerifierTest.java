package com.andorta.verifyflow.verification;

import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

import static org.assertj.core.api.Assertions.assertThat;

class WebhookSignatureVerifierTest {

    private static final String SECRET = "test-secret";

    private final WebhookSignatureVerifier verifier =
            new WebhookSignatureVerifier(SECRET);

    @Test
    void acceptsValidSignature() throws Exception {
        String payload = """
                {"eventId":"event-001","status":"APPROVED"}
                """.trim();

        String signature = sign(payload);

        assertThat(verifier.isValid(payload, signature))
                .isTrue();
    }

    @Test
    void rejectsSignatureWhenPayloadWasModified()
            throws Exception {
        String originalPayload = """
                {"eventId":"event-001","status":"APPROVED"}
                """.trim();

        String signature = sign(originalPayload);

        String modifiedPayload = """
                {"eventId":"event-001","status":"REJECTED"}
                """.trim();

        assertThat(verifier.isValid(
                modifiedPayload,
                signature
        )).isFalse();
    }

    @Test
    void rejectsMalformedSignature() {
        assertThat(verifier.isValid(
                "{}",
                "not-hexadecimal"
        )).isFalse();
    }

    private String sign(String payload) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");

        mac.init(new SecretKeySpec(
                SECRET.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
        ));

        return HexFormat.of().formatHex(
                mac.doFinal(
                        payload.getBytes(StandardCharsets.UTF_8)
                )
        );
    }
}