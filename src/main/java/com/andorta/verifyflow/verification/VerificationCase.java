package com.andorta.verifyflow.verification;

import com.andorta.verifyflow.applicant.Applicant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "verification_cases")
public class VerificationCase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "applicant_id",
            nullable = false,
            updatable = false
    )
    private Applicant applicant;

    @NotBlank
    @Size(max = 50)
    @Column(nullable = false, length = 50, updatable = false)
    private String provider;

    @Size(max = 100)
    @Column(name = "provider_reference", length = 100)
    private String providerReference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private VerificationStatus status;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @Column(name = "decided_at")
    private Instant decidedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(nullable = false)
    private long version;

    protected VerificationCase() {
        // Required by JPA
    }

    public VerificationCase(Applicant applicant, String provider) {
        this.applicant = Objects.requireNonNull(
                applicant,
                "Applicant is required"
        );

        if (provider == null || provider.isBlank()) {
            throw new IllegalArgumentException(
                    "Provider is required"
            );
        }

        this.provider = provider
                .trim()
                .toUpperCase(Locale.ROOT);
        this.status = VerificationStatus.CREATED;
    }

    public void submit(String providerReference) {
        requireStatus(VerificationStatus.CREATED);

        if (providerReference == null
                || providerReference.isBlank()) {
            throw new IllegalArgumentException(
                    "Provider reference is required"
            );
        }

        this.providerReference = providerReference.trim();
        this.status = VerificationStatus.PENDING;
        this.submittedAt = Instant.now();
    }

    public void beginReview() {
        requireStatus(VerificationStatus.PENDING);
        this.status = VerificationStatus.IN_REVIEW;
    }

    public void approve() {
        requireStatus(VerificationStatus.IN_REVIEW);
        this.status = VerificationStatus.APPROVED;
        this.decidedAt = Instant.now();
    }

    public void reject() {
        requireStatus(VerificationStatus.IN_REVIEW);
        this.status = VerificationStatus.REJECTED;
        this.decidedAt = Instant.now();
    }

    public void requestMoreInfo() {
        requireStatus(VerificationStatus.IN_REVIEW);
        this.status = VerificationStatus.MORE_INFO_REQUIRED;
    }

    public void resubmit() {
        requireStatus(VerificationStatus.MORE_INFO_REQUIRED);
        this.status = VerificationStatus.PENDING;
        this.submittedAt = Instant.now();
    }

    private void requireStatus(VerificationStatus expected) {
        if (status != expected) {
            throw new IllegalStateException(
                    "Cannot perform operation while case is "
                            + status
                            + "; expected "
                            + expected
            );
        }
    }

    public UUID getId() {
        return id;
    }

    public Applicant getApplicant() {
        return applicant;
    }

    public String getProvider() {
        return provider;
    }

    public String getProviderReference() {
        return providerReference;
    }

    public VerificationStatus getStatus() {
        return status;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public Instant getDecidedAt() {
        return decidedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public long getVersion() {
        return version;
    }
}