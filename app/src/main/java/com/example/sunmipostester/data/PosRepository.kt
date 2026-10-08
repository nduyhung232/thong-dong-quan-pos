package com.example.sunmipostester.data

import android.content.Context
import com.example.sunmipostester.auth.PinHasher
import com.example.sunmipostester.common.DiscountCalculator
import com.example.sunmipostester.common.DiscountType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Single entry point for all persistence. Keeps Room off the UI thread and hides
 * DAO details from the Activities.
 */
class PosRepository(context: Context) {

    private val db = PosDatabase.get(context)
    private val productDao = db.productDao()
    private val orderDao = db.orderDao()
    private val shiftDao = db.shiftDao()
    private val reportDao = db.reportDao()
    private val staffDao = db.staffDao()
    private val discountCodeDao = db.discountCodeDao()
    private val tableDraftDao = db.tableDraftDao()
    private val cashExpenseDao = db.cashExpenseDao()
    private val toppingDao = db.toppingDao()

    // ---- Products --------------------------------------------------------

    suspend fun activeProducts(): List<ProductEntity> = io { productDao.activeProducts() }

    suspend fun allProducts(): List<ProductEntity> = io { productDao.allProducts() }

    suspend fun activeCategories(): List<String> = io { productDao.activeCategories() }

    suspend fun addProduct(name: String, price: Int, category: String): Long = io {
        productDao.insert(ProductEntity(name = name, price = price, category = category))
    }

    suspend fun updateProduct(product: ProductEntity) = io { productDao.update(product) }

    /** Soft-delete so historical orders stay explainable. */
    suspend fun deactivateProduct(id: Long) = io { productDao.deactivate(id) }

    suspend fun reactivateProduct(id: Long) = io { productDao.reactivate(id) }

    /**
     * Insert the starter menu the first time the app runs. Does nothing if the
     * products table already has rows, so it never overwrites the shop's data.
     */
    suspend fun seedMenuIfEmpty() = io {
        if (productDao.count() == 0) {
            productDao.insertAll(MenuSeed.starterProducts())
        }
    }

    /** Pre-seed default manager accounts if staff table is empty. */
    suspend fun seedStaffIfEmpty() = io {
        if (staffDao.count() == 0) {
            for (staff in StaffSeed.defaultStaff()) {
                staffDao.insert(staff)
            }
        }
    }

    // ---- Orders ----------------------------------------------------------

    /**
     * Persist a paid order together with its line items in one transaction.
     *
     * The discount is computed with the SHARED [DiscountCalculator] so the figures
     * match what the server re-verifies on push (a mismatch = a rejected sale).
     * All sync keys (syncId) are generated here; `syncedAtMs` stays null until the
     * server accepts the order. Line items store name/price snapshots.
     *
     * @param appliedCode the discount code being spent, or null. Only used when
     *        [discountType] is [DiscountType.CODE]; it is marked consumed here.
     */
    suspend fun savePaidOrder(
        lines: List<CartLine>,
        method: PaymentMethod,
        cashReceived: Int,
        staffId: Long?,
        staffName: String?,
        orderType: OrderType = OrderType.DINE_IN,
        discountType: DiscountType = DiscountType.NONE,
        discountInput: Int = 0,
        appliedCode: DiscountCodeEntity? = null
    ): Long = io {
        val nowMs = System.currentTimeMillis()
        val subtotal = lines.sumOf { it.lineTotal }

        // One auditable place for the money maths, identical to the server rule.
        val result = DiscountCalculator.compute(
            subtotal = subtotal,
            type = discountType,
            input = discountInput,
            codeValueType = appliedCode?.valueType ?: DiscountType.NONE,
            codeValue = appliedCode?.value ?: 0
        )
        val total = result.total
        // Change only applies to cash; non-cash orders carry zero cash fields so
        // they pass the server's payment-consistency check.
        val isCash = method == PaymentMethod.CASH
        val effectiveCashReceived = if (isCash) cashReceived else 0
        val change = if (isCash) (effectiveCashReceived - total).coerceAtLeast(0) else 0

        val currentShift = shiftDao.openShift()
        val staffSyncId = staffId?.let { staffDao.byId(it)?.syncId }
        val orderSyncId = UUID.randomUUID().toString()

        val order = OrderEntity(
            syncId = orderSyncId,
            subtotal = subtotal,
            discountAmount = result.discountAmount,
            total = total,
            discountType = discountType,
            discountInput = discountInput,
            discountCode = if (discountType == DiscountType.CODE) appliedCode?.code else null,
            orderType = orderType,
            paymentMethod = method,
            cashReceived = effectiveCashReceived,
            changeAmount = change,
            status = OrderStatus.PAID,
            createdAtMs = nowMs,
            shiftId = currentShift?.id,
            shiftSyncId = currentShift?.syncId,
            staffId = staffId,
            staffName = staffName,
            staffSyncId = staffSyncId,
            syncedAtMs = null
        )
        val items = lines.map { line ->
            val fullName = if (line.selectedToppings.isNotEmpty()) {
                "${line.product.name} (+${line.toppingsDisplay})"
            } else {
                line.product.name
            }
            OrderItemEntity(
                orderId = 0, // assigned inside the transaction
                productId = line.product.id,
                productSyncId = line.product.syncId,
                productName = fullName,
                unitPrice = line.unitPriceWithToppings,
                quantity = line.quantity
            )
        }
        val orderId = orderDao.saveOrder(order, items)

        // Single-use: mark the code spent locally so it cannot be reused offline.
        // The spend reaches the server implicitly when this order is pushed.
        if (discountType == DiscountType.CODE && appliedCode != null) {
            discountCodeDao.markConsumed(appliedCode.syncId, orderSyncId, nowMs)
        }
        orderId
    }

