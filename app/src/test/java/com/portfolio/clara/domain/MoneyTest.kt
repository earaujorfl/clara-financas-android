package com.portfolio.clara.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MoneyTest {
    @Test fun parsesBrazilianMoneyWithoutFloatingPointErrors() {
        assertEquals(1_234_056L, Money.parseCents("12.340,56"))
        assertEquals(5_900L, Money.parseCents("59"))
        assertEquals(5_990L, Money.parseCents("59,90"))
        assertEquals(1L, Money.parseCents("0,01"))
    }

    @Test fun editAmountRoundTripsWithoutPrecisionLoss() {
        val cents = 123_456L
        assertEquals("1234,56", Money.editable(cents))
        assertEquals(cents, Money.parseCents(Money.editable(cents)))
    }

    @Test fun rejectsInvalidOrNonpositiveMoney() {
        assertNull(Money.parseCents(""))
        assertNull(Money.parseCents("10,999"))
        assertNull(Money.parseCents("-3,00"))
        assertNull(Money.parseCents("0"))
        assertNull(Money.parseCents("10.30"))
    }
}
