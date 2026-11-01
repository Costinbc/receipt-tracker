package com.example.receipttracker.data

import java.time.LocalDate

enum class Category(val label: String) {
    FOOD("Food"),
    GROCERIES("Groceries"),
    TRANSPORT("Transport"),
    SHOPPING("Shopping"),
    BILLS("Bills"),
    OTHER("Other"),
}

data class Expense(
    val id: Long,
    val merchant: String,
    val totalMinor: Long,
    val currency: String,
    val date: LocalDate,
    val category: Category,
    val note: String = "",
    // Later: a local file path or a remote URL for the receipt photo.
    val receiptPhoto: String? = null,
)

fun Expense.formattedTotal(): String = "%.2f %s".format(totalMinor / 100.0, currency)
