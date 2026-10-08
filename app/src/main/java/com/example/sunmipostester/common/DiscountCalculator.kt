package com.example.sunmipostester.common

/**
 * Discount maths — 1:1 port of the server's `shared/discount.ts`.
 *
 * Pure and side-effect free so it can be unit tested in isolation and so the POS
 * produces EXACTLY the figures the server re-verifies. The server recomputes a
 * PERCENT discount with its own copy of this rule and rejects the order if the
 * result differs, so any drift here becomes a rejected sale.
 *
 * Business rules (mirrored from the server side):
 *  1. The total can never go below 0 — a discount larger than the subtotal is
 *     clamped to the subtotal.
 *  2. Percent input is clamped to 0..100.
 *  3. Exactly one discount per order (enforced by the caller).
 */

/** Kind of discount applied to an order. Persisted as text via a Room converter. */
enum class DiscountType { NONE, AMOUNT, PERCENT, CODE }

/** Result of a discount computation. */
data class DiscountResult(
    /** Actual đồng removed (0..subtotal). */
    val discountAmount: Int,
    /** subtotal - discountAmount, never below 0. */
    val total: Int
)

object DiscountCalculator {

    const val MAX_PERCENT = 100

    private fun clamp(value: Int, min: Int, max: Int): Int =
        minOf(maxOf(value, min), max)

    /** Percent of subtotal, percent clamped to 0..100, rounded HALF_UP. */
    private fun percentDiscount(subtotal: Int, percent: Int): Int =
        Money.percentOf(subtotal, clamp(percent, 0, MAX_PERCENT))

    /**
     * Compute a discount.
     *
     * @param subtotal      order subtotal in VND.
     * @param type          discount kind.
     * @param input         raw operator figure: VND for AMOUNT, percent for PERCENT.
     * @param codeValueType for CODE: the code's own value type (AMOUNT/PERCENT).
     * @param codeValue     for CODE: the code's value (VND or percent).
     */
    fun compute(
        subtotal: Int,
        type: DiscountType,
        input: Int = 0,
        codeValueType: DiscountType = DiscountType.NONE,
        codeValue: Int = 0
    ): DiscountResult {
        val safeSubtotal = maxOf(subtotal, 0)

        val raw: Int = when (type) {
            DiscountType.NONE -> 0
            DiscountType.AMOUNT -> maxOf(input, 0)
            DiscountType.PERCENT -> percentDiscount(safeSubtotal, input)
            DiscountType.CODE -> when (codeValueType) {
                DiscountType.AMOUNT -> maxOf(codeValue, 0)
                DiscountType.PERCENT -> percentDiscount(safeSubtotal, codeValue)
                else -> 0
            }
        }

        // Rule 1: never below zero — clamp the discount to the subtotal.
        val discountAmount = clamp(raw, 0, safeSubtotal)
        return DiscountResult(discountAmount = discountAmount, total = safeSubtotal - discountAmount)
    }

    fun isPercentValid(percent: Int): Boolean = percent in 0..MAX_PERCENT
}
