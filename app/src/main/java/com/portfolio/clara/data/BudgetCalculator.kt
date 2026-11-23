
package com.portfolio.clara.domain

import com.portfolio.clara.data.BudgetEntity
import com.portfolio.clara.data.TransactionEntity
import com.portfolio.clara.data.TransactionType
import java.time.YearMonth

data class BudgetProgress(
    val category: String,
    val limitCents: Long,
    val spentCents: Long
) {
    val remainingCents: Long
        get() = limitCents - spentCents

    val percentageUsed: Double
        get() = if (limitCents > 0) {
            spentCents.toDouble() / limitCents * 100
        } else {
            0.0
        }

    val progress: Float
        get() = if (limitCents > 0) {
            (spentCents.toFloat() / limitCents.toFloat())
                .coerceIn(0f, 1f)
        } else {
            0f
        }

    val isExceeded: Boolean
        get() = spentCents > limitCents
}

object BudgetCalculator {

    fun calculate(
        budget: BudgetEntity,
        transactions: List<TransactionEntity>
    ): BudgetProgress {

        val month = YearMonth.parse(budget.yearMonth)

        val spent = transactions
            .filter { transaction ->
                transaction.type == TransactionType.EXPENSE &&
                        transaction.category == budget.category &&
                        YearMonth.from(
                            TransactionDates.localDate(
                                transaction.timestampMillis
                            )
                        ) == month
            }
            .sumOf { it.amountCents }

        return BudgetProgress(
            category = budget.category,
            limitCents = budget.limitCents,
            spentCents = spent
        )
    }
}
