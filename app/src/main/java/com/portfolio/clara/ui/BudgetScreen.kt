
package com.portfolio.clara.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.portfolio.clara.data.BudgetEntity
import com.portfolio.clara.data.TransactionType
import com.portfolio.clara.domain.BudgetProgress
import com.portfolio.clara.domain.CategoryOptions
import com.portfolio.clara.domain.Money
import java.time.YearMonth
import java.util.Locale

@Composable
fun BudgetScreen(
    month: YearMonth,
    viewModel: FinanceViewModel
) {
    val budgetFlow = remember(month, viewModel) {
        viewModel.observeBudgetProgress(month)
    }

    val budgets by budgetFlow.collectAsStateWithLifecycle(
        initialValue = emptyList<BudgetProgress>()
    )

    val customCategories by
    viewModel.customCategories.collectAsStateWithLifecycle()

    val categories = (
            CategoryOptions.builtIn(TransactionType.EXPENSE) +
                    customCategories
                        .filter { it.type == TransactionType.EXPENSE }
                        .map { it.name }
            ).distinct()

    var showNew by remember { mutableStateOf(false) }
    var editing by remember {
        mutableStateOf<BudgetProgress?>(null)
    }
    var deleting by remember {
        mutableStateOf<BudgetProgress?>(null)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Orçamentos mensais",
            style = MaterialTheme.typography.headlineSmall
        )

        Button(onClick = { showNew = true }) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Novo orçamento")
        }

        if (budgets.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Nenhum orçamento neste mês.")
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                items(budgets, key = { it.category }) { budget ->
                    BudgetCard(
                        budget = budget,
                        onEdit = { editing = budget },
                        onDelete = { deleting = budget }
                    )
                }
            }
        }
    }

    if (showNew || editing != null) {
        BudgetEditorDialog(
            initial = editing,
            categories = categories,
            onDismiss = {
                showNew = false
                editing = null
            },
            onSave = { category, cents ->
                viewModel.saveBudget(month, category, cents)
                showNew = false
                editing = null
            }
        )
    }

    deleting?.let { budget ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text("Excluir orçamento?") },
            text = {
                Text("O limite de ${budget.category} será removido.")
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteBudget(
                        BudgetEntity(
                            yearMonth = month.toString(),
                            category = budget.category,
                            limitCents = budget.limitCents
                        )
                    )
                    deleting = null
                }) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                TextButton(onClick = { deleting = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun BudgetCard(
    budget: BudgetProgress,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val color = if (budget.isExceeded) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.primary
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                budget.category,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                "${Money.format(budget.spentCents)} de " +
                        Money.format(budget.limitCents)
            )

            LinearProgressIndicator(
                progress = { budget.progress },
                modifier = Modifier.fillMaxWidth(),
                color = color
            )

            Text(
                String.format(
                    Locale.forLanguageTag("pt-BR"),
                    "%.1f%% utilizado",
                    budget.percentageUsed
                )
            )

            Text(
                if (budget.isExceeded) {
                    "Limite ultrapassado em " +
                            Money.format(-budget.remainingCents)
                } else {
                    Money.format(budget.remainingCents) +
                            " disponíveis"
                },
                color = color
            )

            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Editar orçamento"
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Excluir orçamento"
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BudgetEditorDialog(
    initial: BudgetProgress?,
    categories: List<String>,
    onDismiss: () -> Unit,
    onSave: (String, Long) -> Unit
) {
    var category by remember(initial?.category) {
        mutableStateOf(
            initial?.category ?: categories.firstOrNull().orEmpty()
        )
    }

    var amount by remember(initial?.category) {
        mutableStateOf(
            initial?.let { Money.editable(it.limitCents) }.orEmpty()
        )
    }

    val cents = Money.parseCents(amount)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (initial == null) "Novo orçamento"
                else "Editar orçamento"
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Categoria")

                if (initial != null) {
                    Text(category)
                } else {
                    FlowRow(
                        horizontalArrangement =
                            Arrangement.spacedBy(8.dp)
                    ) {
                        categories.forEach { option ->
                            FilterChip(
                                selected = category == option,
                                onClick = { category = option },
                                label = { Text(option) }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Limite em R$") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = category.isNotBlank() && cents != null,
                onClick = {
                    if (cents != null) onSave(category, cents)
                }
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
