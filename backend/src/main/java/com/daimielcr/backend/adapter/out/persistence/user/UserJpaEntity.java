package com.daimielcr.backend.adapter.out.persistence.user;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import com.daimielcr.backend.domain.model.user.PhoneVerificationStatus;
import com.daimielcr.backend.domain.model.user.UserRole;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class UserJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "phone_number", nullable = false, length = 16)
    private String phoneNumber;

    @Column(name = "display_name", nullable = false, length = 60)
    private String displayName;

    @Column(name = "profile_image_url", length = 2048)
    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "phone_verification_status", nullable = false, length = 16)
    private PhoneVerificationStatus phoneVerificationStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 16)
    private UserRole role;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected UserJpaEntity() {
        // Required by JPA.
    }

    public UserJpaEntity(
            UUID id,
            String phoneNumber,
            String displayName,
            String profileImageUrl,
            PhoneVerificationStatus phoneVerificationStatus,
            UserRole role,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = Objects.requireNonNull(id);
        this.phoneNumber = Objects.requireNonNull(phoneNumber);
        this.displayName = Objects.requireNonNull(displayName);
        this.profileImageUrl = profileImageUrl;
        this.phoneVerificationStatus = Objects.requireNonNull(phoneVerificationStatus);
        this.role = Objects.requireNonNull(role);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public UUID getId() {
        return id;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public PhoneVerificationStatus getPhoneVerificationStatus() {
        return phoneVerificationStatus;
    }

    public UserRole getRole() {
        return role;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}