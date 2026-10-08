package com.example.sunmipostester.data

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.Normalizer

/**
 * Formatting helpers shared across the app.
 */
object TextFormat {

    private val vndFormat = DecimalFormat("#,###", DecimalFormatSymbols().apply {
        groupingSeparator = '.'
    })

    /** Format an integer VND amount, e.g. 45000 -> "45.000 đ". */
    fun vnd(amount: Int): String = "${vndFormat.format(amount.toLong())} đ"

    /** Format without the currency suffix, e.g. 45000 -> "45.000". */
    fun money(amount: Int): String = vndFormat.format(amount.toLong())

    private val combiningMarks = Regex("\\p{Mn}+")

    /**
     * Remove Vietnamese diacritics so text prints correctly on ESC/POS printers
     * that don't have a Vietnamese code page loaded.
     * e.g. "Cà phê sữa" -> "Ca phe sua".
     */
    fun deaccent(input: String): String {
        val normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
        return combiningMarks.replace(normalized, "")
            .replace('đ', 'd').replace('Đ', 'D')
    }
}
