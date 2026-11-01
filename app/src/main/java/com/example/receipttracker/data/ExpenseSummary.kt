package com.example.receipttracker.data

import java.time.YearMonth
import java.util.Locale

/** An amount in one currency. Minor units, so no floating point ever touches money. */
data class MoneyTotal(val currency: String, val amountMinor: Long) {
    /**
     * The locale is an explicit parameter rather than an implicit default inside the format call.
     * A Romanian device renders "187,45 RON" and a US one "187.45 RON", both correct for their
     * user, but a hidden Locale.getDefault() also makes the output of a test depend on the machine
     * it runs on: it would pass on CI and fail on a Romanian laptop. Callers that want a stable
     * string (tests, logs, anything we later send over the wire) pass Locale.ROOT.
     */
    fun formatted(locale: Locale = Locale.getDefault()): String =
        String.format(locale, "%.2f %s", amountMinor / 100.0, currency)
}

/** What was spent in one category, kept split per currency. */
data class CategoryTotal(val category: Category, val totals: List<MoneyTotal>)

/**
 * What was spent in one month.
 *
 * [totals] holds one entry per currency and is never collapsed into a single number. Converting
 * EUR into RON needs an exchange rate, a rate is a network call, and a network call is exactly the
 * thing this app must work without. So the summary refuses to guess: offline it reports
 * "187,45 RON and 9,80 EUR", which is true, rather than one total that is quietly wrong.
 * Conversion becomes a separate, clearly-dated concern once we cache rates.
 */
data class ExpenseSummary(
    val month: YearMonth,
    val expenseCount: Int,
    val totals: List<MoneyTotal>,
    val byCategory: List<CategoryTotal>,
) {
    val isEmpty: Boolean get() = expenseCount == 0
}

object ExpenseSummaries {

    /** Summarises the expenses that fall inside [month]; everything else is ignored. */
    fun forMonth(expenses: List<Expense>, month: YearMonth): ExpenseSummary {
        val inMonth = expenses.filter { YearMonth.from(it.date) == month }

        return ExpenseSummary(
            month = month,
            expenseCount = inMonth.size,
            totals = totalsPerCurrency(inMonth),
            // Categories keep their declaration order. Ranking them by size would mean comparing
            // amounts across currencies, which is the guess this type exists to avoid.
            byCategory = inMonth.groupBy { it.category }
                .toSortedMap(compareBy { it.ordinal })
                .map { (category, ofCategory) -> CategoryTotal(category, totalsPerCurrency(ofCategory)) },
        )
    }

    private fun totalsPerCurrency(expenses: List<Expense>): List<MoneyTotal> =
        expenses.groupBy { it.currency }
            .map { (currency, ofCurrency) -> MoneyTotal(currency, ofCurrency.sumOf { it.totalMinor }) }
            // Biggest first, then alphabetically, so the order is stable for tests and the UI.
            .sortedWith(compareByDescending<MoneyTotal> { it.amountMinor }.thenBy { it.currency })
}
