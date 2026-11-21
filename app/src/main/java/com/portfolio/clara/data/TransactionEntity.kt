package com.portfolio.clara.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Monetary amounts are stored as positive integer cents. */
@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val description: String,
    val amountCents: Long,
    val type: String,
    val category: String,
    val timestampMillis: Long
)

object TransactionType {
    const val INCOME = "INCOME"
    const val EXPENSE = "EXPENSE"
}
