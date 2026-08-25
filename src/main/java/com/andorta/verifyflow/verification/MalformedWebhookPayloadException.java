package com.andorta.verifyflow.verification;

public class MalformedWebhookPayloadException
        extends RuntimeException {

    public MalformedWebhookPayloadException(Throwable cause) {
        super("Webhook payload is invalid", cause);
    }
}