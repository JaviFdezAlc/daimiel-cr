package com.daimielcr.backend.domain.model.user;

import java.time.Instant;
import java.util.Objects;

public class User {

    private final UserId id;
    private PhoneNumber phoneNumber;
    private DisplayName displayName;
    private String profileImageUrl;
    private PhoneVerificationStatus phoneVerificationStatus;
    private UserRole role;
    private final Instant createdAt;
    private Instant updatedAt;

    private User(
            UserId id,
            PhoneNumber phoneNumber,
            DisplayName displayName,
            String profileImageUrl,
            PhoneVerificationStatus phoneVerificationStatus,
            UserRole role,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = Objects.requireNonNull(id, "El id del usuario es obligatorio");
        this.phoneNumber = Objects.requireNonNull(phoneNumber, "El teléfono es obligatorio");
        this.displayName = Objects.requireNonNull(displayName, "El nombre es obligatorio");
        this.profileImageUrl = normalizeProfileImageUrl(profileImageUrl);
        this.phoneVerificationStatus = Objects.requireNonNull(
                phoneVerificationStatus,
                "El estado de verificación es obligatorio"
        );
        this.role = Objects.requireNonNull(role, "El rol es obligatorio");
        this.createdAt = Objects.requireNonNull(createdAt, "La fecha de creación es obligatoria");
        this.updatedAt = Objects.requireNonNull(updatedAt, "La fecha de actualización es obligatoria");
    }

    public static User register(
            UserId id,
            PhoneNumber phoneNumber,
            DisplayName displayName,
            Instant now
    ) {
        return new User(
                id,
                phoneNumber,
                displayName,
                null,
                PhoneVerificationStatus.PENDING,
                UserRole.USER,
                now,
                now
        );
    }

    public static User restore(
            UserId id,
            PhoneNumber phoneNumber,
            DisplayName displayName,
            String profileImageUrl,
            PhoneVerificationStatus phoneVerificationStatus,
            UserRole role,
            Instant createdAt,
            Instant updatedAt
    ) {
        return new User(
                id,
                phoneNumber,
                displayName,
                profileImageUrl,
                phoneVerificationStatus,
                role,
                createdAt,
                updatedAt
        );
    }

    public void updateProfile(
            DisplayName displayName,
            String profileImageUrl,
            Instant now
    ) {
        this.displayName = Objects.requireNonNull(displayName, "El nombre es obligatorio");
        this.profileImageUrl = normalizeProfileImageUrl(profileImageUrl);
        this.updatedAt = Objects.requireNonNull(now, "La fecha de actualización es obligatoria");
    }

    public void changePhoneNumber(PhoneNumber newPhoneNumber, Instant now) {
        Objects.requireNonNull(newPhoneNumber, "El nuevo teléfono es obligatorio");

        if (!this.phoneNumber.equals(newPhoneNumber)) {
            this.phoneNumber = newPhoneNumber;
            this.phoneVerificationStatus = PhoneVerificationStatus.PENDING;
            this.updatedAt = Objects.requireNonNull(now, "La fecha de actualización es obligatoria");
        }
    }

    public void verifyPhone(Instant now) {
        this.phoneVerificationStatus = PhoneVerificationStatus.VERIFIED;
        this.updatedAt = Objects.requireNonNull(now, "La fecha de actualización es obligatoria");
    }

    public boolean isPhoneVerified() {
        return phoneVerificationStatus == PhoneVerificationStatus.VERIFIED;
    }

    public boolean hasRole(UserRole role) {
        return this.role == role;
    }

    public void makeAdmin(Instant now) {
        this.role = UserRole.ADMIN;
        this.updatedAt = Objects.requireNonNull(now, "La fecha de actualización es obligatoria");
    }

    public void makeRegularUser(Instant now) {
        this.role = UserRole.USER;
        this.updatedAt = Objects.requireNonNull(now, "La fecha de actualización es obligatoria");
    }

    private static String normalizeProfileImageUrl(String profileImageUrl) {
        if (profileImageUrl == null || profileImageUrl.isBlank()) {
            return null;
        }

        return profileImageUrl.trim();
    }

    public UserId id() {
        return id;
    }

    public PhoneNumber phoneNumber() {
        return phoneNumber;
    }

    public DisplayName displayName() {
        return displayName;
    }

    public String profileImageUrl() {
        return profileImageUrl;
    }

    public PhoneVerificationStatus phoneVerificationStatus() {
        return phoneVerificationStatus;
    }

    public UserRole role() {
        return role;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }
}