    suspend fun recentOrders(limit: Int = 200): List<OrderEntity> = io {
        orderDao.recentOrders(limit)
    }

    suspend fun ordersBetween(fromMs: Long, toMs: Long, limit: Int, offset: Int): List<OrderEntity> = io {
        orderDao.ordersBetween(fromMs, toMs, limit, offset)
    }

    suspend fun orderWithItems(orderId: Long): OrderWithItems? = io {
        orderDao.orderWithItems(orderId)
    }

    /**
     * Void an order, recording which manager authorised it. Clears `syncedAtMs`
     * so the cancellation is re-pushed to the server on the next sync.
     */
    suspend fun cancelOrder(orderId: Long, staffId: Long?) = io {
        val staffSyncId = staffId?.let { staffDao.byId(it)?.syncId }
        orderDao.cancelOrder(orderId, staffId, staffSyncId, System.currentTimeMillis())
    }

    /** Paid revenue and order count within a time window (inclusive). */
    suspend fun revenueSummary(fromMs: Long, toMs: Long): Pair<Int, Int> = io {
        orderDao.revenueBetween(fromMs, toMs) to orderDao.paidCountBetween(fromMs, toMs)
    }

    // ---- Dine-in tables --------------------------------------------------

    /** Save an unpaid dine-in order without exposing it to financial reports or server sync. */
    suspend fun saveTableDraft(tableName: String, lines: List<CartLine>) = io {
        require(tableName.isNotBlank())
        val snapshots = lines.map { line ->
            TableDraftItemEntity(
                tableName = tableName,
                productId = line.product.id,
                productSyncId = line.product.syncId,
                productName = line.product.name,
                category = line.product.category,
                unitPrice = line.product.price,
                quantity = line.quantity
            )
        }
        tableDraftDao.saveDraft(
            TableDraftEntity(tableName = tableName),
            snapshots
        )
    }

    suspend fun tableDrafts(): List<TableOrderDraft> = io {
        tableDraftDao.drafts().map { draft ->
            draft.toTableOrderDraft(tableDraftDao.items(draft.tableName))
        }
    }

    suspend fun tableDraft(tableName: String): TableOrderDraft? = io {
        tableDraftDao.drafts().firstOrNull { it.tableName == tableName }
            ?.let { it.toTableOrderDraft(tableDraftDao.items(tableName)) }
    }

    suspend fun deleteTableDraft(tableName: String) = io {
        tableDraftDao.deleteDraft(tableName)
    }

    private fun TableDraftEntity.toTableOrderDraft(items: List<TableDraftItemEntity>) =
        TableOrderDraft(
            tableName = tableName,
            updatedAtMs = updatedAtMs,
            lines = items.map { item ->
                CartLine(
                    product = ProductEntity(
                        id = item.productId,
                        syncId = item.productSyncId,
                        name = item.productName,
                        price = item.unitPrice,
                        category = item.category
                    ),
                    quantity = item.quantity
                )
            }
        )

    // ---- Shifts ----------------------------------------------------------

    /** The currently open shift, or null if no shift is open. */
    suspend fun openShift(): ShiftEntity? = io { shiftDao.openShift() }

