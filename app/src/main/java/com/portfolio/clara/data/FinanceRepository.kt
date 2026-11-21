package com.portfolio.clara.data

import kotlinx.coroutines.flow.Flow

class FinanceRepository(private val dao: TransactionDao) {
    val transactions: Flow<List<TransactionEntity>> = dao.observeAll()

    suspend fun add(description: String, amountCents: Long, type: String, category: String) {
        require(description.isNotBlank())
        require(amountCents > 0)
        require(type == TransactionType.INCOME || type == TransactionType.EXPENSE)
        dao.insert(
            TransactionEntity(
                description = description.trim(),
                amountCents = amountCents,
                type = type,
                category = category,
                timestampMillis = System.currentTimeMillis()
            )
        )
    }

    suspend fun delete(entry: TransactionEntity) = dao.delete(entry)
}
