package com.portfolio.clara.domain

import com.portfolio.clara.data.TransactionEntity
import com.portfolio.clara.data.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Test

class FinanceCalculatorTest {
    @Test fun calculatesMonthlyBalanceUsingCents() {
        val transactions = listOf(
            transaction(1, 200_000, TransactionType.INCOME, "Salário"),
            transaction(2, 5_099, TransactionType.EXPENSE, "Alimentação"),
            transaction(3, 10_000, TransactionType.EXPENSE, "Moradia")
        )
        val result = FinanceCalculator.summarize(transactions)
        assertEquals(200_000L, result.incomeCents)
        assertEquals(15_099L, result.expenseCents)
        assertEquals(184_901L, result.balanceCents)
    }

    @Test fun groupsExpensesAndIgnoresIncome() {
        val result = FinanceCalculator.expensesByCategory(
            listOf(
                transaction(1, 100, TransactionType.EXPENSE, "Lazer"),
                transaction(2, 300, TransactionType.EXPENSE, "Lazer"),
                transaction(3, 500, TransactionType.EXPENSE, "Moradia"),
                transaction(4, 999, TransactionType.INCOME, "Salário")
            )
        )
        assertEquals(listOf("Moradia" to 500L, "Lazer" to 400L), result)
    }

    private fun transaction(id: Long, cents: Long, type: String, category: String) =
        TransactionEntity(id, "Teste", cents, type, category, 0L)
}
