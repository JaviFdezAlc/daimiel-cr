package com.daimielcr.backend.domain.model.user;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class UserTest {

    private static final Instant NOW = Instant.parse("2026-06-26T10:00:00Z");
    private static final Instant LATER = Instant.parse("2026-06-26T11:00:00Z");

    @Test
    void shouldRegisterUserAsRegularUserWithPendingPhoneVerification() {
        User user = User.register(
                new UserId(UUID.fromString("11111111-1111-1111-1111-111111111111")),
                new PhoneNumber("+34600111222"),
                new DisplayName("Javier Fernández"),
                NOW
        );

        assertEquals(UserRole.USER, user.role());
        assertEquals(PhoneVerificationStatus.PENDING, user.phoneVerificationStatus());
        assertFalse(user.isPhoneVerified());
        assertEquals(NOW, user.createdAt());
        assertEquals(NOW, user.updatedAt());
        assertEquals("+34600111222", user.phoneNumber().value());
    }

    @Test
    void shouldVerifyPhone() {
        User user = registeredUser();

        user.verifyPhone(LATER);

        assertTrue(user.isPhoneVerified());
        assertEquals(PhoneVerificationStatus.VERIFIED, user.phoneVerificationStatus());
        assertEquals(LATER, user.updatedAt());
    }

    @Test
    void shouldSetPhoneAsPendingWhenPhoneNumberChanges() {
        User user = registeredUser();
        user.verifyPhone(NOW);

        user.changePhoneNumber(new PhoneNumber("+34600999888"), LATER);

        assertEquals("+34600999888", user.phoneNumber().value());
        assertEquals(PhoneVerificationStatus.PENDING, user.phoneVerificationStatus());
        assertFalse(user.isPhoneVerified());
        assertEquals(LATER, user.updatedAt());
    }

    @Test
    void shouldKeepPhoneVerifiedWhenSamePhoneNumberIsAssigned() {
        User user = registeredUser();
        user.verifyPhone(NOW);

        user.changePhoneNumber(new PhoneNumber("+34600111222"), LATER);

        assertTrue(user.isPhoneVerified());
        assertEquals(NOW, user.updatedAt());
    }

    @Test
    void shouldUpdateProfile() {
        User user = registeredUser();

        user.updateProfile(
                new DisplayName("  Javier   F.  "),
                "  https://cdn.rutacrd.es/profiles/javier.png  ",
                LATER
        );

        assertEquals("Javier F.", user.displayName().value());
        assertEquals(
                "https://cdn.rutacrd.es/profiles/javier.png",
                user.profileImageUrl()
        );
        assertEquals(LATER, user.updatedAt());
    }

    @Test
    void shouldRecognizeAdminRole() {
        User user = registeredUser();

        user.makeAdmin(LATER);

        assertTrue(user.hasRole(UserRole.ADMIN));
        assertFalse(user.hasRole(UserRole.USER));
    }

    private User registeredUser() {
        return User.register(
                UserId.newId(),
                new PhoneNumber("+34600111222"),
                new DisplayName("Javier Fernández"),
                NOW
        );
    }
}
