package com.andorta.verifyflow.verification;

import com.andorta.verifyflow.applicant.Applicant;
import com.andorta.verifyflow.applicant.ApplicantNotFoundException;
import com.andorta.verifyflow.applicant.ApplicantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VerificationServiceTest {

    @Mock
    private ApplicantRepository applicantRepository;

    @Mock
    private VerificationCaseRepository verificationCaseRepository;

    @Mock
    private VerificationProvider verificationProvider;

    @InjectMocks
    private VerificationService verificationService;

    @Test
    void startsVerificationForExistingApplicant() {
        UUID applicantId = UUID.randomUUID();

        Applicant applicant = new Applicant(
                "customer-301",
                "verification@example.com",
                "GB"
        );

        ProviderSession providerSession =
                new ProviderSession(
                        "mock-session-301",
                        URI.create(
                                "https://sandbox.verifyflow.example/"
                                        + "sessions/mock-session-301"
                        )
                );

        when(applicantRepository.findById(applicantId))
                .thenReturn(Optional.of(applicant));
        when(verificationProvider.name())
                .thenReturn("MOCK");
        when(verificationProvider.createSession(applicantId))
                .thenReturn(providerSession);
        when(verificationCaseRepository.saveAndFlush(
                any(VerificationCase.class)
        )).thenAnswer(invocation -> invocation.getArgument(0));

        StartVerificationResult result =
                verificationService.startVerification(applicantId);

        assertThat(result.verificationCase().getStatus())
                .isEqualTo(VerificationStatus.PENDING);
        assertThat(
                result.verificationCase()
                        .getProviderReference()
        ).isEqualTo("mock-session-301");
        assertThat(result.verificationUrl())
                .isEqualTo(providerSession.verificationUrl());
    }

    @Test
    void rejectsUnknownApplicantBeforeCallingProvider() {
        UUID applicantId = UUID.randomUUID();

        when(applicantRepository.findById(applicantId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> verificationService
                        .startVerification(applicantId)
        )
                .isInstanceOf(ApplicantNotFoundException.class)
                .hasMessageContaining(applicantId.toString());

        verifyNoInteractions(
                verificationProvider,
                verificationCaseRepository
        );
    }
}