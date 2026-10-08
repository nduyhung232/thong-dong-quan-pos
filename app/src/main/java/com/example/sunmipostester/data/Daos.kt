package com.example.sunmipostester.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update

@Dao
interface ProductDao {

    @Query("SELECT * FROM products WHERE active = 1 ORDER BY category, name")
    suspend fun activeProducts(): List<ProductEntity>

    @Query("SELECT * FROM products ORDER BY active DESC, category, name")
    suspend fun allProducts(): List<ProductEntity>

    @Query("SELECT DISTINCT category FROM products WHERE active = 1 ORDER BY category")
    suspend fun activeCategories(): List<String>

    @Query("SELECT COUNT(*) FROM products")
    suspend fun count(): Int

    /** Lookup by the server's cross-device key, for sync upserts. */
    @Query("SELECT * FROM products WHERE syncId = :syncId")
    suspend fun bySyncId(syncId: String): ProductEntity?

    @Insert
    suspend fun insert(product: ProductEntity): Long

    @Insert
    suspend fun insertAll(products: List<ProductEntity>)

    @Update
    suspend fun update(product: ProductEntity)

    /**
     * Soft-delete: keeps the row so historical orders remain explainable while
     * removing the item from the sale menu.
     */
    @Query("UPDATE products SET active = 0 WHERE id = :id")
    suspend fun deactivate(id: Long)

    @Query("UPDATE products SET active = 1 WHERE id = :id")
    suspend fun reactivate(id: Long)
}

@Dao
interface OrderDao {

    @Insert
    suspend fun insertOrder(order: OrderEntity): Long

    @Insert
    suspend fun insertItems(items: List<OrderItemEntity>)

    /**
     * Persist an order header and its items atomically. Returns the new order id.
     * Running both inserts in one transaction prevents orphan headers if the app
     * dies midway.
     */
    @Transaction
    suspend fun saveOrder(order: OrderEntity, items: List<OrderItemEntity>): Long {
        val orderId = insertOrder(order)
        insertItems(items.map { it.copy(orderId = orderId) })
        return orderId
    }

    @Query("SELECT * FROM orders ORDER BY createdAtMs DESC LIMIT :limit")
    suspend fun recentOrders(limit: Int = 200): List<OrderEntity>

    @Query("SELECT * FROM orders WHERE createdAtMs BETWEEN :fromMs AND :toMs ORDER BY createdAtMs DESC LIMIT :limit OFFSET :offset")
    suspend fun ordersBetween(fromMs: Long, toMs: Long, limit: Int, offset: Int): List<OrderEntity>

    /** Orders not yet durably accepted by the server (new or re-pushed cancels). */
    @Query("SELECT * FROM orders WHERE syncedAtMs IS NULL ORDER BY createdAtMs")
    suspend fun unsyncedOrders(): List<OrderEntity>

    /** Mark an order as accepted by the server. */
    @Query("UPDATE orders SET syncedAtMs = :atMs WHERE syncId = :syncId")
    suspend fun markSynced(syncId: String, atMs: Long)

    @Query("SELECT * FROM orders WHERE id = :orderId")
    suspend fun orderById(orderId: Long): OrderEntity?

    @Query("SELECT * FROM order_items WHERE orderId = :orderId ORDER BY id")
    suspend fun itemsOf(orderId: Long): List<OrderItemEntity>

    @Transaction
    suspend fun orderWithItems(orderId: Long): OrderWithItems? {
        val order = orderById(orderId) ?: return null
        return OrderWithItems(order, itemsOf(orderId))
    }

    /**
     * Void an order, recording who did it and when. Status change only — the row
     * is never deleted, so the audit trail survives.
     */
    @Query(
        """
        UPDATE orders
        SET status = 'CANCELLED', cancelledByStaffId = :staffId,
            cancelledByStaffSyncId = :staffSyncId, cancelledAtMs = :atMs, syncedAtMs = NULL
        WHERE id = :orderId
        """
    )
    suspend fun cancelOrder(orderId: Long, staffId: Long?, staffSyncId: String?, atMs: Long)

    /** Revenue of PAID orders created within the given time window. */
    @Query(
        """
        SELECT COALESCE(SUM(total), 0) FROM orders
        WHERE status = 'PAID' AND createdAtMs BETWEEN :fromMs AND :toMs
        """
    )
    suspend fun revenueBetween(fromMs: Long, toMs: Long): Int

    @Query(
        """
        SELECT COUNT(*) FROM orders
        WHERE status = 'PAID' AND createdAtMs BETWEEN :fromMs AND :toMs
        """
    )
    suspend fun paidCountBetween(fromMs: Long, toMs: Long): Int
}

@Dao
interface CashExpenseDao {
    @Insert
    suspend fun insert(expense: CashExpenseEntity): Long

