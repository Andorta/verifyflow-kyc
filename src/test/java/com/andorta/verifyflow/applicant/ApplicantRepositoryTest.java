package com.andorta.verifyflow.applicant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class ApplicantRepositoryTest {

    @Autowired
    private ApplicantRepository applicantRepository;

    @Test
    void savesAndFindsApplicantByExternalReference() {
        String externalReference = "customer-" + UUID.randomUUID();

        Applicant applicant = new Applicant(
                externalReference,
                "Demo.Customer@Example.com",
                "gb"
        );

        Applicant saved = applicantRepository.saveAndFlush(applicant);

        Optional<Applicant> result =
                applicantRepository.findByExternalReference(externalReference);

        assertThat(saved.getId()).isNotNull();
        assertThat(result).isPresent();
        assertThat(result.get().getEmail())
                .isEqualTo("demo.customer@example.com");
        assertThat(result.get().getCountryCode()).isEqualTo("GB");
        assertThat(result.get().getCreatedAt()).isNotNull();
    }
}