    /**
     * Open a new shift with the given starting cash float.
     * @return the new shift, or null if a shift is already open (guards against
     *         two overlapping shifts corrupting the cash reconciliation).
     */
    suspend fun startShift(
        openingCash: Int,
        staffId: Long?,
        staffName: String?
    ): ShiftEntity? = io {
        if (shiftDao.openShift() != null) return@io null
        val shift = ShiftEntity(
            openedAtMs = System.currentTimeMillis(),
            openingCash = openingCash,
            status = ShiftStatus.OPEN,
            staffId = staffId,
            staffName = staffName,
            staffSyncId = staffId?.let { staffDao.byId(it)?.syncId },
            syncedAtMs = null
        )
        val id = shiftDao.insert(shift)
        shift.copy(id = id)
    }

    /**
     * Live figures for the open shift, used by the shift screen before closing.
     * Returns null when no shift is open.
     */
    suspend fun openShiftSummary(): ShiftSummary? = io {
        val shift = shiftDao.openShift() ?: return@io null
        buildSummary(shift)
    }

    /** Figures for any shift (open or closed). */
    suspend fun shiftSummary(shiftId: Long): ShiftSummary? = io {
        val shift = shiftDao.shiftById(shiftId) ?: return@io null
        buildSummary(shift)
    }

    private suspend fun buildSummary(shift: ShiftEntity): ShiftSummary {
        val cashSales = shiftDao.cashSalesOfShift(shift.id)
        val transferSales = shiftDao.transferSalesOfShift(shift.id)
        val cardSales = shiftDao.cardSalesOfShift(shift.id)
        val totalSales = shiftDao.totalSalesOfShift(shift.id)
        val orderCount = shiftDao.paidCountOfShift(shift.id)
        val cashExpenses = cashExpenseDao.totalForShift(shift.id)
        return ShiftSummary(
            shift = shift,
            cashSales = cashSales,
            transferSales = transferSales,
            cardSales = cardSales,
            totalSales = totalSales,
            orderCount = orderCount,
            cashExpenses = cashExpenses,
            expectedCash = shift.openingCash + cashSales - cashExpenses
        )
    }

    /** Persist a cash expense against the current shift; never allow the drawer balance to go negative. */
    suspend fun recordCashExpense(
        description: String,
        amount: Int,
        staffId: Long?,
        staffName: String?
    ): CashExpenseEntity = io {
        require(description.isNotBlank()) { "Nhập nội dung chi" }
        require(amount > 0) { "Số tiền chi phải lớn hơn 0" }
        val shift = shiftDao.openShift() ?: error("Chưa có ca đang mở")
        val currentCash = shift.openingCash + shiftDao.cashSalesOfShift(shift.id) - cashExpenseDao.totalForShift(shift.id)
        require(amount <= currentCash) { "Số tiền chi vượt quá tiền két dự kiến (${TextFormat.vnd(currentCash)})" }
        val expense = CashExpenseEntity(
            shiftId = shift.id,
            amount = amount,
            description = description.trim(),
            staffId = staffId,
            staffName = staffName
        )
        val id = cashExpenseDao.insert(expense)
        expense.copy(id = id)
    }

    suspend fun cashExpensesForShift(shiftId: Long): List<CashExpenseEntity> = io {
        cashExpenseDao.allForShift(shiftId)
    }

    /**
     * Close the open shift with the physically counted cash amount and record the
     * variance. Returns the closed shift summary, or null if no shift was open.
     */
    suspend fun closeShift(
        countedCash: Int,
        closedByStaffId: Long?,
        cashBreakdown: String? = null
    ): ShiftSummary? = io {
        val shift = shiftDao.openShift() ?: return@io null
        val cashSales = shiftDao.cashSalesOfShift(shift.id)
        val expected = shift.openingCash + cashSales - cashExpenseDao.totalForShift(shift.id)
        val closed = shift.copy(
            closedAtMs = System.currentTimeMillis(),
            countedCash = countedCash,
            expectedCash = expected,
            cashDifference = countedCash - expected,
            cashBreakdown = cashBreakdown,
            status = ShiftStatus.CLOSED,
            closedByStaffId = closedByStaffId,
            closedByStaffSyncId = closedByStaffId?.let { staffDao.byId(it)?.syncId },
            // Re-push so the server stores the closing figures too.
            syncedAtMs = null
        )
        shiftDao.update(closed)
        buildSummary(closed)
    }

    suspend fun recentShifts(limit: Int = 100): List<ShiftEntity> = io {
        shiftDao.recentShifts(limit)
    }

    // ---- Reports ---------------------------------------------------------

