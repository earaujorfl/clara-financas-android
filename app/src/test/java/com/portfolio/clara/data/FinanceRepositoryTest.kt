
package com.portfolio.clara.data

import com.portfolio.clara.domain.TransactionDates
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FinanceRepositoryTest {

    @Test
    fun addAndEditKeepTheIdAndMoveTransactionToNewMonth() = runBlocking {
        val dao = FakeDao()
        val repository = FinanceRepository(
            dao,
            FakeCategoryDao(),
            FakeBudgetDao()
        )

        val october = LocalDate.of(2026, 10, 20)
        val november = LocalDate.of(2026, 11, 5)

        repository.add(
            "  Mercado ",
            5000,
            TransactionType.EXPENSE,
            "Alimentação",
            october
        )

        val before = repository.transactions.first().single()

        assertEquals("Mercado", before.description)
        assertEquals(
            october,
            TransactionDates.localDate(before.timestampMillis)
        )

        repository.update(
            before,
            "Restaurante",
            8100,
            TransactionType.EXPENSE,
            "Lazer",
            november
        )

        val after = repository.transactions.first().single()

        assertEquals(before.id, after.id)
        assertEquals("Restaurante", after.description)
        assertEquals(8100L, after.amountCents)
        assertEquals("Lazer", after.category)
        assertEquals(
            november,
            TransactionDates.localDate(after.timestampMillis)
        )
        assertEquals(1, repository.transactions.first().size)
    }

    @Test
    fun invalidUpdateDoesNotOverwriteSavedTransaction() = runBlocking {
        val repository = FinanceRepository(
            FakeDao(),
            FakeCategoryDao(),
            FakeBudgetDao()
        )

        repository.add(
            "Salário",
            1000,
            TransactionType.INCOME,
            "Salário",
            LocalDate.of(2026, 11, 10)
        )

        val entry = repository.transactions.first().single()

        try {
            repository.update(
                entry,
                "",
                0,
                TransactionType.INCOME,
                "Salário",
                LocalDate.of(2026, 12, 1)
            )
            throw AssertionError("Expected invalid update to fail")
        } catch (_: IllegalArgumentException) {
            // A validação impede alterações inválidas.
        }

        assertEquals(entry, repository.transactions.first().single())
    }

    @Test
    fun userCategoriesAreSavedAndDuplicatesRejected() = runBlocking {
        val categories = FakeCategoryDao()

        val repository = FinanceRepository(
            FakeDao(),
            categories,
            FakeBudgetDao()
        )

        assertTrue(
            repository.createCategory(
                TransactionType.EXPENSE,
                "  Pets  "
            )
        )

        assertEquals(
            "Pets",
            repository.customCategories.first().single().name
        )

        assertEquals(
            false,
            repository.createCategory(TransactionType.EXPENSE, "pets")
        )

        assertEquals(
            false,
            repository.createCategory(TransactionType.EXPENSE, "Moradia")
        )

        assertTrue(
            repository.createCategory(TransactionType.INCOME, "Pets")
        )

        assertEquals(2, repository.customCategories.first().size)
    }

    @Test
    fun budgetsCanBeCreatedUpdatedAndDeleted() = runBlocking {
        val repository = FinanceRepository(
            FakeDao(),
            FakeCategoryDao(),
            FakeBudgetDao()
        )

        val november = YearMonth.of(2026, 11)
        val december = YearMonth.of(2026, 12)

        repository.saveBudget(
            november,
            "Alimentação",
            80000L
        )

        repository.saveBudget(
            december,
            "Alimentação",
            90000L
        )

        assertEquals(
            80000L,
            repository.budgetsForMonth(november)
                .first().single().limitCents
        )

        repository.saveBudget(
            november,
            "Alimentação",
            75000L
        )

        val novemberBudgets =
            repository.budgetsForMonth(november).first()

        assertEquals(1, novemberBudgets.size)
        assertEquals(75000L, novemberBudgets.single().limitCents)

        repository.deleteBudget(novemberBudgets.single())

        assertTrue(
            repository.budgetsForMonth(november)
                .first().isEmpty()
        )

        assertEquals(
            90000L,
            repository.budgetsForMonth(december)
                .first().single().limitCents
        )
    }

    private class FakeBudgetDao : BudgetDao {
        private val data =
            MutableStateFlow<List<BudgetEntity>>(emptyList())

        override fun observeByMonth(
            yearMonth: String
        ): Flow<List<BudgetEntity>> =
            data.map { budgets ->
                budgets
                    .filter { it.yearMonth == yearMonth }
                    .sortedBy { it.category }
            }

        override suspend fun save(budget: BudgetEntity) {
            data.value = data.value.filterNot {
                it.yearMonth == budget.yearMonth &&
                        it.category == budget.category
            } + budget
        }

        override suspend fun delete(budget: BudgetEntity) {
            data.value = data.value.filterNot {
                it.yearMonth == budget.yearMonth &&
                        it.category == budget.category
            }
        }
    }

    private class FakeCategoryDao : CategoryDao {
        private val data =
            MutableStateFlow<List<CustomCategoryEntity>>(emptyList())

        override fun observeAll(): Flow<List<CustomCategoryEntity>> =
            data

        override suspend fun countSameName(
            type: String,
            name: String
        ): Int =
            data.value.count {
                it.type == type &&
                        it.name.equals(name, ignoreCase = true)
            }

        override suspend fun insert(
            category: CustomCategoryEntity
        ): Long {
            if (countSameName(category.type, category.name) > 0) {
                return -1L
            }

            val id = (data.value.maxOfOrNull { it.id } ?: 0L) + 1L

            data.value = data.value + category.copy(id = id)
            return id
        }
    }

    private class FakeDao : TransactionDao {
        private val data =
            MutableStateFlow<List<TransactionEntity>>(emptyList())

        override fun observeAll(): Flow<List<TransactionEntity>> =
            data

        override suspend fun insert(
            transaction: TransactionEntity
        ) {
            val id = (data.value.maxOfOrNull { it.id } ?: 0L) + 1L
            data.value = data.value + transaction.copy(id = id)
        }

        override suspend fun update(
            transaction: TransactionEntity
        ) {
            data.value = data.value.map {
                if (it.id == transaction.id) transaction else it
            }
        }

        override suspend fun delete(
            transaction: TransactionEntity
        ) {
            data.value = data.value.filterNot {
                it.id == transaction.id
            }
        }
    }
}
