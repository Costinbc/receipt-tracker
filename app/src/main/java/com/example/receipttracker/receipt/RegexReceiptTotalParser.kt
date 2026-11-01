package com.example.receipttracker.receipt

/**
 * Picks the total out of receipt text with keyword and layout heuristics.
 *
 * Receipts are not structured documents: the largest number on the page is often the cash the
 * customer handed over, and the number next to "TOTAL" is sometimes on the following line because
 * OCR broke the two columns apart. So the rules here are deliberately explicit rather than clever:
 *
 *  1. Find lines that name a total ("TOTAL", "TOTAL DE PLATA", "AMOUNT DUE", ...) and are not
 *     one of the lines that merely look like one ("SUBTOTAL", "TVA", "CHANGE", "NUMERAR", ...).
 *  2. Prefer the last such line. Receipts print the grand total after the breakdown.
 *  3. Take the last amount on that line, because the label is on the left and the figure is on
 *     the right. If the line has no amount, look at the next line and trust it less.
 *  4. If nothing names a total, fall back to the largest amount on the receipt and say so with a
 *     low confidence, which is the signal the cloud path would act on.
 *
 * @param defaultCurrency used when the receipt text names no currency at all.
 */
class RegexReceiptTotalParser(
    private val defaultCurrency: String = "RON",
) : ReceiptTotalParser {

    override fun parse(ocrText: String): ParsedTotal? {
        val lines = ocrText.lines().map { it.trim() }.filter { it.isNotEmpty() }
        if (lines.isEmpty()) return null

        val keywordHits = keywordCandidates(lines)
        val candidate = keywordHits.lastOrNull() ?: largestAmountCandidate(lines) ?: return null

        val (currency, currencyWasExplicit) = detectCurrency(candidate.line, ocrText)

        var confidence = candidate.baseConfidence
        if (currencyWasExplicit) confidence += CURRENCY_BONUS
        if (keywordHits.distinctBy { it.amountMinor }.size > 1) confidence -= DISAGREEMENT_PENALTY

        return ParsedTotal(
            amountMinor = candidate.amountMinor,
            currency = currency,
            matchedLine = candidate.line,
            confidence = confidence.coerceIn(0f, 1f),
        )
    }

    private fun keywordCandidates(lines: List<String>): List<Candidate> {
        val candidates = mutableListOf<Candidate>()
        lines.forEachIndexed { index, line ->
            val upper = line.uppercase()
            if (!TOTAL_KEYWORD.containsMatchIn(upper)) return@forEachIndexed
            if (NOT_A_TOTAL_KEYWORD.containsMatchIn(upper)) return@forEachIndexed

            val onThisLine = lastAmountIn(line)
            if (onThisLine != null) {
                candidates += Candidate(onThisLine, line, SAME_LINE_CONFIDENCE)
                return@forEachIndexed
            }
            // OCR split the label and the figure into two lines.
            val next = lines.getOrNull(index + 1) ?: return@forEachIndexed
            val onNextLine = lastAmountIn(next) ?: return@forEachIndexed
            candidates += Candidate(onNextLine, "$line $next", NEXT_LINE_CONFIDENCE)
        }
        return candidates
    }

    private fun largestAmountCandidate(lines: List<String>): Candidate? =
        lines.mapNotNull { line -> lastAmountIn(line)?.let { Candidate(it, line, FALLBACK_CONFIDENCE) } }
            .maxByOrNull { it.amountMinor }

    private fun lastAmountIn(line: String): Long? =
        AMOUNT.findAll(line).mapNotNull { toMinorUnits(it.value) }.lastOrNull()

    /**
     * Returns the detected currency and whether the receipt actually said so, rather than us
     * falling back to [defaultCurrency]. The caller uses that to adjust confidence.
     */
    private fun detectCurrency(line: String, wholeText: String): Pair<String, Boolean> {
        matchCurrency(line)?.let { return it to true }
        matchCurrency(wholeText)?.let { return it to true }
        return defaultCurrency to false
    }

    private fun matchCurrency(text: String): String? {
        val upper = text.uppercase()
        return CURRENCY_PATTERNS.firstOrNull { (pattern, _) -> pattern.containsMatchIn(upper) }?.second
    }

    private data class Candidate(
        val amountMinor: Long,
        val line: String,
        val baseConfidence: Float,
    )

    companion object {
        private const val SAME_LINE_CONFIDENCE = 0.85f
        private const val NEXT_LINE_CONFIDENCE = 0.65f
        private const val FALLBACK_CONFIDENCE = 0.30f
        private const val CURRENCY_BONUS = 0.05f
        private const val DISAGREEMENT_PENALTY = 0.20f

        /** Guards against an OCR smear of digits overflowing the multiplication below. */
        private const val MAX_WHOLE_DIGITS = 12

        // "0" for "O" and "7" for "T" are the substitutions OCR makes most often on receipt fonts.
        private val TOTAL_KEYWORD = Regex(
            """\bT[O0][T7]AL\b|\bDE\s*PLATA\b|\bSUMA\b|\bAMOUNT\s+DUE\b|\bBALANCE\s+DUE\b"""
        )

        // Lines that contain a total-looking word but are never the amount the customer owes.
        private val NOT_A_TOTAL_KEYWORD = Regex(
            """\bSUB\s*T[O0][T7]AL\b|\bTVA\b|\bVAT\b|\bTAX\b|\bREST\b|\bCHANGE\b|""" +
                """\bCASH\b|\bNUMERAR\b|\bCARD\b|\bTIP\b|\bDISCOUNT\b|\bREDUCERE\b|\bAVANS\b"""
        )

        /**
         * An amount with exactly two decimals, with or without thousands separators.
         * Matches 19,50 · 19.50 · 1.234,56 · 1,234.56 · 1 234,56.
         * The trailing lookaheads reject dates such as 01.11.2026, where "01.11" would otherwise
         * look like a perfectly good amount.
         */
        private val AMOUNT =
            Regex("""(?<!\d)(?:\d{1,3}(?:[.,\s]\d{3})+|\d+)[.,]\d{2}(?!\d)(?![.,]\d)""")

        // Order matters: LEI must be read as RON, and the codes are checked before the symbols.
        private val CURRENCY_PATTERNS = listOf(
            Regex("""\bRON\b""") to "RON",
            Regex("""\bLEI\b""") to "RON",
            Regex("""\bEUR\b""") to "EUR",
            Regex("""\bUSD\b""") to "USD",
            Regex("""\bGBP\b""") to "GBP",
            Regex("€") to "EUR",
            Regex("\\$") to "USD",
            Regex("£") to "GBP",
        )

        /**
         * Converts a matched amount to minor units, working out whether "." and "," are decimal
         * or thousands separators from their position rather than from a locale.
         */
        private fun toMinorUnits(raw: String): Long? {
            val cleaned = raw.filterNot { it.isWhitespace() }
            val lastSeparator = cleaned.lastIndexOfAny(charArrayOf('.', ','))
            if (lastSeparator == -1) return null

            val whole = cleaned.substring(0, lastSeparator).filter { it.isDigit() }
            val fraction = cleaned.substring(lastSeparator + 1)
            if (whole.isEmpty() || whole.length > MAX_WHOLE_DIGITS) return null

            val wholeValue = whole.toLongOrNull() ?: return null
            val fractionValue = fraction.toLongOrNull() ?: return null
            return wholeValue * 100 + fractionValue
        }
    }
}
