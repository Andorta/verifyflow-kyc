package com.andorta.verifyflow.applicant;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.net.URI;
import java.util.UUID;

@Tag(name = "Applicants", description = "Applicant onboarding and retrieval")

@RestController
@RequestMapping("/api/v1/applicants")
public class ApplicantController {

        private final ApplicantService applicantService;

        public ApplicantController(ApplicantService applicantService) {
                this.applicantService = applicantService;
        }

        @PostMapping
        public ResponseEntity<ApplicantResponse> createApplicant(
                        @Valid @RequestBody CreateApplicantRequest request) {
                Applicant applicant = applicantService.createApplicant(
                                request.externalReference(),
                                request.email(),
                                request.countryCode());

                URI location = ServletUriComponentsBuilder
                                .fromCurrentRequest()
                                .path("/{id}")
                                .buildAndExpand(applicant.getId())
                                .toUri();

                return ResponseEntity
                                .created(location)
                                .body(ApplicantResponse.from(applicant));
        }

        @GetMapping("/{id}")
        public ApplicantResponse getApplicant(@PathVariable UUID id) {
                return ApplicantResponse.from(
                                applicantService.getApplicant(id));
        }
}