package com.portfolio.clara.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.portfolio.clara.data.FinanceRepository
import com.portfolio.clara.data.TransactionEntity
import java.time.LocalDate
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import com.portfolio.clara.data.BudgetEntity
import com.portfolio.clara.domain.BudgetCalculator
import com.portfolio.clara.domain.BudgetProgress
import java.time.YearMonth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine


class FinanceViewModel(private val repository: FinanceRepository) : ViewModel() {
    val transactions = repository.transactions.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    val customCategories = repository.customCategories.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    fun createCategory(type: String, name: String) {
        viewModelScope.launch { repository.createCategory(type, name) }
    }

    fun add(description: String, cents: Long, type: String, category: String, date: LocalDate) {
        viewModelScope.launch { repository.add(description, cents, type, category, date) }
    }

    fun update(
        entry: TransactionEntity,
        description: String,
        cents: Long,
        type: String,
        category: String,
        date: LocalDate
    ) {
        viewModelScope.launch { repository.update(entry, description, cents, type, category, date) }
    }

    fun delete(entry: TransactionEntity) {
        viewModelScope.launch { repository.delete(entry) }
    }

    fun observeBudgetProgress(
        month: YearMonth
    ): Flow<List<BudgetProgress>> {

        return combine(
            repository.budgetsForMonth(month),
            repository.transactions
        ) { budgets, transactions ->

            budgets.map { budget ->
                BudgetCalculator.calculate(
                    budget = budget,
                    transactions = transactions
                )
            }
        }
    }

    fun saveBudget(
        month: YearMonth,
        category: String,
        limitCents: Long
    ) {
        viewModelScope.launch {
            repository.saveBudget(
                month = month,
                category = category,
                limitCents = limitCents
            )
        }
    }

    fun deleteBudget(budget: BudgetEntity) {
        viewModelScope.launch {
            repository.deleteBudget(budget)
        }
    }

    companion object {
        fun factory(repository: FinanceRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { FinanceViewModel(repository) }
        }
    }
}
