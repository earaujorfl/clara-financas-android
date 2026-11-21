package com.portfolio.clara.data

import com.portfolio.clara.domain.TransactionDates
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

class FinanceRepository(private val dao: TransactionDao) {
    val transactions: Flow<List<TransactionEntity>> = dao.observeAll()

    suspend fun add(
        description: String,
        amountCents: Long,
        type: String,
        category: String,
        date: LocalDate
    ) {
        validate(description, amountCents, type, category)
        dao.insert(
            TransactionEntity(
                description = description.trim(),
                amountCents = amountCents,
                type = type,
                category = category,
                timestampMillis = TransactionDates.atStartOfDayMillis(date)
            )
        )
    }

    suspend fun update(
        original: TransactionEntity,
        description: String,
        amountCents: Long,
        type: String,
        category: String,
        date: LocalDate
    ) {
        require(original.id > 0) { "Lançamento precisa ter um ID salvo" }
        validate(description, amountCents, type, category)
        dao.update(
            original.copy(
                description = description.trim(),
                amountCents = amountCents,
                type = type,
                category = category,
                timestampMillis = TransactionDates.atStartOfDayMillis(date)
            )
        )
    }

    suspend fun delete(entry: TransactionEntity) = dao.delete(entry)

    private fun validate(description: String, amountCents: Long, type: String, category: String) {
        require(description.isNotBlank())
        require(amountCents > 0)
        require(type == TransactionType.INCOME || type == TransactionType.EXPENSE)
        require(category.isNotBlank())
    }
}
