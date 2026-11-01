package com.example.receipttracker.receipt

/**
 * The amount a parser believes is the total of a receipt.
 *
 * [confidence] is what makes the placement decision measurable later: the on-device path is the
 * default, and a low confidence is the signal that would justify paying for the cloud path.
 * [matchedLine] is kept so the UI can show the user what we read and let them correct it.
 */
data class ParsedTotal(
    val amountMinor: Long,
    val currency: String,
    val matchedLine: String,
    val confidence: Float,
)

/**
 * Turns the raw text of a receipt into a total.
 *
 * The input is plain text, not an image, so this stays a pure JVM unit and does not care whether
 * the text came from on-device OCR or from a cloud service. That is the seam the
 * device-versus-cloud comparison will sit behind.
 */
interface ReceiptTotalParser {
    fun parse(ocrText: String): ParsedTotal?
}
