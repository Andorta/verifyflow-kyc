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
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

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
                                "GB");

                ProviderSession providerSession = new ProviderSession(
                                "mock-session-301",
                                URI.create(
                                                "https://sandbox.verifyflow.example/"
                                                                + "sessions/mock-session-301"));

                when(applicantRepository.findById(applicantId))
                                .thenReturn(Optional.of(applicant));
                when(verificationProvider.name())
                                .thenReturn("MOCK");
                when(verificationProvider.createSession(applicantId))
                                .thenReturn(providerSession);
                when(verificationCaseRepository.saveAndFlush(
                                any(VerificationCase.class))).thenAnswer(invocation -> invocation.getArgument(0));

                StartVerificationResult result = verificationService.startVerification(applicantId);

                assertThat(result.verificationCase().getStatus())
                                .isEqualTo(VerificationStatus.PENDING);
                assertThat(
                                result.verificationCase()
                                                .getProviderReference())
                                .isEqualTo("mock-session-301");
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
                                                .startVerification(applicantId))
                                .isInstanceOf(ApplicantNotFoundException.class)
                                .hasMessageContaining(applicantId.toString());

                verifyNoInteractions(
                                verificationProvider,
                                verificationCaseRepository);
        }

        @Test
        void returnsVerificationCaseWhenItExists() {
                UUID caseId = UUID.randomUUID();
                UUID applicantId = UUID.randomUUID();

                Instant submittedAt = Instant.parse("2026-08-29T10:00:00Z");
                Instant decidedAt = Instant.parse("2026-08-29T10:05:00Z");
                Instant createdAt = Instant.parse("2026-08-29T09:59:00Z");
                Instant updatedAt = Instant.parse("2026-08-29T10:05:00Z");

                Applicant applicant = mock(Applicant.class);
                VerificationCase verificationCase = mock(VerificationCase.class);

                when(verificationCaseRepository.findById(caseId))
                                .thenReturn(Optional.of(verificationCase));

                when(verificationCase.getId()).thenReturn(caseId);
                when(verificationCase.getApplicant())
                                .thenReturn(applicant);
                when(applicant.getId()).thenReturn(applicantId);
                when(verificationCase.getProvider())
                                .thenReturn("MOCK");
                when(verificationCase.getProviderReference())
                                .thenReturn("mock-session-601");
                when(verificationCase.getStatus())
                                .thenReturn(VerificationStatus.APPROVED);
                when(verificationCase.getSubmittedAt())
                                .thenReturn(submittedAt);
                when(verificationCase.getDecidedAt())
                                .thenReturn(decidedAt);
                when(verificationCase.getCreatedAt())
                                .thenReturn(createdAt);
                when(verificationCase.getUpdatedAt())
                                .thenReturn(updatedAt);

                VerificationCaseResponse result = verificationService.getVerificationCase(caseId);

                assertThat(result).isEqualTo(
                                new VerificationCaseResponse(
                                                caseId,
                                                applicantId,
                                                "MOCK",
                                                "mock-session-601",
                                                VerificationStatus.APPROVED,
                                                submittedAt,
                                                decidedAt,
                                                createdAt,
                                                updatedAt));
        }

        @Test
        void throwsWhenVerificationCaseDoesNotExist() {
                UUID caseId = UUID.randomUUID();

                when(verificationCaseRepository.findById(caseId))
                                .thenReturn(Optional.empty());

                assertThatThrownBy(
                                () -> verificationService
                                                .getVerificationCase(caseId))
                                .isInstanceOf(
                                                VerificationCaseNotFoundException.class)
                                .hasMessageContaining(caseId.toString());

                verifyNoInteractions(
                                applicantRepository,
                                verificationProvider);
        }
        @Test
void returnsVerificationCasesForExistingApplicant() {
    UUID applicantId = UUID.randomUUID();
    UUID caseId = UUID.randomUUID();

    Applicant applicant = mock(Applicant.class);
    VerificationCase verificationCase =
            mock(VerificationCase.class);

    when(applicantRepository.existsById(applicantId))
            .thenReturn(true);
    when(verificationCaseRepository
            .findByApplicant_IdOrderByCreatedAtDesc(
                    applicantId
            ))
            .thenReturn(List.of(verificationCase));

    when(verificationCase.getId()).thenReturn(caseId);
    when(verificationCase.getApplicant())
            .thenReturn(applicant);
    when(applicant.getId()).thenReturn(applicantId);
    when(verificationCase.getProvider())
            .thenReturn("MOCK");
    when(verificationCase.getProviderReference())
            .thenReturn("mock-session-801");
    when(verificationCase.getStatus())
            .thenReturn(VerificationStatus.PENDING);

    List<VerificationCaseResponse> result =
            verificationService
                    .getVerificationCasesForApplicant(
                            applicantId
                    );

    assertThat(result).hasSize(1);
    assertThat(result.get(0).caseId())
            .isEqualTo(caseId);
    assertThat(result.get(0).applicantId())
            .isEqualTo(applicantId);
    assertThat(result.get(0).providerReference())
            .isEqualTo("mock-session-801");
    assertThat(result.get(0).status())
            .isEqualTo(VerificationStatus.PENDING);
}

@Test
void returnsEmptyHistoryForApplicantWithoutCases() {
    UUID applicantId = UUID.randomUUID();

    when(applicantRepository.existsById(applicantId))
            .thenReturn(true);
    when(verificationCaseRepository
            .findByApplicant_IdOrderByCreatedAtDesc(
                    applicantId
            ))
            .thenReturn(List.of());

    List<VerificationCaseResponse> result =
            verificationService
                    .getVerificationCasesForApplicant(
                            applicantId
                    );

    assertThat(result).isEmpty();
}

@Test
void rejectsHistoryRequestForUnknownApplicant() {
    UUID applicantId = UUID.randomUUID();

    when(applicantRepository.existsById(applicantId))
            .thenReturn(false);

    assertThatThrownBy(
            () -> verificationService
                    .getVerificationCasesForApplicant(
                            applicantId
                    )
    )
            .isInstanceOf(ApplicantNotFoundException.class)
            .hasMessageContaining(applicantId.toString());

    verifyNoInteractions(
            verificationCaseRepository,
            verificationProvider
    );
}
}