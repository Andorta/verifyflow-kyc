package com.andorta.verifyflow.verification;

public class InvalidWebhookSignatureException
        extends RuntimeException {

    public InvalidWebhookSignatureException() {
        super("Webhook signature is invalid");
    }
}