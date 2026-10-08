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

    /** Add one unit, merging into an existing line for the same product without toppings. */
    fun add(product: ProductEntity) {
        val existing = lines.firstOrNull { it.product.id == product.id && it.selectedToppings.isEmpty() }
        if (existing != null) existing.quantity++
        else lines.add(CartLine(product = product, quantity = 1))
    }

    /** Increase one unit by lineId. */
    fun increaseLine(lineId: String) {
        val line = lines.firstOrNull { it.lineId == lineId } ?: return
        line.quantity++
    }

    /** Decrease one unit by lineId; removes the line when it reaches zero. */
    fun decreaseLine(lineId: String) {
        val idx = lines.indexOfFirst { it.lineId == lineId }
        if (idx < 0) return
        val line = lines[idx]
        if (line.quantity <= 1) lines.removeAt(idx)
        else line.quantity--
    }

    /** Remove a line entirely by lineId. */
    fun removeLine(lineId: String) {
        lines.removeAll { it.lineId == lineId }
    }

    /** Update toppings for a specific line. */
    fun updateToppings(lineId: String, toppings: List<ToppingEntity>) {
        val line = lines.firstOrNull { it.lineId == lineId } ?: return
        line.selectedToppings.clear()
        line.selectedToppings.addAll(toppings)
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
        lines.addAll(linesToLoad.map {
            val copy = CartLine(lineId = it.lineId, product = it.product, quantity = it.quantity)
            copy.selectedToppings.addAll(it.selectedToppings)
            copy
        })
    }
}
