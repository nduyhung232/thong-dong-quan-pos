package com.example.sunmipostester.common

/**
 * Money — SINGLE SOURCE OF TRUTH for VND arithmetic on the POS (Android) side.
 *
 * This is a deliberate 1:1 port of the server's `shared/money.ts`. Both
 * implementations MUST agree, because the server re-derives every pushed order's
 * figures and REJECTS the order if a single đồng disagrees. They are pinned by
 * the shared vector file `money-vectors.json` (server: test/money.test.mjs;
 * Android: MoneyVectorsTest).
 *
 * ## Storage
 * Every monetary amount is an integer number of VND (ISO 4217 exponent 0).
 * `+`, `-`, `*` on integers are exact. Rounding is only ever needed for DIVISION.
 *
 * ## Rounding rule (mirrors money.ts / Java RoundingMode.HALF_UP)
 * Round to the nearest whole VND, HALF_UP:
 *   fractional part  < 0.5  -> toward zero
 *   fractional part >= 0.5  -> away from zero
 * Negative amounts round by magnitude:  -7 / 2 = -3.5 -> -4
 *
 * ## Rules for callers
 *  - NEVER use `/` or `%` directly on a money value. Use these functions so every
 *    rounding decision stays in one auditable place.
 *  - Never use a floating-point value as a money amount.
 *
 * NOTE: VAT extraction is intentionally NOT ported — the statutory rounding rule
 * for Vietnamese VAT invoices is a legal question, not a coding one.
 */
object Money {

    /**
     * Exact integer HALF_UP division of numerator/denominator.
     * Uses [Long] intermediates so `amount * percent` cannot overflow before the
     * division (an Int would overflow around a 21M subtotal at 100%).
     * Behaviour matches BigDecimal.divide(..., 0, RoundingMode.HALF_UP).
     */
    private fun halfUp(numerator: Long, denominator: Long): Long {
        require(denominator != 0L) { "Money: denominator must not be zero" }
        val sign = java.lang.Long.signum(numerator) * java.lang.Long.signum(denominator)
        val n = Math.abs(numerator)
        val d = Math.abs(denominator)

        val q = n / d
        val remainder = n - q * d
        // remainder/d >= 0.5  <=>  2*remainder >= d   (integer-only comparison)
        val rounded = if (2 * remainder >= d) q + 1 else q

        // Normalise negative zero away so both sides agree on divide(-1, 3) == 0.
        return if (rounded == 0L) 0L else sign * rounded
    }

    /**
     * Divide a VND amount and round to whole đồng.
     * Throws on a zero divisor — that is a programming error, not a value to absorb.
     */
    fun divide(amount: Int, divisor: Int): Int {
        require(divisor != 0) { "Money.divide: divisor must not be zero" }
        return halfUp(amount.toLong(), divisor.toLong()).toInt()
    }

    /**
     * Percentage of an amount, rounded to whole đồng. Used for discounts.
     *   percentOf(45_000, 15) = 6_750
     *   percentOf(33_000,  8) = 2_640
     */
    fun percentOf(amount: Int, percent: Int): Int {
        return halfUp(amount.toLong() * percent.toLong(), 100L).toInt()
    }

    /**
     * Split an amount into [parts] shares that sum EXACTLY back to [amount].
     * The rounding remainder is spread one đồng at a time over the first shares.
     *   split(100_000, 3) = [33_334, 33_333, 33_333]
     *   split(10, 4)      = [3, 3, 2, 2]
     *
     * Negative amounts are REJECTED to stay aligned with money.ts (a negative
     * total does not preserve the sum under Int truncation — see money-vectors
     * knownDivergence). Failing loudly beats silently disagreeing with the server.
     */
    fun split(amount: Int, parts: Int): IntArray {
        require(parts > 0) { "Money.split: parts must be positive" }
        require(amount >= 0) { "Money.split: amount must not be negative" }

        val base = amount / parts
        var remainder = amount - base * parts
        val shares = IntArray(parts)
        for (i in 0 until parts) {
            if (remainder > 0) {
                remainder -= 1
                shares[i] = base + 1
            } else {
                shares[i] = base
            }
        }
        return shares
    }

    /** Average of a total over a count, 0 when count is 0. */
    fun average(total: Int, count: Int): Int = if (count == 0) 0 else divide(total, count)
}
