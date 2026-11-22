package com.portfolio.clara.domain

import com.portfolio.clara.data.TransactionType

object CategoryOptions {
    fun builtIn(type: String): List<String> = when (type) {
        TransactionType.EXPENSE -> listOf("Alimentação", "Moradia", "Transporte", "Saúde", "Lazer", "Outros")
        TransactionType.INCOME -> listOf("Salário", "Freelance", "Investimentos", "Outros")
        else -> emptyList()
    }
}
