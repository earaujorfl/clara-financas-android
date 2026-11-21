package com.portfolio.clara.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.portfolio.clara.data.FinanceRepository
import com.portfolio.clara.data.TransactionEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FinanceViewModel(private val repository: FinanceRepository) : ViewModel() {
    val transactions = repository.transactions.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    fun add(description: String, cents: Long, type: String, category: String) {
        viewModelScope.launch { repository.add(description, cents, type, category) }
    }

    fun delete(entry: TransactionEntity) {
        viewModelScope.launch { repository.delete(entry) }
    }

    companion object {
        fun factory(repository: FinanceRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { FinanceViewModel(repository) }
        }
    }
}
