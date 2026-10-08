package com.example.sunmipostester.data

/** Payment method for an order. Persisted in Room via [Converters]. */
enum class PaymentMethod(val label: String) {
    CASH("Tiền mặt"),
    TRANSFER("Chuyển khoản"),
    CARD("Thẻ");
}

/**
 * A line in the *in-progress* cart (not yet persisted).
 * Holds a live [ProductEntity]; on payment it is converted to an
 * [OrderItemEntity] with a price snapshot.
 */
data class CartLine(
    val lineId: String = java.util.UUID.randomUUID().toString(),
    val product: ProductEntity,
    var quantity: Int,
    val selectedToppings: MutableList<ToppingEntity> = mutableListOf()
) {
    val unitPriceWithToppings: Int get() = product.price + selectedToppings.sumOf { it.price }
    val lineTotal: Int get() = unitPriceWithToppings * quantity
    val toppingsDisplay: String get() = selectedToppings.joinToString(", ") { it.name }
}

/**
 * An order plus its line items, as read back from the database.
 * Used by the order history screen and for reprinting receipts.
 */
data class OrderWithItems(
    val order: OrderEntity,
    val items: List<OrderItemEntity>
)

/** Persisted in-progress table order with price/name snapshots. */
data class TableOrderDraft(
    val tableName: String,
    val updatedAtMs: Long,
    val lines: List<CartLine>
) {
    val total: Int get() = lines.sumOf { it.lineTotal }
}

/**
 * Computed figures for a shift.
 *
 * [expectedCash] = openingCash + cashSales — what should physically be in the
 * drawer. Compare with the counted amount to find a variance.
 */
data class ShiftSummary(
    val shift: ShiftEntity,
    val cashSales: Int,
    val totalSales: Int,
    val orderCount: Int,
    val cashExpenses: Int,
    val expectedCash: Int
) {
    /** Positive = drawer over, negative = drawer short, null = shift still open. */
    val difference: Int? get() = shift.countedCash?.minus(expectedCash)

    val isOpen: Boolean get() = shift.status == ShiftStatus.OPEN
}

/** Revenue report over a time window. */
data class RevenueReport(
    val fromMs: Long,
    val toMs: Long,
    val totalRevenue: Int,
    val totalOrders: Int,
    val byMethod: List<MethodRevenue>,
    val topProducts: List<TopProduct>
) {
    /** Average order value; 0 when there are no orders. */
    val averageOrderValue: Int
        get() = if (totalOrders == 0) 0 else totalRevenue / totalOrders
}

/**
 * Staff record as it appears in an export.
 *
 * SECURITY: intentionally omits pinHash / pinSalt. Exported files leave the
 * device, so credential material must never be included.
 */
data class StaffExport(
    val id: Long,
    val name: String,
    val role: StaffRole,
    val active: Boolean
)

/** Full business-data snapshot for backup / export. Contains no credentials. */
data class ExportSnapshot(
    val generatedAtMs: Long,
    val products: List<ProductEntity>,
    val orders: List<OrderEntity>,
    val items: List<OrderItemEntity>,
    val expenses: List<CashExpenseEntity>,
    val shifts: List<ShiftEntity>,
    val staff: List<StaffExport>
)
