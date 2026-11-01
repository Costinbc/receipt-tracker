package com.example.receipttracker.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.receipttracker.data.Category
import com.example.receipttracker.data.Expense
import com.example.receipttracker.ui.theme.ReceiptTrackerTheme
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddExpenseScreen(onSave: (Expense) -> Unit, onBack: () -> Unit) {
    var merchant by remember { mutableStateOf("") }
    var total by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(Category.OTHER) }
    var note by remember { mutableStateOf("") }

    val parsedTotal = total.replace(',', '.').toDoubleOrNull()?.let { Math.round(it * 100) }
    val canSave = merchant.isNotBlank() && parsedTotal != null && parsedTotal > 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add expense") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Cancel") } },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = merchant,
                onValueChange = { merchant = it },
                label = { Text("Merchant") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = total,
                onValueChange = { total = it },
                label = { Text("Total (RON)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
            )

            Text("Category", style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Category.entries.forEach { option ->
                    FilterChip(
                        selected = option == category,
                        onClick = { category = option },
                        label = { Text(option.label) },
                    )
                }
            }

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note (optional)") },
                modifier = Modifier.fillMaxWidth(),
            )

            Button(
                onClick = {
                    onSave(
                        Expense(
                            id = 0, // the store assigns the real id
                            merchant = merchant.trim(),
                            totalMinor = parsedTotal!!,
                            currency = "RON",
                            date = LocalDate.now(),
                            category = category,
                            note = note.trim(),
                        )
                    )
                },
                enabled = canSave,
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Save") }

            // Later: "Scan receipt" opens the camera and fills these fields with OCR.
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AddExpensePreview() {
    ReceiptTrackerTheme {
        AddExpenseScreen(onSave = {}, onBack = {})
    }
}
