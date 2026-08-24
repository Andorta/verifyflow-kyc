package com.andorta.verifyflow.applicant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicantServiceTest {

    @Mock
    private ApplicantRepository applicantRepository;

    @InjectMocks
    private ApplicantService applicantService;

    @Test
    void createsApplicantWhenReferenceIsAvailable() {
        when(applicantRepository.existsByExternalReference("customer-101"))
                .thenReturn(false);

        when(applicantRepository.saveAndFlush(any(Applicant.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Applicant result = applicantService.createApplicant(
                " customer-101 ",
                "Customer@Example.com",
                "gb"
        );

        assertThat(result.getExternalReference())
                .isEqualTo("customer-101");
        assertThat(result.getEmail())
                .isEqualTo("customer@example.com");
        assertThat(result.getCountryCode()).isEqualTo("GB");

        verify(applicantRepository)
                .saveAndFlush(any(Applicant.class));
    }

    @Test
    void rejectsApplicantWhenReferenceAlreadyExists() {
        when(applicantRepository.existsByExternalReference("customer-101"))
                .thenReturn(true);

        assertThatThrownBy(() -> applicantService.createApplicant(
                "customer-101",
                "customer@example.com",
                "GB"
        ))
                .isInstanceOf(DuplicateApplicantException.class)
                .hasMessageContaining("customer-101");

        verify(applicantRepository, never())
                .saveAndFlush(any(Applicant.class));
    }

    @Test
    void convertsDatabaseRaceIntoDuplicateApplicantException() {
        when(applicantRepository.existsByExternalReference("customer-101"))
                .thenReturn(false);

        when(applicantRepository.saveAndFlush(any(Applicant.class)))
                .thenThrow(new DataIntegrityViolationException(
                        "Unique constraint violation"
                ));

        assertThatThrownBy(() -> applicantService.createApplicant(
                "customer-101",
                "customer@example.com",
                "GB"
        ))
                .isInstanceOf(DuplicateApplicantException.class)
                .hasCauseInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void returnsApplicantWhenItExists() {
        UUID id = UUID.randomUUID();

        Applicant applicant = new Applicant(
                "customer-202",
                "customer@example.com",
                "GB"
        );

        when(applicantRepository.findById(id))
                .thenReturn(Optional.of(applicant));

        Applicant result = applicantService.getApplicant(id);

        assertThat(result).isSameAs(applicant);
    }

    @Test
    void throwsWhenApplicantDoesNotExist() {
        UUID id = UUID.randomUUID();

        when(applicantRepository.findById(id))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> applicantService.getApplicant(id))
                .isInstanceOf(ApplicantNotFoundException.class)
                .hasMessageContaining(id.toString());
    }
}