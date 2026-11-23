
package com.portfolio.clara.data

import androidx.room.Entity

@Entity(
    tableName = "monthly_budgets",
    primaryKeys = ["yearMonth", "category"]
)
data class BudgetEntity(
    val yearMonth: String,
    val category: String,
    val limitCents: Long
)
