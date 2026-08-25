package com.andorta.verifyflow.verification;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/webhooks/verification")
public class WebhookController {

    private final WebhookProcessingService processingService;

    public WebhookController(
            WebhookProcessingService processingService
    ) {
        this.processingService = processingService;
    }

    @PostMapping(
            value = "/{provider}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<WebhookResponse> receiveWebhook(
            @PathVariable String provider,
            @RequestHeader("X-Webhook-Signature")
            String signature,
            @RequestBody String rawPayload
    ) {
        WebhookProcessingResult result =
                processingService.process(
                        provider,
                        signature,
                        rawPayload
                );

        return ResponseEntity.ok(
                new WebhookResponse(result)
        );
    }
}