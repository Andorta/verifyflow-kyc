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
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

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

                VerificationCase verificationCase = mock(VerificationCase.class);

                when(verificationCase.getId()).thenReturn(caseId);
                when(verificationCase.getProvider())
                                .thenReturn("MOCK");
                when(verificationCase.getProviderReference())
                                .thenReturn("mock-session-401");
                when(verificationCase.getStatus())
                                .thenReturn(VerificationStatus.PENDING);
                when(verificationCase.getSubmittedAt())
                                .thenReturn(Instant.parse(
                                                "2026-08-25T10:00:00Z"));

                StartVerificationResult result = new StartVerificationResult(
                                verificationCase,
                                URI.create(
                                                "https://sandbox.verifyflow.example/"
                                                                + "sessions/mock-session-401"));

                when(verificationService.startVerification(applicantId))
                                .thenReturn(result);

                mockMvc.perform(post(
                                "/api/v1/applicants/{applicantId}"
                                                + "/verification-cases",
                                applicantId))
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
                                                                                + "sessions/mock-session-401"));
        }

        @Test
        void returnsNotFoundForUnknownApplicant() throws Exception {
                UUID applicantId = UUID.randomUUID();

                when(verificationService.startVerification(applicantId))
                                .thenThrow(
                                                new ApplicantNotFoundException(applicantId));

                mockMvc.perform(post(
                                "/api/v1/applicants/{applicantId}"
                                                + "/verification-cases",
                                applicantId))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.code")
                                                .value("APPLICANT_NOT_FOUND"));
        }

        @Test
        void rejectsMalformedApplicantId() throws Exception {
                mockMvc.perform(post(
                                "/api/v1/applicants/not-a-uuid"
                                                + "/verification-cases"))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.code")
                                                .value("INVALID_PARAMETER"));
        }

        @Test
        void returnsVerificationCase() throws Exception {
                UUID caseId = UUID.randomUUID();
                UUID applicantId = UUID.randomUUID();

                VerificationCaseResponse response = new VerificationCaseResponse(
                                caseId,
                                applicantId,
                                "MOCK",
                                "mock-session-701",
                                VerificationStatus.APPROVED,
                                Instant.parse(
                                                "2026-08-29T10:00:00Z"),
                                Instant.parse(
                                                "2026-08-29T10:05:00Z"),
                                Instant.parse(
                                                "2026-08-29T09:59:00Z"),
                                Instant.parse(
                                                "2026-08-29T10:05:00Z"));

                when(verificationService.getVerificationCase(caseId))
                                .thenReturn(response);

                mockMvc.perform(get(
                                "/api/v1/verification-cases/{caseId}",
                                caseId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.caseId")
                                                .value(caseId.toString()))
                                .andExpect(jsonPath("$.applicantId")
                                                .value(applicantId.toString()))
                                .andExpect(jsonPath("$.provider")
                                                .value("MOCK"))
                                .andExpect(jsonPath("$.providerReference")
                                                .value("mock-session-701"))
                                .andExpect(jsonPath("$.status")
                                                .value("APPROVED"))
                                .andExpect(jsonPath("$.decidedAt")
                                                .value("2026-08-29T10:05:00Z"));
        }

        @Test
        void returnsNotFoundForUnknownVerificationCase()
                        throws Exception {
                UUID caseId = UUID.randomUUID();

                when(verificationService.getVerificationCase(caseId))
                                .thenThrow(
                                                new VerificationCaseNotFoundException(
                                                                caseId));

                mockMvc.perform(get(
                                "/api/v1/verification-cases/{caseId}",
                                caseId))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.code")
                                                .value("VERIFICATION_CASE_NOT_FOUND"));
        }

        @Test
        void rejectsMalformedVerificationCaseId()
                        throws Exception {
                mockMvc.perform(get(
                                "/api/v1/verification-cases/not-a-uuid"))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.code")
                                                .value("INVALID_PARAMETER"));
        }
        @Test
void returnsApplicantVerificationHistory()
        throws Exception {
    UUID applicantId = UUID.randomUUID();
    UUID caseId = UUID.randomUUID();

    VerificationCaseResponse response =
            new VerificationCaseResponse(
                    caseId,
                    applicantId,
                    "MOCK",
                    "mock-session-901",
                    VerificationStatus.APPROVED,
                    Instant.parse(
                            "2026-08-29T11:00:00Z"
                    ),
                    Instant.parse(
                            "2026-08-29T11:05:00Z"
                    ),
                    Instant.parse(
                            "2026-08-29T10:59:00Z"
                    ),
                    Instant.parse(
                            "2026-08-29T11:05:00Z"
                    )
            );

    when(verificationService
            .getVerificationCasesForApplicant(
                    applicantId
            ))
            .thenReturn(List.of(response));

    mockMvc.perform(get(
                    "/api/v1/applicants/{applicantId}"
                            + "/verification-cases",
                    applicantId
            ))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()")
                    .value(1))
            .andExpect(jsonPath("$[0].caseId")
                    .value(caseId.toString()))
            .andExpect(jsonPath("$[0].providerReference")
                    .value("mock-session-901"))
            .andExpect(jsonPath("$[0].status")
                    .value("APPROVED"));
}

@Test
void returnsEmptyApplicantVerificationHistory()
        throws Exception {
    UUID applicantId = UUID.randomUUID();

    when(verificationService
            .getVerificationCasesForApplicant(
                    applicantId
            ))
            .thenReturn(List.of());

    mockMvc.perform(get(
                    "/api/v1/applicants/{applicantId}"
                            + "/verification-cases",
                    applicantId
            ))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()")
                    .value(0));
}

@Test
void returnsNotFoundWhenListingCasesForUnknownApplicant()
        throws Exception {
    UUID applicantId = UUID.randomUUID();

    when(verificationService
            .getVerificationCasesForApplicant(
                    applicantId
            ))
            .thenThrow(
                    new ApplicantNotFoundException(
                            applicantId
                    )
            );

    mockMvc.perform(get(
                    "/api/v1/applicants/{applicantId}"
                            + "/verification-cases",
                    applicantId
            ))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code")
                    .value("APPLICANT_NOT_FOUND"));
}
}