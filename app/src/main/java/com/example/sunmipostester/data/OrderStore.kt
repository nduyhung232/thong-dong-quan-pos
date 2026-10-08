package com.example.sunmipostester.data

/**
 * In-memory holder for the cart being built right now. Only the *in-progress*
 * order lives here; once paid it is persisted via [PosRepository.savePaidOrder].
 *
 * Not thread-safe; intended to be used from the UI thread.
 */
object OrderStore {

    private val lines = mutableListOf<CartLine>()

    /** Current cart lines (read-only copy). */
    fun currentLines(): List<CartLine> = lines.toList()

    /** Cart total in VND. */
    fun total(): Int = lines.sumOf { it.lineTotal }

    fun itemCount(): Int = lines.sumOf { it.quantity }

    fun isEmpty(): Boolean = lines.isEmpty()

    /** Add one unit, merging into an existing line for the same product. */
    fun add(product: ProductEntity) {
        val existing = lines.firstOrNull { it.product.id == product.id }
        if (existing != null) existing.quantity++
        else lines.add(CartLine(product, 1))
    }

    /** Decrease one unit; removes the line when it reaches zero. */
    fun decrease(productId: Long) {
        val idx = lines.indexOfFirst { it.product.id == productId }
        if (idx < 0) return
        val line = lines[idx]
        if (line.quantity <= 1) lines.removeAt(idx)
        else line.quantity--
    }

    /** Remove a line entirely. */
    fun remove(productId: Long) {
        lines.removeAll { it.product.id == productId }
    }

    /** Clear the cart (after payment or cancel). */
    fun clear() {
        lines.clear()
    }

    /** Replace the current cart with a persisted table draft. */
    fun replaceWith(linesToLoad: List<CartLine>) {
        lines.clear()
        lines.addAll(linesToLoad.map { CartLine(it.product, it.quantity) })
    }
}