    /** Full revenue report for a time window (inclusive). */
    suspend fun revenueReport(fromMs: Long, toMs: Long): RevenueReport = io {
        val byMethod = reportDao.revenueByMethod(fromMs, toMs)
        val top = reportDao.topProducts(fromMs, toMs)
        val totalRevenue = byMethod.sumOf { it.revenue }
        val totalOrders = byMethod.sumOf { it.orderCount }
        RevenueReport(
            fromMs = fromMs,
            toMs = toMs,
            totalRevenue = totalRevenue,
            totalOrders = totalOrders,
            byMethod = byMethod,
            topProducts = top
        )
    }

    // ---- Staff & authentication -----------------------------------------

    suspend fun activeStaff(): List<StaffEntity> = io { staffDao.activeStaff() }

    suspend fun allStaff(): List<StaffEntity> = io { staffDao.allStaff() }

    /** True when no staff exist yet — the app must then run first-time setup. */
    suspend fun needsInitialSetup(): Boolean = io { staffDao.count() == 0 }

    /**
     * Create a staff member. The plaintext [pin] is hashed immediately and never
     * persisted. Returns null if the PIN fails policy.
     */
    suspend fun createStaff(name: String, role: StaffRole, pin: String): Long? = io {
        if (!PinHasher.isPolicyValid(pin)) return@io null
        val credential = PinHasher.create(pin)
        staffDao.insert(
            StaffEntity(
                name = name,
                role = role,
                pinHash = credential.hash,
                pinSalt = credential.salt
            )
        )
    }

    /** Rename or change the role of a staff member (PIN untouched). */
    suspend fun updateStaffProfile(staff: StaffEntity, name: String, role: StaffRole): Boolean =
        io {
            // Never leave the shop without a manager who can administer it.
            if (staff.role == StaffRole.MANAGER && role != StaffRole.MANAGER &&
                staffDao.activeManagerCount() <= 1
            ) {
                return@io false
            }
            staffDao.update(staff.copy(name = name, role = role))
            true
        }

    /** Reset someone's PIN. Returns false if the new PIN fails policy. */
    suspend fun resetStaffPin(staff: StaffEntity, newPin: String): Boolean = io {
        if (!PinHasher.isPolicyValid(newPin)) return@io false
        val credential = PinHasher.create(newPin)
        staffDao.update(staff.copy(pinHash = credential.hash, pinSalt = credential.salt))
        true
    }

    /**
     * Soft-delete a staff member. Refuses to deactivate the last active manager,
     * which would otherwise lock everyone out of administration.
     */
    suspend fun deactivateStaff(staff: StaffEntity): Boolean = io {
        if (staff.role == StaffRole.MANAGER && staffDao.activeManagerCount() <= 1) {
            return@io false
        }
        staffDao.deactivate(staff.id)
        true
    }

    suspend fun reactivateStaff(id: Long) = io { staffDao.reactivate(id) }

    /**
     * Verify a PIN for the given staff id.
     * @return the staff row on success, null on failure. The caller must not log
     *         the PIN or distinguish "wrong PIN" from "unknown user" to the user.
     */
    suspend fun authenticate(staffId: Long, pin: String): StaffEntity? = io {
        val staff = staffDao.byId(staffId) ?: return@io null
        if (!staff.active) return@io null
        if (!PinHasher.verify(pin, staff.pinHash, staff.pinSalt)) return@io null
        staff
    }

    // ---- Sync support ----------------------------------------------------

    /** Upsert a product delivered by the server, keyed by syncId (server-owned). */
    suspend fun upsertServerProduct(
        syncId: String,
        name: String,
        price: Int,
        category: String,
        active: Boolean,
        updatedAtMs: Long
    ) = io {
        val existing = productDao.bySyncId(syncId)
        if (existing == null) {
            productDao.insert(
                ProductEntity(
                    syncId = syncId, name = name, price = price,
                    category = category, active = active, updatedAtMs = updatedAtMs
                )
            )
        } else {
            productDao.update(
                existing.copy(
                    name = name, price = price, category = category,
                    active = active, updatedAtMs = updatedAtMs
                )
            )
        }
    }

    /** Upsert a staff credential delivered by the server, keyed by syncId. */
    suspend fun upsertServerStaff(
        syncId: String,
        name: String,
        role: StaffRole,
        pinHash: String,
        pinSalt: String,
        active: Boolean,
        updatedAtMs: Long
    ) = io {
        val existing = staffDao.bySyncId(syncId)
        if (existing == null) {
            staffDao.insert(
                StaffEntity(
                    syncId = syncId, name = name, role = role,
                    pinHash = pinHash, pinSalt = pinSalt, active = active,
                    updatedAtMs = updatedAtMs
                )
            )
        } else {
            staffDao.update(
                existing.copy(
                    name = name, role = role, pinHash = pinHash,
                    pinSalt = pinSalt, active = active, updatedAtMs = updatedAtMs
                )
            )
        }
    }

