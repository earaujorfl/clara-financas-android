package com.portfolio.clara.domain

import com.portfolio.clara.data.TransactionEntity
import com.portfolio.clara.data.TransactionType

data class MonthlySummary(val incomeCents: Long, val expenseCents: Long) {
    val balanceCents: Long get() = incomeCents - expenseCents
}

object FinanceCalculator {
    fun summarize(entries: List<TransactionEntity>) = MonthlySummary(
        incomeCents = entries.filter { it.type == TransactionType.INCOME }.sumOf { it.amountCents },
        expenseCents = entries.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amountCents }
    )

    fun expensesByCategory(entries: List<TransactionEntity>): List<Pair<String, Long>> =
        entries.asSequence()
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.category }
            .map { (category, items) -> category to items.sumOf { it.amountCents } }
            .sortedByDescending { it.second }
}