    @Query("SELECT COALESCE(SUM(amount), 0) FROM cash_expenses WHERE shiftId = :shiftId")
    suspend fun totalForShift(shiftId: Long): Int

    @Query("SELECT * FROM cash_expenses WHERE shiftId = :shiftId ORDER BY createdAtMs DESC")
    suspend fun allForShift(shiftId: Long): List<CashExpenseEntity>

    @Query("SELECT * FROM cash_expenses ORDER BY createdAtMs DESC")
    suspend fun all(): List<CashExpenseEntity>
}

@Dao
interface TableDraftDao {

    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun putDraft(draft: TableDraftEntity)

    @Query("DELETE FROM table_draft_items WHERE tableName = :tableName")
    suspend fun clearItems(tableName: String)

    @Insert
    suspend fun insertItems(items: List<TableDraftItemEntity>)

    @Transaction
    suspend fun saveDraft(draft: TableDraftEntity, items: List<TableDraftItemEntity>) {
        putDraft(draft)
        clearItems(draft.tableName)
        if (items.isNotEmpty()) insertItems(items)
    }

    @Query("SELECT * FROM table_drafts ORDER BY tableName")
    suspend fun drafts(): List<TableDraftEntity>

    @Query("SELECT * FROM table_draft_items WHERE tableName = :tableName ORDER BY id")
    suspend fun items(tableName: String): List<TableDraftItemEntity>

    @Query("DELETE FROM table_drafts WHERE tableName = :tableName")
    suspend fun deleteDraft(tableName: String)
}

@Dao
interface ShiftDao {

    @Insert
    suspend fun insert(shift: ShiftEntity): Long

    @Update
    suspend fun update(shift: ShiftEntity)

    /** The currently open shift, or null if none. At most one is expected. */
    @Query("SELECT * FROM shifts WHERE status = 'OPEN' ORDER BY openedAtMs DESC LIMIT 1")
    suspend fun openShift(): ShiftEntity?

    @Query("SELECT * FROM shifts ORDER BY openedAtMs DESC LIMIT :limit")
    suspend fun recentShifts(limit: Int = 100): List<ShiftEntity>

    /** Shifts not yet durably accepted by the server (opened, or re-pushed close). */
    @Query("SELECT * FROM shifts WHERE syncedAtMs IS NULL ORDER BY openedAtMs")
    suspend fun unsyncedShifts(): List<ShiftEntity>

    @Query("UPDATE shifts SET syncedAtMs = :atMs WHERE syncId = :syncId")
    suspend fun markSynced(syncId: String, atMs: Long)

    @Query("SELECT * FROM shifts WHERE id = :shiftId")
    suspend fun shiftById(shiftId: Long): ShiftEntity?

    /** Cash collected (order totals, not tendered amounts) for PAID cash orders. */
    @Query(
        """
        SELECT COALESCE(SUM(total), 0) FROM orders
        WHERE shiftId = :shiftId AND status = 'PAID' AND paymentMethod = 'CASH'
        """
    )
    suspend fun cashSalesOfShift(shiftId: Long): Int

    /** All PAID revenue in the shift, regardless of payment method. */
    @Query(
        """
        SELECT COALESCE(SUM(total), 0) FROM orders
        WHERE shiftId = :shiftId AND status = 'PAID'
        """
    )
    suspend fun totalSalesOfShift(shiftId: Long): Int

    @Query("SELECT COUNT(*) FROM orders WHERE shiftId = :shiftId AND status = 'PAID'")
    suspend fun paidCountOfShift(shiftId: Long): Int

    /** Non-cash bank transfer revenue for PAID orders in the shift. */
    @Query(
        """
        SELECT COALESCE(SUM(total), 0) FROM orders
        WHERE shiftId = :shiftId AND status = 'PAID' AND paymentMethod = 'TRANSFER'
        """
    )
    suspend fun transferSalesOfShift(shiftId: Long): Int

    /** Card revenue for PAID orders in the shift. */
    @Query(
        """
        SELECT COALESCE(SUM(total), 0) FROM orders
        WHERE shiftId = :shiftId AND status = 'PAID' AND paymentMethod = 'CARD'
        """
    )
    suspend fun cardSalesOfShift(shiftId: Long): Int
}

/** Aggregated revenue split by payment method, for the report screen. */
data class MethodRevenue(
    val paymentMethod: PaymentMethod,
    val orderCount: Int,
    val revenue: Int
)

/** Best-selling product aggregation, for the report screen. */
data class TopProduct(
    val productName: String,
    val quantity: Int,
    val revenue: Int
)

@Dao
interface ReportDao {

    @Query(
        """
        SELECT paymentMethod, COUNT(*) AS orderCount, COALESCE(SUM(total), 0) AS revenue
        FROM orders
        WHERE status = 'PAID' AND createdAtMs BETWEEN :fromMs AND :toMs
        GROUP BY paymentMethod
        """
    )
    suspend fun revenueByMethod(fromMs: Long, toMs: Long): List<MethodRevenue>

