package com.andorta.verifyflow.applicant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ApplicantController.class)
@Import(ApiExceptionHandler.class)
class ApplicantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ApplicantService applicantService;

    @Test
    void createsApplicant() throws Exception {
        UUID applicantId = UUID.randomUUID();
        Instant createdAt = Instant.parse(
                "2026-08-20T10:00:00Z"
        );

        Applicant applicant = mock(Applicant.class);

        when(applicant.getId()).thenReturn(applicantId);
        when(applicant.getExternalReference())
                .thenReturn("customer-201");
        when(applicant.getEmail())
                .thenReturn("demo@example.com");
        when(applicant.getCountryCode()).thenReturn("GB");
        when(applicant.getCreatedAt()).thenReturn(createdAt);

        when(applicantService.createApplicant(
                "customer-201",
                "demo@example.com",
                "gb"
        )).thenReturn(applicant);

        mockMvc.perform(post("/api/v1/applicants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "externalReference": "customer-201",
                                  "email": "demo@example.com",
                                  "countryCode": "gb"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id")
                        .value(applicantId.toString()))
                .andExpect(jsonPath("$.externalReference")
                        .value("customer-201"))
                .andExpect(jsonPath("$.email")
                        .value("demo@example.com"))
                .andExpect(jsonPath("$.countryCode")
                        .value("GB"))
                .andExpect(jsonPath("$.createdAt")
                        .value("2026-08-20T10:00:00Z"));
    }

    @Test
    void rejectsInvalidRequest() throws Exception {
        mockMvc.perform(post("/api/v1/applicants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "externalReference": "",
                                  "email": "not-an-email",
                                  "countryCode": "United Kingdom"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code")
                        .value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors.externalReference")
                        .exists())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.countryCode")
                        .exists());

        verifyNoInteractions(applicantService);
    }

    @Test
    void returnsConflictForDuplicateApplicant() throws Exception {
        when(applicantService.createApplicant(
                "customer-201",
                "demo@example.com",
                "GB"
        )).thenThrow(
                new DuplicateApplicantException("customer-201")
        );

        mockMvc.perform(post("/api/v1/applicants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "externalReference": "customer-201",
                                  "email": "demo@example.com",
                                  "countryCode": "GB"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title")
                        .value("Applicant already exists"))
                .andExpect(jsonPath("$.code")
                        .value("APPLICANT_ALREADY_EXISTS"));
    }
}