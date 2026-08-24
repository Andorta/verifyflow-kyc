package com.andorta.verifyflow.applicant;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/applicants")
public class ApplicantController {

    private final ApplicantService applicantService;

    public ApplicantController(ApplicantService applicantService) {
        this.applicantService = applicantService;
    }

    @PostMapping
    public ResponseEntity<ApplicantResponse> createApplicant(
            @Valid @RequestBody CreateApplicantRequest request
    ) {
        Applicant applicant = applicantService.createApplicant(
                request.externalReference(),
                request.email(),
                request.countryCode()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApplicantResponse.from(applicant));
    }
}