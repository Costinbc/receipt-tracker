package com.example.receipttracker.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth
import java.util.Locale

class ExpenseSummariesTest {

    private val october = YearMonth.of(2026, 10)
    private val november = YearMonth.of(2026, 11)

    @Test
    fun `sums a single currency in minor units`() {
        val summary = ExpenseSummaries.forMonth(
            listOf(
                expense(totalMinor = 18745, date = november.atDay(1)),
                expense(totalMinor = 2310, date = november.atDay(2)),
            ),
            november,
        )

        assertEquals(listOf(MoneyTotal("RON", 21055)), summary.totals)
        assertEquals(2, summary.expenseCount)
    }

    @Test
    fun `ignores expenses from other months`() {
        val summary = ExpenseSummaries.forMonth(
            listOf(
                expense(totalMinor = 10000, date = november.atDay(1)),
                expense(totalMinor = 99999, date = october.atDay(31)),
                expense(totalMinor = 88888, date = november.atEndOfMonth().plusDays(1)),
            ),
            november,
        )

        assertEquals(listOf(MoneyTotal("RON", 10000)), summary.totals)
        assertEquals(1, summary.expenseCount)
    }

    @Test
    fun `never adds two currencies together`() {
        val summary = ExpenseSummaries.forMonth(
            listOf(
                expense(totalMinor = 18745, currency = "RON", date = november.atDay(1)),
                expense(totalMinor = 980, currency = "EUR", date = november.atDay(2)),
                expense(totalMinor = 215, currency = "EUR", date = november.atDay(3)),
            ),
            november,
        )

        // 187,45 RON and 11,95 EUR. Collapsing these needs an exchange rate, which needs a network.
        assertEquals(
            listOf(MoneyTotal("RON", 18745), MoneyTotal("EUR", 1195)),
            summary.totals,
        )
    }

    @Test
    fun `orders totals by size then alphabetically so the output is stable`() {
        val summary = ExpenseSummaries.forMonth(
            listOf(
                expense(totalMinor = 100, currency = "USD", date = november.atDay(1)),
                expense(totalMinor = 500, currency = "EUR", date = november.atDay(1)),
                expense(totalMinor = 100, currency = "GBP", date = november.atDay(1)),
            ),
            november,
        )

        assertEquals(
            listOf(MoneyTotal("EUR", 500), MoneyTotal("GBP", 100), MoneyTotal("USD", 100)),
            summary.totals,
        )
    }

    @Test
    fun `breaks the month down per category, keeping currencies apart`() {
        val summary = ExpenseSummaries.forMonth(
            listOf(
                expense(totalMinor = 1000, category = Category.FOOD, date = november.atDay(1)),
                expense(totalMinor = 500, category = Category.FOOD, currency = "EUR", date = november.atDay(2)),
                expense(totalMinor = 2000, category = Category.TRANSPORT, date = november.atDay(3)),
            ),
            november,
        )

        assertEquals(
            listOf(
                CategoryTotal(Category.FOOD, listOf(MoneyTotal("RON", 1000), MoneyTotal("EUR", 500))),
                CategoryTotal(Category.TRANSPORT, listOf(MoneyTotal("RON", 2000))),
            ),
            summary.byCategory,
        )
    }

    @Test
    fun `leaves out categories with nothing spent in them`() {
        val summary = ExpenseSummaries.forMonth(
            listOf(expense(totalMinor = 1000, category = Category.BILLS, date = november.atDay(1))),
            november,
        )

        assertEquals(listOf(Category.BILLS), summary.byCategory.map { it.category })
    }

    @Test
    fun `an empty month is empty rather than zero RON`() {
        val summary = ExpenseSummaries.forMonth(emptyList(), november)

        assertTrue(summary.isEmpty)
        assertEquals(0, summary.expenseCount)
        assertEquals(emptyList<MoneyTotal>(), summary.totals)
        assertEquals(emptyList<CategoryTotal>(), summary.byCategory)
    }

    @Test
    fun `stays exact over many expenses, where a Double would drift`() {
        // 0,10 + 0,20 is not 0,30 in binary floating point. In minor units it always is.
        val expenses = (1..1000).map { expense(totalMinor = 10, date = november.atDay(1)) }

        assertEquals(listOf(MoneyTotal("RON", 10_000)), ExpenseSummaries.forMonth(expenses, november).totals)
    }

    @Test
    fun `formats minor units back into a readable amount`() {
        assertEquals("187.45 RON", MoneyTotal("RON", 18745).formatted(Locale.ROOT))
        assertEquals("0.05 EUR", MoneyTotal("EUR", 5).formatted(Locale.ROOT))
    }

    @Test
    fun `formats using the locale it is given, not the one the machine happens to have`() {
        val amount = MoneyTotal("RON", 18745)

        assertEquals("187.45 RON", amount.formatted(Locale.US))
        assertEquals("187,45 RON", amount.formatted(Locale.forLanguageTag("ro-RO")))
    }

    private fun expense(
        totalMinor: Long,
        currency: String = "RON",
        category: Category = Category.OTHER,
        date: LocalDate = LocalDate.of(2026, 11, 1),
    ) = Expense(
        id = 0,
        merchant = "Test",
        totalMinor = totalMinor,
        currency = currency,
        date = date,
        category = category,
    )
}
