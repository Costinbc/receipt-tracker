package com.example.receipttracker.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.receipttracker.data.Expense
import com.example.receipttracker.data.FakeExpenseStore
import com.example.receipttracker.data.formattedTotal
import com.example.receipttracker.ui.theme.ReceiptTrackerTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val dateFormat = DateTimeFormatter.ofPattern("d MMM yyyy")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseListScreen(
    expenses: List<Expense>,
    onExpenseClick: (Long) -> Unit,
    onAddClick: () -> Unit,
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Expenses") }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = onAddClick) { Text("+ Add expense") }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 88.dp),
        ) {
            item { MonthSummary(expenses) }
            items(expenses, key = { it.id }) { expense ->
                ExpenseRow(expense, onClick = { onExpenseClick(expense.id) })
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun MonthSummary(expenses: List<Expense>) {
    val now = LocalDate.now()
    // Only RON for now: mixing currencies needs exchange rates (a network concern, later).
    val monthTotal = expenses
        .filter { it.currency == "RON" && it.date.month == now.month && it.date.year == now.year }
        .sumOf { it.total }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Spent this month", style = MaterialTheme.typography.labelLarge)
            Text(
                "%.2f RON".format(monthTotal),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun ExpenseRow(expense: Expense, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(Modifier.weight(1f)) {
            Text(expense.merchant, style = MaterialTheme.typography.titleMedium)
            Text(
                "${expense.category.label} · ${expense.date.format(dateFormat)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(expense.formattedTotal(), style = MaterialTheme.typography.titleMedium)
    }
}

@Preview(showBackground = true)
@Composable
private fun ExpenseListPreview() {
    ReceiptTrackerTheme {
        ExpenseListScreen(FakeExpenseStore.expenses, onExpenseClick = {}, onAddClick = {})
    }
}