    @Query(
        """
        SELECT i.productName AS productName,
               SUM(i.quantity) AS quantity,
               SUM(i.unitPrice * i.quantity) AS revenue
        FROM order_items i
        INNER JOIN orders o ON o.id = i.orderId
        WHERE o.status = 'PAID' AND o.createdAtMs BETWEEN :fromMs AND :toMs
        GROUP BY i.productName
        ORDER BY quantity DESC
        LIMIT :limit
        """
    )
    suspend fun topProducts(fromMs: Long, toMs: Long, limit: Int = 10): List<TopProduct>

    /** Every order line — used by the export/backup snapshot. */
    @Query("SELECT * FROM order_items ORDER BY orderId, id")
    suspend fun allOrderItems(): List<OrderItemEntity>
}

@Dao
interface StaffDao {

    @Insert
    suspend fun insert(staff: StaffEntity): Long

    @Update
    suspend fun update(staff: StaffEntity)

    @Query("SELECT * FROM staff WHERE active = 1 ORDER BY role, name")
    suspend fun activeStaff(): List<StaffEntity>

    @Query("SELECT * FROM staff ORDER BY active DESC, role, name")
    suspend fun allStaff(): List<StaffEntity>

    @Query("SELECT * FROM staff WHERE id = :id")
    suspend fun byId(id: Long): StaffEntity?

    /** Lookup by the server's cross-device key, for sync upserts. */
    @Query("SELECT * FROM staff WHERE syncId = :syncId")
    suspend fun bySyncId(syncId: String): StaffEntity?

    @Query("SELECT COUNT(*) FROM staff")
    suspend fun count(): Int

    /** Number of active managers — used to block removing the last manager. */
    @Query("SELECT COUNT(*) FROM staff WHERE active = 1 AND role = 'MANAGER'")
    suspend fun activeManagerCount(): Int

    @Query("UPDATE staff SET active = 0 WHERE id = :id")
    suspend fun deactivate(id: Long)

    @Query("UPDATE staff SET active = 1 WHERE id = :id")
    suspend fun reactivate(id: Long)
}

@Dao
interface DiscountCodeDao {

    @Insert
    suspend fun insert(code: DiscountCodeEntity): Long

    @Update
    suspend fun update(code: DiscountCodeEntity)

    /** Lookup by the server's cross-device key, for pull upserts. */
    @Query("SELECT * FROM discount_codes WHERE syncId = :syncId")
    suspend fun bySyncId(syncId: String): DiscountCodeEntity?

    /** Lookup by the human-facing code string, for applying at checkout. */
    @Query("SELECT * FROM discount_codes WHERE code = :code")
    suspend fun byCode(code: String): DiscountCodeEntity?

    /** Codes still available to spend on this terminal. */
    @Query("SELECT * FROM discount_codes WHERE consumed = 0 ORDER BY campaignName, code")
    suspend fun available(): List<DiscountCodeEntity>

    /** Mark a code consumed by an order (single-use, offline-safe). */
    @Query(
        """
        UPDATE discount_codes
        SET consumed = 1, consumedByOrderSyncId = :orderSyncId, consumedAtMs = :atMs
        WHERE syncId = :syncId
        """
    )
    suspend fun markConsumed(syncId: String, orderSyncId: String, atMs: Long)

    /**
     * Remove codes the server no longer sends AND that we have not spent locally.
     * Consumed-but-not-yet-reported codes are kept so their spend still pushes up.
     */
    @Query("DELETE FROM discount_codes WHERE consumed = 0 AND syncId NOT IN (:keepSyncIds)")
    suspend fun deleteUnconsumedNotIn(keepSyncIds: List<String>)

    /** Used when the server assigns no codes to this device at all. */
    @Query("DELETE FROM discount_codes WHERE consumed = 0")
    suspend fun deleteAllUnconsumed()
}

@Dao
interface ToppingDao {

    @Query("SELECT * FROM toppings WHERE active = 1 ORDER BY name ASC")
    suspend fun getAllActive(): List<ToppingEntity>

    @Query("SELECT * FROM toppings WHERE syncId = :syncId LIMIT 1")
    suspend fun getBySyncId(syncId: String): ToppingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(topping: ToppingEntity): Long

    @Query(
        """
        SELECT t.* FROM toppings t
        INNER JOIN product_toppings pt ON t.syncId = pt.toppingSyncId
        WHERE pt.productSyncId = :productSyncId AND t.active = 1
        ORDER BY t.name ASC
        """
    )
    suspend fun getToppingsForProduct(productSyncId: String): List<ToppingEntity>

    @Query("DELETE FROM product_toppings WHERE productSyncId = :productSyncId")
    suspend fun clearProductToppings(productSyncId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProductToppings(links: List<ProductToppingEntity>)
}
