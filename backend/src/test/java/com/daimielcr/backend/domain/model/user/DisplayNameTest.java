package com.daimielcr.backend.domain.model.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

class DisplayNameTest {

    @Test
    void shouldNormalizeWhitespace() {
        DisplayName displayName = new DisplayName("  Javier    Fernández  ");

        assertEquals("Javier Fernández", displayName.value());
    }

    @Test
    void shouldRejectNameWithLessThanTwoCharacters() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new DisplayName("J")
        );
    }

    @Test
    void shouldRejectBlankName() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new DisplayName("   ")
        );
    }
}
