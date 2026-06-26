package com.daimielcr.backend.domain.model.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

class PhoneNumberTest {

    @Test
    void shouldNormalizePhoneNumber() {
        PhoneNumber phoneNumber = new PhoneNumber(" +34 600-111-222 ");

        assertEquals("+34600111222", phoneNumber.value());
        assertEquals("34600111222", phoneNumber.whatsappValue());
    }

    @Test
    void shouldRejectPhoneNumberWithoutInternationalPrefix() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new PhoneNumber("600111222")
        );
    }

    @Test
    void shouldRejectInvalidPhoneNumber() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new PhoneNumber("+34ABC123")
        );
    }
}
