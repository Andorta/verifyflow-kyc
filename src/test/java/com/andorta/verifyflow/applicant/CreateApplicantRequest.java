package com.andorta.verifyflow.applicant;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateApplicantRequest(

        @NotBlank
        @Size(max = 100)
        String externalReference,

        @NotBlank
        @Email
        @Size(max = 254)
        String email,

        @NotBlank
        @Pattern(
                regexp = "^[A-Za-z]{2}$",
                message = "must be a two-letter country code"
        )
        String countryCode
) {
}