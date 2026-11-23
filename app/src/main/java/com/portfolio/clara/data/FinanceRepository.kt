package com.portfolio.clara.data

import com.portfolio.clara.domain.CategoryOptions
import com.portfolio.clara.domain.TransactionDates
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth

class FinanceRepository(
    private val dao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val budgetDao: BudgetDao
) {

    val transactions: Flow<List<TransactionEntity>> = dao.observeAll()
    val customCategories: Flow<List<CustomCategoryEntity>> = categoryDao.observeAll()

    fun budgetsForMonth(
        month: YearMonth
    ): Flow<List<BudgetEntity>> {
        return budgetDao.observeByMonth(month.toString())
    }

    suspend fun saveBudget(
        month: YearMonth,
        category: String,
        limitCents: Long
    ) {
        require(category.isNotBlank()) {
            "A categoria é obrigatória"
        }

        require(limitCents > 0) {
            "O limite deve ser maior que zero"
        }

        budgetDao.save(
            BudgetEntity(
                yearMonth = month.toString(),
                category = category.trim(),
                limitCents = limitCents
            )
        )
    }

    suspend fun deleteBudget(
        budget: BudgetEntity
    ) {
        budgetDao.delete(budget)
    }

    suspend fun createCategory(type: String, name: String): Boolean {
        val normalized = name.trim().replace(Regex("\\s+"), " ")
        if (normalized.isEmpty() || normalized.length > 40 || CategoryOptions.builtIn(type).isEmpty()) return false
        if (CategoryOptions.builtIn(type).any { it.equals(normalized, ignoreCase = true) }) return false
        if (categoryDao.countSameName(type, normalized) > 0) return false
        return categoryDao.insert(CustomCategoryEntity(type = type, name = normalized)) != -1L
    }

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