    /** Upsert one discount code assignment from the server, keyed by syncId. */
    suspend fun upsertServerDiscountCode(
        syncId: String,
        code: String,
        campaignSyncId: String,
        campaignName: String,
        valueType: DiscountType,
        value: Int
    ) = io {
        val existing = discountCodeDao.bySyncId(syncId)
        if (existing == null) {
            discountCodeDao.insert(
                DiscountCodeEntity(
                    syncId = syncId, code = code, campaignSyncId = campaignSyncId,
                    campaignName = campaignName, valueType = valueType, value = value
                )
            )
        } else if (!existing.consumed) {
            // Refresh mutable campaign fields; never resurrect a consumed code.
            discountCodeDao.update(
                existing.copy(
                    code = code, campaignSyncId = campaignSyncId,
                    campaignName = campaignName, valueType = valueType, value = value
                )
            )
        } else {
            // no-op
        }
    }

    /** Drop unconsumed codes the server no longer assigns to this device. */
    suspend fun pruneDiscountCodes(keepSyncIds: List<String>) = io {
        if (keepSyncIds.isEmpty()) discountCodeDao.deleteAllUnconsumed()
        else discountCodeDao.deleteUnconsumedNotIn(keepSyncIds)
    }

    suspend fun availableDiscountCodes(): List<DiscountCodeEntity> = io { discountCodeDao.available() }

    suspend fun discountCodeByCode(code: String): DiscountCodeEntity? = io { discountCodeDao.byCode(code) }

    /** Orders awaiting server acceptance, each with its line items, for push. */
    suspend fun unsyncedOrdersWithItems(): List<OrderWithItems> = io {
        orderDao.unsyncedOrders().map { OrderWithItems(it, orderDao.itemsOf(it.id)) }
    }

    suspend fun unsyncedShifts(): List<ShiftEntity> = io { shiftDao.unsyncedShifts() }

    suspend fun markOrderSynced(syncId: String, atMs: Long) = io { orderDao.markSynced(syncId, atMs) }

    suspend fun markShiftSynced(syncId: String, atMs: Long) = io { shiftDao.markSynced(syncId, atMs) }

    // ---- Toppings --------------------------------------------------------

    suspend fun upsertServerTopping(
        syncId: String,
        name: String,
        price: Int,
        active: Boolean,
        updatedAtMs: Long
    ) = io {
        val existing = toppingDao.getBySyncId(syncId)
        val entity = ToppingEntity(
            id = existing?.id ?: 0,
            syncId = syncId,
            name = name,
            price = price,
            active = active,
            updatedAtMs = updatedAtMs
        )
        toppingDao.upsert(entity)
    }

    suspend fun setProductToppings(productSyncId: String, toppingSyncIds: List<String>) = io {
        toppingDao.clearProductToppings(productSyncId)
        if (toppingSyncIds.isNotEmpty()) {
            val links = toppingSyncIds.map { ProductToppingEntity(productSyncId, it) }
            toppingDao.insertProductToppings(links)
        }
    }

    suspend fun getToppingsForProduct(productSyncId: String): List<ToppingEntity> = io {
        toppingDao.getToppingsForProduct(productSyncId)
    }

    suspend fun getAllActiveToppings(): List<ToppingEntity> = io {
        toppingDao.getAllActive()
    }

    // ---- Export ----------------------------------------------------------

    /**
     * Snapshot of all business data for backup/export.
     *
     * SECURITY: staff PIN hashes and salts are deliberately EXCLUDED so an
     * exported file cannot be used for offline brute-forcing of PINs.
     */
    suspend fun exportSnapshot(): ExportSnapshot = io {
        ExportSnapshot(
            generatedAtMs = System.currentTimeMillis(),
            products = productDao.allProducts(),
            orders = orderDao.recentOrders(limit = Int.MAX_VALUE),
            items = reportDao.allOrderItems(),
            expenses = cashExpenseDao.all(),
            shifts = shiftDao.recentShifts(limit = Int.MAX_VALUE),
            staff = staffDao.allStaff().map {
                StaffExport(id = it.id, name = it.name, role = it.role, active = it.active)
            }
        )
    }

    private suspend fun <T> io(block: suspend () -> T): T =
        withContext(Dispatchers.IO) { block() }
}
