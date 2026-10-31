package com.example.receipttracker.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.receipttracker.data.FakeExpenseStore
import kotlinx.serialization.Serializable

@Serializable
object ExpenseListRoute

@Serializable
data class ExpenseDetailRoute(val id: Long)

@Serializable
object AddExpenseRoute

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = ExpenseListRoute) {
        composable<ExpenseListRoute> {
            ExpenseListScreen(
                expenses = FakeExpenseStore.expenses,
                onExpenseClick = { id -> navController.navigate(ExpenseDetailRoute(id)) },
                onAddClick = { navController.navigate(AddExpenseRoute) },
            )
        }
        composable<ExpenseDetailRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<ExpenseDetailRoute>()
            ExpenseDetailScreen(
                expense = FakeExpenseStore.find(route.id),
                onBack = { navController.popBackStack() },
            )
        }
        composable<AddExpenseRoute> {
            AddExpenseScreen(
                onSave = { expense ->
                    FakeExpenseStore.add(expense)
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() },
            )
        }
    }
}
