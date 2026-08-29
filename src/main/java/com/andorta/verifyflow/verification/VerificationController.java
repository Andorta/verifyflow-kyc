package com.andorta.verifyflow.verification;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class VerificationController {

    private final VerificationService verificationService;

    public VerificationController(
            VerificationService verificationService
    ) {
        this.verificationService = verificationService;
    }

    @PostMapping(
            "/applicants/{applicantId}/verification-cases"
    )
    public ResponseEntity<StartVerificationResponse>
    startVerification(
            @PathVariable UUID applicantId
    ) {
        StartVerificationResult result =
                verificationService.startVerification(
                        applicantId
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(StartVerificationResponse.from(
                        applicantId,
                        result
                ));
    }

    @GetMapping("/verification-cases/{caseId}")
    public ResponseEntity<VerificationCaseResponse>
    getVerificationCase(
            @PathVariable UUID caseId
    ) {
        VerificationCaseResponse response =
                verificationService.getVerificationCase(
                        caseId
                );

        return ResponseEntity.ok(response);
    }
    @GetMapping(
        "/applicants/{applicantId}/verification-cases"
)
public ResponseEntity<List<VerificationCaseResponse>>
getApplicantVerificationCases(
        @PathVariable UUID applicantId
) {
    List<VerificationCaseResponse> cases =
            verificationService
                    .getVerificationCasesForApplicant(
                            applicantId
                    );

    return ResponseEntity.ok(cases);
}
}