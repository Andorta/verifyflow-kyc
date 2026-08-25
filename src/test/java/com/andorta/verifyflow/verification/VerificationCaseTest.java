package com.andorta.verifyflow.verification;

import com.andorta.verifyflow.applicant.Applicant;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VerificationCaseTest {

    @Test
    void followsApprovalWorkflow() {
        VerificationCase verificationCase =
                new VerificationCase(applicant(), "mock");

        assertThat(verificationCase.getStatus())
                .isEqualTo(VerificationStatus.CREATED);
        assertThat(verificationCase.getProvider())
                .isEqualTo("MOCK");

        verificationCase.submit("provider-case-001");
        assertThat(verificationCase.getStatus())
                .isEqualTo(VerificationStatus.PENDING);
        assertThat(verificationCase.getSubmittedAt())
                .isNotNull();

        verificationCase.beginReview();
        assertThat(verificationCase.getStatus())
                .isEqualTo(VerificationStatus.IN_REVIEW);

        verificationCase.approve();
        assertThat(verificationCase.getStatus())
                .isEqualTo(VerificationStatus.APPROVED);
        assertThat(verificationCase.getDecidedAt())
                .isNotNull();
    }

    @Test
    void allowsResubmissionAfterMoreInformationIsRequested() {
        VerificationCase verificationCase =
                new VerificationCase(applicant(), "mock");

        verificationCase.submit("provider-case-002");
        verificationCase.beginReview();
        verificationCase.requestMoreInfo();

        assertThat(verificationCase.getStatus())
                .isEqualTo(
                        VerificationStatus.MORE_INFO_REQUIRED
                );

        verificationCase.resubmit();

        assertThat(verificationCase.getStatus())
                .isEqualTo(VerificationStatus.PENDING);
    }

    @Test
    void preventsSkippingRequiredStates() {
        VerificationCase verificationCase =
                new VerificationCase(applicant(), "mock");

        assertThatThrownBy(verificationCase::approve)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("CREATED")
                .hasMessageContaining("IN_REVIEW");
    }

    private Applicant applicant() {
        return new Applicant(
                "customer-state-test",
                "state-test@example.com",
                "GB"
        );
    }
}