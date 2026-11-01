package com.example.receipttracker.receipt

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The receipts here are written the way real ones print: the figure the customer owes is not the
 * largest number on the page, the breakdown lines look like totals, and OCR gets some characters
 * wrong. Each test names the trap it is guarding against.
 */
class RegexReceiptTotalParserTest {

    private val parser = RegexReceiptTotalParser()

    @Test
    fun `picks TOTAL and not the cash handed over or the change given back`() {
        val receipt = """
            LIDL ROMANIA SCS
            Bd. Timisoara 26
            01.11.2026  14:32
            PAINE INTEGRALA        1 x 5,49    5,49
            LAPTE 1.5L             2 x 6,25   12,50
            SUBTOTAL                          17,99
            TVA 9%                             1,49
            TOTAL                             17,99
            NUMERAR                           20,00
            REST                               2,01
        """.trimIndent()

        val parsed = parser.parse(receipt)

        // 20,00 is the largest number on the receipt. It is the cash, not the total.
        assertEquals(1799L, parsed?.amountMinor)
        assertEquals("RON", parsed?.currency)
    }

    @Test
    fun `ignores Subtotal, Tax, Cash and Change on an english receipt`() {
        val receipt = """
            WHOLE FOODS
            Subtotal        10.00
            Tax              0.80
            Total           10.80
            Cash            20.00
            Change           9.20
        """.trimIndent()

        assertEquals(1080L, parser.parse(receipt)?.amountMinor)
    }

    @Test
    fun `reads a comma decimal separator with a dot thousands separator`() {
        val parsed = parser.parse("TOTAL DE PLATA   1.234,56 LEI")

        assertEquals(123456L, parsed?.amountMinor)
        assertEquals("RON", parsed?.currency)
    }

    @Test
    fun `reads a dot decimal separator with a comma thousands separator`() {
        val parsed = parser.parse("TOTAL  $1,234.56")

        assertEquals(123456L, parsed?.amountMinor)
        assertEquals("USD", parsed?.currency)
    }

    @Test
    fun `does not read a date as an amount`() {
        // "01.11" would otherwise be a perfectly well-formed amount.
        val receipt = """
            SHOP
            01.11.2026
            TOTAL 42,00
        """.trimIndent()

        assertEquals(4200L, parser.parse(receipt)?.amountMinor)
    }

    @Test
    fun `survives OCR reading the letter O in TOTAL as a zero`() {
        val parsed = parser.parse("T0TAL                   98,35 RON")

        assertEquals(9835L, parsed?.amountMinor)
    }

    @Test
    fun `finds the amount when OCR puts the label and the figure on separate lines`() {
        val receipt = """
            MEGA IMAGE
            TOTAL DE PLATA
            119,00 LEI
        """.trimIndent()

        val parsed = parser.parse(receipt)

        assertEquals(11900L, parsed?.amountMinor)
        // Lower than a same-line hit: we inferred the pairing from layout rather than read it.
        assertTrue(
            "expected a reduced confidence, was ${parsed?.confidence}",
            parsed!!.confidence < SAME_LINE_CONFIDENCE,
        )
    }

    @Test
    fun `falls back to the largest amount with a low confidence when nothing names a total`() {
        val receipt = """
            SOME SHOP
            item a 10,00
            item b 25,50
        """.trimIndent()

        val parsed = parser.parse(receipt)

        assertEquals(2550L, parsed?.amountMinor)
        // This is the case that would justify spending a network round trip on the cloud parser.
        assertTrue(
            "expected a low confidence, was ${parsed?.confidence}",
            parsed!!.confidence <= LOW_CONFIDENCE,
        )
    }

    @Test
    fun `lowers confidence when two lines claim to be the total and disagree`() {
        val confident = parser.parse("TOTAL 59,50")!!
        val conflicted = parser.parse(
            """
            SHOP
            TOTAL        50,00
            GRAND TOTAL  59,50
            """.trimIndent(),
        )!!

        assertEquals(5950L, conflicted.amountMinor)
        assertTrue(
            "conflicting totals should score below a single clear one",
            conflicted.confidence < confident.confidence,
        )
    }

    @Test
    fun `reports the line it read so the user can check it`() {
        val parsed = parser.parse("Cafe de Flore\nTOTAL  9,80 EUR")

        assertEquals("TOTAL  9,80 EUR", parsed?.matchedLine)
        assertEquals("EUR", parsed?.currency)
    }

    @Test
    fun `uses the default currency when the receipt names none`() {
        assertEquals("RON", parser.parse("TOTAL 42,00")?.currency)
        assertEquals("EUR", RegexReceiptTotalParser(defaultCurrency = "EUR").parse("TOTAL 42,00")?.currency)
    }

    @Test
    fun `trusts a receipt that names its currency more than one that does not`() {
        val named = parser.parse("TOTAL 42,00 RON")!!
        val unnamed = parser.parse("TOTAL 42,00")!!

        assertTrue(named.confidence > unnamed.confidence)
    }

    @Test
    fun `returns null rather than guessing when there is nothing to read`() {
        assertNull(parser.parse(""))
        assertNull(parser.parse("   \n  \n "))
        assertNull(parser.parse("JUST SOME TEXT\nTOTAL"))
    }

    @Test
    fun `never reports a confidence outside zero and one`() {
        val receipts = listOf(
            "TOTAL 42,00 RON",
            "TOTAL 50,00\nGRAND TOTAL 59,50",
            "item 10,00",
            "TOTAL DE PLATA\n119,00 LEI",
        )

        receipts.forEach { receipt ->
            val confidence = parser.parse(receipt)!!.confidence
            assertTrue("confidence $confidence out of range for: $receipt", confidence in 0f..1f)
        }
    }

    private companion object {
        const val SAME_LINE_CONFIDENCE = 0.85f
        const val LOW_CONFIDENCE = 0.4f
    }
}
