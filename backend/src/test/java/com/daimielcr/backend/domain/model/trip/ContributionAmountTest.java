package com.daimielcr.backend.domain.model.trip;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.daimielcr.backend.domain.exceptions.InvalidTripException;

class ContributionAmountTest {

    @Test
    void shouldNormalizeContributionToTwoDecimals() {
        ContributionAmount amount = new ContributionAmount(new BigDecimal("3"));

        assertEquals(new BigDecimal("3.00"), amount.value());
        assertFalse(amount.isFree());
    }

    @Test
    void shouldCreateFreeContribution() {
        ContributionAmount amount = ContributionAmount.none();

        assertEquals(new BigDecimal("0.00"), amount.value());
        assertTrue(amount.isFree());
    }

    @Test
    void shouldRejectNegativeContribution() {
        assertThrows(
                InvalidTripException.class,
                () -> new ContributionAmount(new BigDecimal("-1.00"))
        );
    }

    @Test
    void shouldRejectContributionWithMoreThanTwoDecimals() {
        assertThrows(
                InvalidTripException.class,
                () -> new ContributionAmount(new BigDecimal("3.456"))
        );
    }
}
