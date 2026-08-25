package com.andorta.verifyflow.verification;

import com.andorta.verifyflow.applicant.ApiExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WebhookController.class)
@Import(ApiExceptionHandler.class)
class WebhookControllerTest {

    private static final String PAYLOAD = """
            {
              "eventId": "event-501",
              "providerReference": "mock-session-401",
              "status": "APPROVED"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WebhookProcessingService processingService;

    @Test
    void processesValidWebhook() throws Exception {
        when(processingService.process(
                "mock",
                "valid-signature",
                PAYLOAD
        )).thenReturn(WebhookProcessingResult.PROCESSED);

        mockMvc.perform(post(
                        "/api/v1/webhooks/verification/mock"
                )
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(
                                "X-Webhook-Signature",
                                "valid-signature"
                        )
                        .content(PAYLOAD))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status")
                        .value("PROCESSED"));
    }

    @Test
    void acknowledgesDuplicateWebhook() throws Exception {
        when(processingService.process(
                "mock",
                "valid-signature",
                PAYLOAD
        )).thenReturn(WebhookProcessingResult.DUPLICATE);

        mockMvc.perform(post(
                        "/api/v1/webhooks/verification/mock"
                )
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(
                                "X-Webhook-Signature",
                                "valid-signature"
                        )
                        .content(PAYLOAD))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status")
                        .value("DUPLICATE"));
    }

    @Test
    void rejectsInvalidSignature() throws Exception {
        when(processingService.process(
                "mock",
                "invalid-signature",
                PAYLOAD
        )).thenThrow(
                new InvalidWebhookSignatureException()
        );

        mockMvc.perform(post(
                        "/api/v1/webhooks/verification/mock"
                )
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(
                                "X-Webhook-Signature",
                                "invalid-signature"
                        )
                        .content(PAYLOAD))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code")
                        .value("INVALID_WEBHOOK_SIGNATURE"));
    }

    @Test
    void rejectsMalformedPayload() throws Exception {
        when(processingService.process(
                "mock",
                "valid-signature",
                PAYLOAD
        )).thenThrow(
                new MalformedWebhookPayloadException(
                        new IllegalArgumentException(
                                "Invalid JSON"
                        )
                )
        );

        mockMvc.perform(post(
                        "/api/v1/webhooks/verification/mock"
                )
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(
                                "X-Webhook-Signature",
                                "valid-signature"
                        )
                        .content(PAYLOAD))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code")
                        .value("MALFORMED_WEBHOOK_PAYLOAD"));
    }

    @Test
    void returnsNotFoundForUnknownVerificationCase()
            throws Exception {
        when(processingService.process(
                "mock",
                "valid-signature",
                PAYLOAD
        )).thenThrow(
                new VerificationCaseNotFoundException(
                        "MOCK",
                        "mock-session-401"
                )
        );

        mockMvc.perform(post(
                        "/api/v1/webhooks/verification/mock"
                )
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(
                                "X-Webhook-Signature",
                                "valid-signature"
                        )
                        .content(PAYLOAD))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code")
                        .value("VERIFICATION_CASE_NOT_FOUND"));
    }
}