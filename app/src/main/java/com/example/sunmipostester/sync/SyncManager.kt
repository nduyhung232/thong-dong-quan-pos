package com.example.sunmipostester.sync

import com.example.sunmipostester.common.DiscountType
import com.example.sunmipostester.data.OrderEntity
import com.example.sunmipostester.data.OrderItemEntity
import com.example.sunmipostester.data.OrderWithItems
import com.example.sunmipostester.data.PosRepository
import com.example.sunmipostester.data.ShiftEntity
import com.example.sunmipostester.data.StaffRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/**
 * Orchestrates one sync cycle with the server.
 *
 * ORDER OF OPERATIONS (matches the ownership model):
 *   1. PULL  server -> POS : products, staff credentials, this device's codes.
 *   2. PUSH  POS -> server : orders and shifts recorded since the last sync.
 *
 * Pull first so a freshly-provisioned terminal has master data before it starts
 * pushing. Each direction is independent; a push rejection never blocks a pull.
 *
 * All work runs on [Dispatchers.IO]. Network/parse failures surface as exceptions
 * for the caller (SyncSettingsActivity) to show.
 */
class SyncManager(
    private val repo: PosRepository,
    private val config: SyncConfig
) {

    data class SyncResult(
        val pulledProducts: Int,
        val pulledStaff: Int,
        val pulledCodes: Int,
        val pushedOrders: Int,
        val pushedShifts: Int,
        val rejected: List<String>
    )

    data class PullResult(
        val products: Int,
        val staff: Int,
        val codes: Int
    )

    data class PushResult(
        val orders: Int,
        val shifts: Int,
        val rejected: List<String>
    )

    /** PULL only: pull master data from server. */
    suspend fun pullOnly(): PullResult = withContext(Dispatchers.IO) {
        val (products, staff, codes) = pull(api())
        config.lastSyncAtMs = System.currentTimeMillis()
        PullResult(products, staff, codes)
    }

    /** PUSH only: push local orders and shifts to server. */
    suspend fun pushOnly(): PushResult = withContext(Dispatchers.IO) {
        val (orders, shifts, rejected) = push(api())
        config.lastSyncAtMs = System.currentTimeMillis()
        PushResult(orders, shifts, rejected)
    }

    private fun api(): SyncApi = SyncApi(config.baseUrl, config.token)

    /** GET pull only, without applying — used by the "test connection" button. */
    suspend fun testConnection(): Int = withContext(Dispatchers.IO) {
        val res = api().getJson(PATH_PULL)
        // A 200 with the expected shape means the URL and token are valid.
        res.getJSONArray("products").length()
    }

    /** Full cycle: pull master data, then push local orders/shifts. */
    suspend fun sync(): SyncResult = withContext(Dispatchers.IO) {
        val api = api()
        val (products, staff, codes) = pull(api)
        val (pushedOrders, pushedShifts, rejected) = push(api)
        config.lastSyncAtMs = System.currentTimeMillis()
        SyncResult(products, staff, codes, pushedOrders, pushedShifts, rejected)
    }

    // ---- pull ----------------------------------------------------------------

    private data class PullCounts(val products: Int, val staff: Int, val codes: Int)

    private suspend fun pull(api: SyncApi): PullCounts {
        val res = api.getJson(PATH_PULL)

        val products = res.getJSONArray("products")
        for (i in 0 until products.length()) {
            val p = products.getJSONObject(i)
            repo.upsertServerProduct(
                syncId = p.getString("syncId"),
                name = p.getString("name"),
                price = p.getInt("price"),
                category = p.getString("category"),
                active = p.getBoolean("active"),
                updatedAtMs = p.optLong("updatedAtMs", System.currentTimeMillis())
            )
        }

        val staff = res.getJSONArray("staff")
        for (i in 0 until staff.length()) {
            val s = staff.getJSONObject(i)
            repo.upsertServerStaff(
                syncId = s.getString("syncId"),
                name = s.getString("name"),
                role = StaffRole.valueOf(s.getString("role")),
                pinHash = s.getString("pinHash"),
                pinSalt = s.getString("pinSalt"),
                active = s.getBoolean("active"),
                updatedAtMs = s.optLong("updatedAtMs", System.currentTimeMillis())
            )
        }

        val codes = res.getJSONArray("discountCodes")
        val keepSyncIds = ArrayList<String>(codes.length())
        for (i in 0 until codes.length()) {
            val c = codes.getJSONObject(i)
            val syncId = c.getString("syncId")
            keepSyncIds.add(syncId)
            repo.upsertServerDiscountCode(
                syncId = syncId,
                code = c.getString("code"),
                campaignSyncId = c.getString("campaignSyncId"),
                campaignName = c.getString("campaignName"),
                valueType = DiscountType.valueOf(c.getString("valueType")),
                value = c.getInt("value")
            )
        }
        // Server no longer assigns these codes -> drop the unconsumed ones.
        repo.pruneDiscountCodes(keepSyncIds)

        return PullCounts(products.length(), staff.length(), codes.length())
    }

    // ---- push ----------------------------------------------------------------

    private data class PushCounts(
        val orders: Int,
        val shifts: Int,
        val rejected: List<String>
    )

    private suspend fun push(api: SyncApi): PushCounts {
        val orders = repo.unsyncedOrdersWithItems()
        val shifts = repo.unsyncedShifts()
        if (orders.isEmpty() && shifts.isEmpty()) {
            return PushCounts(0, 0, emptyList())
        }

        val body = JSONObject().apply {
            put("clientTimeMs", System.currentTimeMillis())
            put("orders", JSONArray().apply { orders.forEach { put(orderJson(it)) } })
            put("shifts", JSONArray().apply { shifts.forEach { put(shiftJson(it)) } })
        }

        val res = api.postJson(PATH_PUSH, body)
        val serverTimeMs = res.optLong("serverTimeMs", System.currentTimeMillis())

        val acceptedOrders = res.optJSONArray("acceptedOrderIds") ?: JSONArray()
        for (i in 0 until acceptedOrders.length()) {
            repo.markOrderSynced(acceptedOrders.getString(i), serverTimeMs)
        }
        val acceptedShifts = res.optJSONArray("acceptedShiftIds") ?: JSONArray()
        for (i in 0 until acceptedShifts.length()) {
            repo.markShiftSynced(acceptedShifts.getString(i), serverTimeMs)
        }

        val rejected = ArrayList<String>()
        val rejArr = res.optJSONArray("rejected") ?: JSONArray()
        for (i in 0 until rejArr.length()) {
            val r = rejArr.getJSONObject(i)
            rejected.add("${r.optString("kind")} ${r.optString("syncId")}: ${r.optString("reason")}")
        }

        return PushCounts(acceptedOrders.length(), acceptedShifts.length(), rejected)
    }

    // ---- JSON builders (match server OrderPush / ShiftPush shapes) ------------

    private fun orderJson(ow: OrderWithItems): JSONObject {
        val o: OrderEntity = ow.order
        return JSONObject().apply {
            put("syncId", o.syncId)
            put("subtotal", o.subtotal)
            put("discountAmount", o.discountAmount)
            put("total", o.total)
            put("discountType", o.discountType.name)
            put("discountInput", o.discountInput)
            putNullable("discountCode", o.discountCode)
            put("orderType", o.orderType.name)
            put("paymentMethod", o.paymentMethod.name)
            put("cashReceived", o.cashReceived)
            put("changeAmount", o.changeAmount)
            put("status", o.status.name)
            put("createdAtMs", o.createdAtMs)
            putNullable("shiftSyncId", o.shiftSyncId)
            putNullable("staffSyncId", o.staffSyncId)
            putNullable("staffName", o.staffName)
            putNullable("cancelledByStaffSyncId", o.cancelledByStaffSyncId)
            putNullable("cancelledAtMs", o.cancelledAtMs)
            put("items", JSONArray().apply { ow.items.forEach { put(itemJson(it)) } })
        }
    }

    private fun itemJson(it: OrderItemEntity): JSONObject = JSONObject().apply {
        put("syncId", it.syncId)
        putNullable("productSyncId", it.productSyncId)
        put("productName", it.productName)
        put("unitPrice", it.unitPrice)
        put("quantity", it.quantity)
    }

    private fun shiftJson(s: ShiftEntity): JSONObject = JSONObject().apply {
        put("syncId", s.syncId)
        put("openedAtMs", s.openedAtMs)
        putNullable("closedAtMs", s.closedAtMs)
        put("openingCash", s.openingCash)
        putNullable("countedCash", s.countedCash)
        putNullable("expectedCash", s.expectedCash)
        putNullable("cashDifference", s.cashDifference)
        putNullable("cashBreakdown", s.cashBreakdown)
        put("status", s.status.name)
        putNullable("staffSyncId", s.staffSyncId)
        putNullable("staffName", s.staffName)
        putNullable("closedByStaffSyncId", s.closedByStaffSyncId)
    }

    /** Put an explicit JSON null when the value is null (server zod expects null, not missing). */
    private fun JSONObject.putNullable(key: String, value: Any?) {
        put(key, value ?: JSONObject.NULL)
    }

    companion object {
        private const val PATH_PULL = "/api/sync/pull"
        private const val PATH_PUSH = "/api/sync/push"
    }
}
