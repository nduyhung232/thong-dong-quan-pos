package com.example.sunmipostester.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.sunmipostester.common.DiscountType
import java.util.UUID

/**
 * SYNC MODEL (added for server sync)
 *
 * Every row that crosses the wire carries a `syncId` (UUID), UNIQUE, which is the
 * cross-device key the server uses. The local integer `id` stays as Room's key;
 * the server never sees it. This makes every sync operation idempotent.
 *
 * Ownership is one-directional (removes conflicts), matching the server:
 *   - Product / Staff / DiscountCode : SERVER owns, POS pulls & upserts by syncId
 *   - Order / OrderItem / Shift      : POS owns, POS pushes; `syncedAtMs` marks
 *                                      a row the server has durably accepted.
 */

/**
 * A menu product (drink / snack). Prices are integer VND to avoid rounding issues.
 * Server-owned: pulled from the web admin and upserted by [syncId].
 */
@Entity(
    tableName = "products",
    indices = [Index(value = ["syncId"], unique = true)]
)
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** Cross-device key from the server (or generated locally for the seed menu). */
    val syncId: String = UUID.randomUUID().toString(),
    val name: String,
    val price: Int,
    val category: String,
    val active: Boolean = true,
    /** Server's last-updated timestamp; used only for display/debugging. */
    val updatedAtMs: Long = System.currentTimeMillis()
)

/** Order lifecycle state. */
enum class OrderStatus { PAID, CANCELLED }

/** Shift lifecycle state. */
enum class ShiftStatus { OPEN, CLOSED }

/** Dine-in vs take-away. Required by the server sync contract. */
enum class OrderType { DINE_IN, TAKE_AWAY }

/** Staff role. Drives access control — see auth/Permission. */
enum class StaffRole { CASHIER, MANAGER }

/**
 * A staff member who can sign in with a PIN.
 * Server-owned: the terminal pulls credential material so it can authenticate
 * offline. [pinHash]/[pinSalt] are PBKDF2-HMAC-SHA256 (never the raw PIN).
 */
@Entity(
    tableName = "staff",
    indices = [Index(value = ["syncId"], unique = true)]
)
data class StaffEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val syncId: String = UUID.randomUUID().toString(),
    val name: String,
    val role: StaffRole,
    val pinHash: String,
    val pinSalt: String,
    val active: Boolean = true,
    val createdAtMs: Long = System.currentTimeMillis(),
    val updatedAtMs: Long = System.currentTimeMillis()
)

/**
 * A cashier shift (mở ca / đóng ca). POS-owned: pushed to the server.
 *
 * expectedCash = openingCash + cash sales; cashDifference = counted - expected.
 */
@Entity(
    tableName = "shifts",
    indices = [Index("staffId"), Index(value = ["syncId"], unique = true)]
)
data class ShiftEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val syncId: String = UUID.randomUUID().toString(),
    val openedAtMs: Long,
    val closedAtMs: Long? = null,
    val openingCash: Int,
    val countedCash: Int? = null,
    val expectedCash: Int? = null,
    val cashDifference: Int? = null,
    /** Note-by-note count, e.g. "500000x2;100000x5" — audit evidence. */
    val cashBreakdown: String? = null,
    val status: ShiftStatus = ShiftStatus.OPEN,
    /** Local staff id (UI/audit). */
    val staffId: Long? = null,
    /** Name snapshot so the record stays readable if the staff row changes. */
    val staffName: String? = null,
    val closedByStaffId: Long? = null,
    /** Cross-device staff keys, resolved at sale/close time for the server. */
    val staffSyncId: String? = null,
    val closedByStaffSyncId: String? = null,
    /** Server acceptance time; null = not yet pushed. */
    val syncedAtMs: Long? = null
)

/**
 * A completed order header. POS-owned: pushed to the server.
 *
 * Totals are stored (not recomputed) so a later price change never alters a
 * historical order. The server re-verifies subtotal - discountAmount == total and
 * that items sum to subtotal, so these fields must be internally consistent.
 */
