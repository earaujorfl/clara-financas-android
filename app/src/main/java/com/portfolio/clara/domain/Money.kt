package com.portfolio.clara.domain

import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

object Money {
    private val validBrl = Regex("""^(\d+(,\d{1,2})?|\d{1,3}(\.\d{3})+(,\d{1,2})?)$""")

    fun parseCents(input: String): Long? {
        val normalized = input.trim()
        if (!validBrl.matches(normalized)) return null
        return try {
            val decimal = BigDecimal(normalized.replace(".", "").replace(',', '.'))
            decimal.movePointRight(2).longValueExact().takeIf { it > 0 }
        } catch (_: ArithmeticException) {
            null
        } catch (_: NumberFormatException) {
            null
        }
    }

    fun format(cents: Long): String =
        NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"))
            .format(BigDecimal.valueOf(cents, 2))
}
