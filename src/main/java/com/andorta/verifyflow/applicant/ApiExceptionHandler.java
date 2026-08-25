package com.andorta.verifyflow.applicant;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(DuplicateApplicantException.class)
    public ResponseEntity<ProblemDetail> handleDuplicateApplicant(
            DuplicateApplicantException exception,
            HttpServletRequest request
    ) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                exception.getMessage()
        );

        problem.setTitle("Applicant already exists");
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty(
                "code",
                "APPLICANT_ALREADY_EXISTS"
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(problem);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidationFailure(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        Map<String, String> errors = exception
                .getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        error -> error.getField(),
                        error -> Optional.ofNullable(
                                error.getDefaultMessage()
                        ).orElse("Invalid value"),
                        (first, ignored) -> first,
                        LinkedHashMap::new
                ));

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Request validation failed"
        );

        problem.setTitle("Invalid request");
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("code", "VALIDATION_FAILED");
        problem.setProperty("errors", errors);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(problem);
    }

   @ExceptionHandler(ApplicantNotFoundException.class)
public ResponseEntity<ProblemDetail> handleApplicantNotFound(
        ApplicantNotFoundException exception,
        HttpServletRequest request
) {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND,
            exception.getMessage()
    );

    problem.setTitle("Applicant not found");
    problem.setInstance(URI.create(request.getRequestURI()));
    problem.setProperty("code", "APPLICANT_NOT_FOUND");

    return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(problem);
}

@ExceptionHandler(MethodArgumentTypeMismatchException.class)
public ResponseEntity<ProblemDetail> handleTypeMismatch(
        MethodArgumentTypeMismatchException exception,
        HttpServletRequest request
) {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            "Invalid value for parameter: "
                    + exception.getName()
    );

    problem.setTitle("Invalid request parameter");
    problem.setInstance(URI.create(request.getRequestURI()));
    problem.setProperty("code", "INVALID_PARAMETER");

    return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(problem);
}
}
