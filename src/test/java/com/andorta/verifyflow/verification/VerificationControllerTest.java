package com.andorta.verifyflow.verification;

import com.andorta.verifyflow.applicant.ApiExceptionHandler;
import com.andorta.verifyflow.applicant.ApplicantNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URI;
import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VerificationController.class)
@Import(ApiExceptionHandler.class)
class VerificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VerificationService verificationService;

    @Test
    void startsVerificationForApplicant() throws Exception {
        UUID applicantId = UUID.randomUUID();
        UUID caseId = UUID.randomUUID();

        VerificationCase verificationCase =
                mock(VerificationCase.class);

        when(verificationCase.getId()).thenReturn(caseId);
        when(verificationCase.getProvider())
                .thenReturn("MOCK");
        when(verificationCase.getProviderReference())
                .thenReturn("mock-session-401");
        when(verificationCase.getStatus())
                .thenReturn(VerificationStatus.PENDING);
        when(verificationCase.getSubmittedAt())
                .thenReturn(Instant.parse(
                        "2026-08-25T10:00:00Z"
                ));

        StartVerificationResult result =
                new StartVerificationResult(
                        verificationCase,
                        URI.create(
                                "https://sandbox.verifyflow.example/"
                                        + "sessions/mock-session-401"
                        )
                );

        when(verificationService.startVerification(applicantId))
                .thenReturn(result);

        mockMvc.perform(post(
                        "/api/v1/applicants/{applicantId}"
                                + "/verification-cases",
                        applicantId
                ))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.caseId")
                        .value(caseId.toString()))
                .andExpect(jsonPath("$.applicantId")
                        .value(applicantId.toString()))
                .andExpect(jsonPath("$.provider")
                        .value("MOCK"))
                .andExpect(jsonPath("$.status")
                        .value("PENDING"))
                .andExpect(jsonPath("$.verificationUrl")
                        .value(
                                "https://sandbox.verifyflow.example/"
                                        + "sessions/mock-session-401"
                        ));
    }

    @Test
    void returnsNotFoundForUnknownApplicant() throws Exception {
        UUID applicantId = UUID.randomUUID();

        when(verificationService.startVerification(applicantId))
                .thenThrow(
                        new ApplicantNotFoundException(applicantId)
                );

        mockMvc.perform(post(
                        "/api/v1/applicants/{applicantId}"
                                + "/verification-cases",
                        applicantId
                ))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code")
                        .value("APPLICANT_NOT_FOUND"));
    }

    @Test
    void rejectsMalformedApplicantId() throws Exception {
        mockMvc.perform(post(
                        "/api/v1/applicants/not-a-uuid"
                                + "/verification-cases"
                ))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code")
                        .value("INVALID_PARAMETER"));
    }
}