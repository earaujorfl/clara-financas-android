package com.portfolio.clara.domain

import com.portfolio.clara.data.TransactionEntity
import java.text.Normalizer
import java.util.Locale

/** Filters the history list without changing dashboard totals. */
object TransactionFilters {
    const val ALL = "ALL"

    fun apply(
        entries: List<TransactionEntity>,
        query: String,
        type: String = ALL,
        category: String? = null
    ): List<TransactionEntity> {
        val normalizedQuery = normalize(query.trim())
        return entries.filter { entry ->
            (type == ALL || entry.type == type) &&
                (category == null || entry.category.equals(category, ignoreCase = true)) &&
                (normalizedQuery.isEmpty() ||
                    normalize(entry.description).contains(normalizedQuery) ||
                    normalize(entry.category).contains(normalizedQuery))
        }
    }

    private fun normalize(value: String): String =
        Normalizer.normalize(value, Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")
            .lowercase(Locale.ROOT)
}
