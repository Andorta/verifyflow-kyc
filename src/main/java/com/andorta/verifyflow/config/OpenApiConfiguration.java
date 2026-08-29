package com.andorta.verifyflow.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class OpenApiConfiguration {

    @Bean
    public OpenAPI verifyFlowOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("VerifyFlow KYC API")
                        .version("v1")
                        .description(
                                "Production-style KYC integration "
                                        + "workflow for applicant onboarding, "
                                        + "verification cases, and secure "
                                        + "idempotent provider webhooks."
                        ));
    }
}