@Entity(
    tableName = "orders",
    indices = [Index("shiftId"), Index("staffId"), Index("createdAtMs"), Index(value = ["syncId"], unique = true)]
)
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val syncId: String = UUID.randomUUID().toString(),
    /** Sum of line items before discount. */
    val subtotal: Int,
    /** đồng removed by the discount (0 when none). */
    val discountAmount: Int = 0,
    val total: Int,
    val discountType: DiscountType = DiscountType.NONE,
    /** Raw operator figure: VND for AMOUNT, percent for PERCENT, 0 otherwise. */
    val discountInput: Int = 0,
    /** The code string when discountType == CODE. */
    val discountCode: String? = null,
    val orderType: OrderType = OrderType.DINE_IN,
    val paymentMethod: PaymentMethod,
    val cashReceived: Int,
    val changeAmount: Int,
    val status: OrderStatus,
    val createdAtMs: Long,
    val shiftId: Long? = null,
    /** Cross-device shift key for the server (null if no open shift). */
    val shiftSyncId: String? = null,
    val staffId: Long? = null,
    val staffName: String? = null,
    val staffSyncId: String? = null,
    val cancelledByStaffId: Long? = null,
    val cancelledAtMs: Long? = null,
    val cancelledByStaffSyncId: String? = null,
    /** Server acceptance time; null = not yet pushed. */
    val syncedAtMs: Long? = null
)

/**
 * A line item inside an order. [productName]/[unitPrice] are SNAPSHOTS at sale
 * time. [productSyncId] links to the server product when known (may be null).
 */
@Entity(
    tableName = "order_items",
    foreignKeys = [
        ForeignKey(
            entity = OrderEntity::class,
            parentColumns = ["id"],
            childColumns = ["orderId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("orderId"), Index(value = ["syncId"], unique = true)]
)
data class OrderItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val syncId: String = UUID.randomUUID().toString(),
    val orderId: Long,
    /** Local product id; 0 when the product is not resolvable locally. */
    val productId: Long,
    /** Server product key snapshot (null if unknown). */
    val productSyncId: String? = null,
    val productName: String,
    val unitPrice: Int,
    val quantity: Int
) {
    val lineTotal: Int get() = unitPrice * quantity
}

/** A locally persisted, unpaid dine-in order associated with one table. */
@Entity(tableName = "table_drafts", indices = [Index("updatedAtMs")])
data class TableDraftEntity(
    @PrimaryKey val tableName: String,
    val updatedAtMs: Long = System.currentTimeMillis()
)

/** Snapshot of an item currently ordered at a table; not sent to server until paid. */
@Entity(
    tableName = "table_draft_items",
    foreignKeys = [
        ForeignKey(
            entity = TableDraftEntity::class,
            parentColumns = ["tableName"],
            childColumns = ["tableName"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("tableName")]
)
data class TableDraftItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tableName: String,
    val productId: Long,
    val productSyncId: String,
    val productName: String,
    val category: String,
    val unitPrice: Int,
    val quantity: Int
)

/** Cash paid out of the open drawer; linked to the shift and staff for audit. */
@Entity(tableName = "cash_expenses", indices = [Index("shiftId"), Index("createdAtMs")])
data class CashExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val shiftId: Long,
    val amount: Int,
    val description: String,
    val createdAtMs: Long = System.currentTimeMillis(),
    val staffId: Long?,
    val staffName: String?
)

/**
 * A single-use discount code pulled from the server and assigned to THIS device.
 *
 * Server-owned identity ([syncId], [code], campaign fields). [consumed] is flipped
 * locally when the code is applied to an order, so it cannot be reused offline;
 * the consumption is reported to the server implicitly by pushing the order that
 * carries [code] (the server then marks it consumed and stops sending it on pull).
 */
@Entity(
    tableName = "discount_codes",
    indices = [
        Index(value = ["syncId"], unique = true),
        Index(value = ["code"], unique = true)
    ]
)
data class DiscountCodeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val syncId: String,
    val code: String,
    val campaignSyncId: String,
    val campaignName: String,
    /** AMOUNT or PERCENT (never NONE/CODE). */
    val valueType: DiscountType,
    /** VND when AMOUNT, percent 1..100 when PERCENT. */
    val value: Int,
    val consumed: Boolean = false,
    val consumedByOrderSyncId: String? = null,
    val consumedAtMs: Long? = null
)
