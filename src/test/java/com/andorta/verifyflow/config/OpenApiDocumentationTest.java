package com.andorta.verifyflow.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OpenApiDocumentationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void exposesVerifyFlowOpenApiContract()
            throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title")
                        .value("VerifyFlow KYC API"))
                .andExpect(jsonPath("$.info.version")
                        .value("v1"))
                .andExpect(jsonPath(
                        "$.paths['/api/v1/applicants']"
                ).exists())
                .andExpect(jsonPath(
                        "$.paths['/api/v1/verification-cases/{caseId}']"
                ).exists())
                .andExpect(jsonPath(
                        "$.paths['/api/v1/webhooks/"
                                + "verification/{provider}']"
                ).exists());
    }
}