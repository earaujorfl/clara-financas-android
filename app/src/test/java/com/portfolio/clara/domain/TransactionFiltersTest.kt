package com.portfolio.clara.domain

import com.portfolio.clara.data.TransactionEntity
import com.portfolio.clara.data.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Test

class TransactionFiltersTest {
    private val transactions = listOf(
        TransactionEntity(1, "Mercado", 12500, TransactionType.EXPENSE, "Alimentação", 1),
        TransactionEntity(2, "Salário", 320000, TransactionType.INCOME, "Salário", 2),
        TransactionEntity(3, "Farmácia", 5000, TransactionType.EXPENSE, "Saúde", 3)
    )

    @Test fun queryIgnoresAccentsAndCase() {
        assertEquals(listOf(3L), TransactionFilters.apply(transactions, "FARMACIA").map { it.id })
        assertEquals(listOf(1L), TransactionFilters.apply(transactions, "alimentacao").map { it.id })
    }

    @Test fun combinesTypeCategoryAndDescriptionSearch() {
        assertEquals(
            listOf(1L),
            TransactionFilters.apply(transactions, "merc", TransactionType.EXPENSE, "Alimentação").map { it.id }
        )
        assertEquals(
            emptyList<Long>(),
            TransactionFilters.apply(transactions, "merc", TransactionType.INCOME).map { it.id }
        )
    }

    @Test fun blankQueryDoesNotHideOtherTransactions() {
        assertEquals(3, TransactionFilters.apply(transactions, "").size)
    }
}
