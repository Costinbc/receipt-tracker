package com.example.receipttracker.data

import androidx.compose.runtime.mutableStateListOf
import java.time.LocalDate

/**
 * Prototype-only, in-memory store with hardcoded data.
 * Nothing here survives process death; Room and a repository replace it in Labs 4-7.
 */
object FakeExpenseStore {

    private val today = LocalDate.now()

    val expenses = mutableStateListOf(
        Expense(1, "Lidl", 187.45, "RON", today, Category.GROCERIES, "Weekly shopping"),
        Expense(2, "Bolt", 23.10, "RON", today, Category.TRANSPORT),
        Expense(3, "Starbucks", 19.50, "RON", today.minusDays(1), Category.FOOD, "Flat white"),
        Expense(4, "Mega Image", 64.20, "RON", today.minusDays(1), Category.GROCERIES),
        Expense(5, "Enel", 212.00, "RON", today.minusDays(3), Category.BILLS, "Electricity, October"),
        Expense(6, "eMAG", 349.99, "RON", today.minusDays(4), Category.SHOPPING, "Headphones"),
        Expense(7, "Metrorex", 5.00, "RON", today.minusDays(4), Category.TRANSPORT),
        Expense(8, "Shaormeria Dristor", 32.00, "RON", today.minusDays(5), Category.FOOD),
        Expense(9, "Kaufland", 142.80, "RON", today.minusDays(7), Category.GROCERIES),
        Expense(10, "Digi", 45.00, "RON", today.minusDays(9), Category.BILLS, "Internet"),
        Expense(11, "Café de Flore", 9.80, "EUR", today.minusDays(12), Category.FOOD, "Paris trip"),
        Expense(12, "RATP", 2.15, "EUR", today.minusDays(12), Category.TRANSPORT, "Paris trip"),
        Expense(13, "Decathlon", 119.99, "RON", today.minusDays(15), Category.SHOPPING),
        Expense(14, "Carrefour", 98.35, "RON", today.minusDays(18), Category.GROCERIES),
        Expense(15, "Netflix", 59.99, "RON", today.minusDays(20), Category.OTHER, "Subscription"),
    )

    fun find(id: Long): Expense? = expenses.firstOrNull { it.id == id }

    fun add(expense: Expense) {
        val nextId = (expenses.maxOfOrNull { it.id } ?: 0) + 1
        expenses.add(0, expense.copy(id = nextId))
    }
}
