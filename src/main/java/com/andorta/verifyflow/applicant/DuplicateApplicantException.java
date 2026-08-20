package com.andorta.verifyflow.applicant;

public class DuplicateApplicantException extends RuntimeException {

    public DuplicateApplicantException(String externalReference) {
        super("An applicant already exists for reference: "
                + externalReference);
    }

    public DuplicateApplicantException(
            String externalReference,
            Throwable cause
    ) {
        super(
                "An applicant already exists for reference: "
                        + externalReference,
                cause
        );
    }
}