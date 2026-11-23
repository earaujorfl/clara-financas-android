package com.portfolio.clara.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import android.app.DatePickerDialog
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.portfolio.clara.data.CustomCategoryEntity
import com.portfolio.clara.data.TransactionEntity
import com.portfolio.clara.data.TransactionType
import com.portfolio.clara.domain.CategoryOptions
import com.portfolio.clara.domain.TransactionFilters
import com.portfolio.clara.domain.FinanceCalculator
import com.portfolio.clara.domain.Money
import java.time.LocalDate
import com.portfolio.clara.domain.TransactionDates
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.material.icons.filled.AccountBalanceWallet

private val darkBackground = Color(0xFF101A18)
private val darkCard = Color(0xFF1C2A25)
private val mint = Color(0xFFA4E0BE)
private val coral = Color(0xFFFFAD9B)
private val palette = darkColorScheme(
    primary = mint, secondary = coral,
    primaryContainer = Color(0xFF2D493E), onPrimaryContainer = mint,
    secondaryContainer = Color(0xFF334A40), onSecondaryContainer = mint,
    background = darkBackground, surface = darkBackground,
    surfaceContainer = darkCard, onSurface = Color(0xFFF2F8F3),
    onPrimary = Color(0xFF10221B)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClaraApp(viewModel: FinanceViewModel) {
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val customCategories by viewModel.customCategories.collectAsStateWithLifecycle()
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var monthKey by rememberSaveable { mutableStateOf(YearMonth.now().toString()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<TransactionEntity?>(null) }
    var deleting by remember { mutableStateOf<TransactionEntity?>(null) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var typeFilter by rememberSaveable { mutableStateOf(TransactionFilters.ALL) }
    var categoryFilter by rememberSaveable { mutableStateOf<String?>(null) }
    val month = YearMonth.parse(monthKey)
    val entries = remember(transactions, monthKey) {
        transactions.filter {
            YearMonth.from(TransactionDates.localDate(it.timestampMillis)) == month
        }
    }
    val summary = remember(entries) { FinanceCalculator.summarize(entries) }
    val visibleHistory = remember(entries, searchQuery, typeFilter, categoryFilter) {
        TransactionFilters.apply(entries, searchQuery, typeFilter, categoryFilter)
    }

    MaterialTheme(colorScheme = palette) {
        Scaffold(
            containerColor = darkBackground,
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("clara.", fontWeight = FontWeight.ExtraBold)
                            Text("Seu dinheiro, com clareza", style = MaterialTheme.typography.labelSmall)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = darkBackground)
                )
            },
            floatingActionButton = {
                if(selectedTab != 3) {
                    FloatingActionButton(
                        onClick = { showAddDialog = true },
                        containerColor = mint,
                        contentColor = darkBackground
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Adicionar lançamento"
                        )
                    }
                }
            },
            bottomBar = {
                NavigationBar(containerColor = darkCard) {
                    NavigationBarItem(
                        selected = selectedTab == 0, onClick = { selectedTab = 0 },
                        icon = { Icon(Icons.Default.Home, contentDescription = null) },
                        label = { Text("Início") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1, onClick = { selectedTab = 1 },
                        icon = { Icon(Icons.Default.List, contentDescription = null) },
                        label = { Text("Lançamentos") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 2, onClick = { selectedTab = 2 },
                        icon = { Icon(Icons.Default.BarChart, contentDescription = null) },
                        label = { Text("Análise") }
                    )

                    NavigationBarItem(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        icon = {
                            Icon(
                                Icons.Default.AccountBalanceWallet,
                                contentDescription = null
                            )
                        },
                        label = {
                            Text("Orçamentos")
                        }
                    )

                }
            }
        ) { padding ->
            Column(Modifier.fillMaxSize().padding(padding)) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = { monthKey = month.minusMonths(1).toString() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Mês anterior")
                    }
                    Text(
                        month.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.forLanguageTag("pt-BR"))),
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { monthKey = month.plusMonths(1).toString() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, "Próximo mês")
                    }
                }
                when (selectedTab) {
                    0 -> {
                        Column(
                            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text("Visão geral", style = MaterialTheme.typography.headlineSmall)
                            SummaryCard("Saldo do mês", summary.balanceCents, mint)
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Box(Modifier.weight(1f)) {
                                    SummaryCard("Receitas", summary.incomeCents, mint)
                                }
                                Box(Modifier.weight(1f)) {
                                    SummaryCard("Despesas", summary.expenseCents, coral)
                                }
                            }
                            Text("Últimos lançamentos", fontWeight = FontWeight.Bold)
                            if (entries.isEmpty()) Text("Nenhum lançamento neste mês.")
                            entries.take(3).forEach { EntryCard(it) }
                            if (entries.size > 3) {
                                TextButton(onClick = { selectedTab = 1 }) {
                                    Text("Ver todos os lançamentos")
                                }
                            }
                            Spacer(Modifier.height(96.dp))
                        }
                    }
                    1 -> {
                        Column(Modifier.fillMaxSize()) {
                            Column(
                                Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("Buscar lançamento") },
                                    singleLine = true,
                                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                    trailingIcon = if (searchQuery.isNotEmpty()) {
                                        {
                                            IconButton(onClick = { searchQuery = "" }) {
                                                Icon(Icons.Default.Close, "Limpar busca")
                                            }
                                        }
                                    } else null
                                )
                                Row(
                                    Modifier.horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf(
                                        TransactionFilters.ALL to "Todos",
                                        TransactionType.INCOME to "Receitas",
                                        TransactionType.EXPENSE to "Despesas"
                                    ).forEach { (value, label) ->
                                        FilterChip(
                                            selected = typeFilter == value,
                                            onClick = {
                                                typeFilter = value
                                                categoryFilter = null
                                            },
                                            label = { Text(label) }
                                        )
                                    }
                                }
                                val categoriesInMonth = entries
                                    .filter { typeFilter == TransactionFilters.ALL || it.type == typeFilter }
                                    .map { it.category }
                                    .distinct()
                                    .sorted()
                                var categoryMenuOpen by remember { mutableStateOf(false) }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box {
                                        OutlinedButton(onClick = { categoryMenuOpen = true }) {
                                            Text(categoryFilter ?: "Todas as categorias")
                                        }
                                        DropdownMenu(
                                            expanded = categoryMenuOpen,
                                            onDismissRequest = { categoryMenuOpen = false }
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text("Todas as categorias") },
                                                onClick = {
                                                    categoryFilter = null
                                                    categoryMenuOpen = false
                                                }
                                            )
                                            categoriesInMonth.forEach { option ->
                                                DropdownMenuItem(
                                                    text = { Text(option) },
                                                    onClick = {
                                                        categoryFilter = option
                                                        categoryMenuOpen = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        "${visibleHistory.size} encontrados",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                            if (visibleHistory.isEmpty()) {
                                Column(
                                    Modifier.weight(1f).fillMaxWidth().padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        if (entries.isEmpty()) "Nenhum lançamento neste mês."
                                        else "Nenhum lançamento encontrado."
                                    )
                                    if (entries.isNotEmpty()) {
                                        TextButton(onClick = {
                                            searchQuery = ""
                                            typeFilter = TransactionFilters.ALL
                                            categoryFilter = null
                                        }) { Text("Limpar filtros") }
                                    }
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(
                                        start = 20.dp, end = 20.dp, top = 8.dp, bottom = 108.dp
                                    ),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(visibleHistory, key = { it.id }) { entry ->
                                        EntryCard(
                                            entry,
                                            onEdit = { editing = it },
                                            onDelete = { deleting = it }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    2 -> {
                        val groups = remember(entries) {
                            FinanceCalculator.expensesByCategory(entries)
                        }
                        Column(
                            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text("Gastos por categoria", style = MaterialTheme.typography.headlineSmall)
                            if (groups.isEmpty()) Text("Registre despesas para ver a análise.")
                            groups.forEach { (category, amount) ->
                                Text(category + " • " + Money.format(amount))
                                val fraction = (amount.toFloat() / summary.expenseCents.coerceAtLeast(1L))
                                    .coerceIn(0f, 1f)
                                Box(
                                    Modifier.fillMaxWidth().height(8.dp)
                                        .clip(RoundedCornerShape(50))
                                        .background(Color(0xFF30443A))
                                ) {
                                    Box(Modifier.fillMaxWidth(fraction).fillMaxHeight().background(mint))
                                }
                            }
                        }
                    }

                    else -> {
                        BudgetScreen(
                            month = month,
                            viewModel = viewModel
                        )
                    }

                }
            }
        }
        if (showAddDialog) {
            val suggestedDate = if (month == YearMonth.now()) LocalDate.now() else month.atDay(1)
            TransactionEditorDialog(
                initialEntry = null,
                defaultDate = suggestedDate,
                customCategories = customCategories,
                onCreateCategory = { type, name -> viewModel.createCategory(type, name) },
                onDismiss = { showAddDialog = false },
                onSave = { description, cents, type, category, date ->
                    viewModel.add(description, cents, type, category, date)
                    monthKey = YearMonth.from(date).toString()
                    showAddDialog = false
                }
            )
        }
        editing?.let { entry ->
            TransactionEditorDialog(
                initialEntry = entry,
                defaultDate = TransactionDates.localDate(entry.timestampMillis),
                customCategories = customCategories,
                onCreateCategory = { type, name -> viewModel.createCategory(type, name) },
                onDismiss = { editing = null },
                onSave = { description, cents, type, category, date ->
                    viewModel.update(entry, description, cents, type, category, date)
                    monthKey = YearMonth.from(date).toString()
                    editing = null
                }
            )
        }
        deleting?.let { entry ->
            AlertDialog(
                onDismissRequest = { deleting = null },
                title = { Text("Excluir lançamento?") },
                text = { Text("A exclusão é permanente.") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.delete(entry)
                        deleting = null
                    }) { Text("Excluir") }
                },
                dismissButton = {
                    TextButton(onClick = { deleting = null }) { Text("Cancelar") }
                }
            )
        }
    }
}

@Composable
private fun SummaryCard(label: String, cents: Long, accent: Color) {
    Card(
        colors = CardDefaults.cardColors(containerColor = darkCard),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(label, style = MaterialTheme.typography.labelLarge)
            Text(Money.format(cents), color = accent, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun EntryCard(
    entry: TransactionEntity,
    onEdit: ((TransactionEntity) -> Unit)? = null,
    onDelete: ((TransactionEntity) -> Unit)? = null
) {
    val date = TransactionDates.localDate(entry.timestampMillis)
        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    Card(
        colors = CardDefaults.cardColors(containerColor = darkCard),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(entry.description, fontWeight = FontWeight.Bold)
                Text(entry.category + " • " + date, style = MaterialTheme.typography.labelSmall)
            }
            val sign = if (entry.type == TransactionType.INCOME) "+ " else "− "
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    sign + Money.format(entry.amountCents),
                    color = if (entry.type == TransactionType.INCOME) mint else coral
                )
                if (onEdit != null || onDelete != null) {
                    Row {
                        if (onEdit != null) {
                            IconButton(onClick = { onEdit(entry) }, modifier = Modifier.size(40.dp)) {
                                Icon(Icons.Default.Edit, "Editar ${entry.description}")
                            }
                        }
                        if (onDelete != null) {
                            IconButton(onClick = { onDelete(entry) }, modifier = Modifier.size(40.dp)) {
                                Icon(Icons.Default.Delete, "Excluir lançamento")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionEditorDialog(
    initialEntry: TransactionEntity?,
    defaultDate: LocalDate,
    customCategories: List<CustomCategoryEntity>,
    onCreateCategory: (String, String) -> Unit,
    onDismiss: () -> Unit,
    onSave: (String, Long, String, String, LocalDate) -> Unit
) {
    var description by remember(initialEntry?.id) {
        mutableStateOf(initialEntry?.description.orEmpty())
    }
    var amount by remember(initialEntry?.id) {
        mutableStateOf(initialEntry?.let { Money.editable(it.amountCents) }.orEmpty())
    }
    var type by remember(initialEntry?.id) {
        mutableStateOf(initialEntry?.type ?: TransactionType.EXPENSE)
    }
    var category by remember(initialEntry?.id) {
        mutableStateOf(initialEntry?.category ?: "Alimentação")
    }
    var selectedDate by remember(initialEntry?.id, defaultDate) {
        mutableStateOf(defaultDate)
    }
    var creatingCategory by remember(initialEntry?.id) { mutableStateOf(false) }
    var newCategoryName by remember(initialEntry?.id) { mutableStateOf("") }
    val context = LocalContext.current
    val categories = (
        CategoryOptions.builtIn(type) +
            customCategories.filter { it.type == type }.map { it.name } +
            category
    ).distinct()
    val newCategory = newCategoryName.trim().replace(Regex("\\s+"), " ")
    val canCreateCategory = newCategory.isNotBlank() && newCategory.length <= 40 &&
        categories.none { it.equals(newCategory, ignoreCase = true) }
    val cents = Money.parseCents(amount)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialEntry == null) "Novo lançamento" else "Editar lançamento") },
        text = {
            Column(
                modifier = Modifier.heightIn(max = 480.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = description, onValueChange = { description = it },
                    label = { Text("Descrição") }, singleLine = true
                )
                OutlinedTextField(
                    value = amount, onValueChange = { amount = it },
                    label = { Text("Valor em R$") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = type == TransactionType.EXPENSE,
                        onClick = {
                            type = TransactionType.EXPENSE
                            category = "Alimentação"
                        },
                        label = { Text("Despesa") }
                    )
                    FilterChip(
                        selected = type == TransactionType.INCOME,
                        onClick = {
                            type = TransactionType.INCOME
                            category = "Salário"
                        },
                        label = { Text("Receita") }
                    )
                }
                Text("Categoria")
                @OptIn(ExperimentalLayoutApi::class)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    categories.forEach { option ->
                        FilterChip(
                            selected = category == option,
                            onClick = { category = option },
                            label = { Text(option) }
                        )
                    }
                }
                if (creatingCategory) {
                    OutlinedTextField(
                        value = newCategoryName,
                        onValueChange = { newCategoryName = it },
                        label = { Text("Nome da nova categoria") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (newCategoryName.isNotBlank() && !canCreateCategory) {
                        Text(
                            "Use um nome diferente, com até 40 caracteres.",
                            style = MaterialTheme.typography.bodySmall,
                            color = coral
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = {
                            creatingCategory = false
                            newCategoryName = ""
                        }) { Text("Cancelar") }
                        Button(
                            enabled = canCreateCategory,
                            onClick = {
                                onCreateCategory(type, newCategory)
                                category = newCategory
                                newCategoryName = ""
                                creatingCategory = false
                            }
                        ) { Text("Criar categoria") }
                    }
                } else {
                    TextButton(onClick = { creatingCategory = true }) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Nova categoria")
                    }
                }
                Text("Data do lançamento")
                OutlinedButton(onClick = {
                    DatePickerDialog(
                        context,
                        { _, year, monthOfYear, dayOfMonth ->
                            selectedDate = LocalDate.of(year, monthOfYear + 1, dayOfMonth)
                        },
                        selectedDate.year,
                        selectedDate.monthValue - 1,
                        selectedDate.dayOfMonth
                    ).show()
                }) {
                    Icon(Icons.Default.DateRange, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(selectedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = description.isNotBlank() && cents != null && category.isNotBlank(),
                onClick = {
                    if (cents != null) onSave(description, cents, type, category, selectedDate)
                }
            ) { Text(if (initialEntry == null) "Salvar" else "Salvar alterações") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
