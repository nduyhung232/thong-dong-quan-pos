package com.example.sunmipostester.backup

import android.content.Context
import com.example.sunmipostester.data.ExportSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Writes business data to files the shop owner can copy off the terminal.
 *
 * Output goes to the app's external files dir (`Android/data/<pkg>/files/export`),
 * which is app-scoped and needs no runtime storage permission on any supported
 * API level.
 *
 * SECURITY: exports never contain PIN hashes or salts — see
 * [com.example.sunmipostester.data.PosRepository.exportSnapshot].
 */
class DataExporter(private val context: Context) {

    data class Result(val files: List<File>, val directory: File)

    /** Write one CSV per table. Returns the files created. */
    suspend fun exportCsv(snapshot: ExportSnapshot): Result = withContext(Dispatchers.IO) {
        val dir = exportDir()
        val stamp = fileStamp(snapshot.generatedAtMs)

        val files = listOf(
            write(File(dir, "products_$stamp.csv"), productsCsv(snapshot)),
            write(File(dir, "orders_$stamp.csv"), ordersCsv(snapshot)),
            write(File(dir, "order_items_$stamp.csv"), itemsCsv(snapshot)),
            write(File(dir, "cash_expenses_$stamp.csv"), expensesCsv(snapshot)),
            write(File(dir, "shifts_$stamp.csv"), shiftsCsv(snapshot)),
            write(File(dir, "staff_$stamp.csv"), staffCsv(snapshot))
        )
        Result(files, dir)
    }

    /**
     * Copy the raw SQLite database (plus its WAL/SHM sidecars when present) so the
     * whole state can be restored. Sidecars matter: copying only the .db file can
     * miss recently committed transactions.
     */
    suspend fun backupDatabase(): Result = withContext(Dispatchers.IO) {
        val dir = exportDir()
        val stamp = fileStamp(System.currentTimeMillis())
        val dbFile = context.getDatabasePath("pos.db")

        val copied = mutableListOf<File>()
        listOf("" to "pos_$stamp.db", "-wal" to "pos_$stamp.db-wal", "-shm" to "pos_$stamp.db-shm")
            .forEach { (suffix, targetName) ->
                val source = File(dbFile.parentFile, dbFile.name + suffix)
                if (source.exists()) {
                    val target = File(dir, targetName)
                    source.copyTo(target, overwrite = true)
                    copied.add(target)
                }
            }
        Result(copied, dir)
    }

    // ---- CSV builders ----------------------------------------------------

    private fun productsCsv(s: ExportSnapshot): String = buildCsv(
        header = listOf("id", "name", "price", "category", "active"),
        rows = s.products.map { listOf(it.id, it.name, it.price, it.category, it.active) }
    )

    private fun ordersCsv(s: ExportSnapshot): String = buildCsv(
        header = listOf(
            "id", "createdAt", "total", "paymentMethod", "cashReceived", "change",
            "status", "shiftId", "staffId", "staffName", "cancelledByStaffId", "cancelledAt"
        ),
        rows = s.orders.map {
            listOf(
                it.id, isoTime(it.createdAtMs), it.total, it.paymentMethod.name,
                it.cashReceived, it.changeAmount, it.status.name, it.shiftId ?: "",
                it.staffId ?: "", it.staffName ?: "",
                it.cancelledByStaffId ?: "", it.cancelledAtMs?.let(::isoTime) ?: ""
            )
        }
    )

    private fun itemsCsv(s: ExportSnapshot): String = buildCsv(
        header = listOf("id", "orderId", "productId", "productName", "unitPrice", "quantity", "lineTotal"),
        rows = s.items.map {
            listOf(it.id, it.orderId, it.productId, it.productName, it.unitPrice, it.quantity, it.lineTotal)
        }
    )

    private fun shiftsCsv(s: ExportSnapshot): String = buildCsv(
        header = listOf(
            "id", "openedAt", "closedAt", "openingCash", "countedCash",
            "expectedCash", "cashDifference", "cashBreakdown", "status", "staffId", "staffName", "closedByStaffId"
        ),
        rows = s.shifts.map {
            listOf(
                it.id, isoTime(it.openedAtMs), it.closedAtMs?.let(::isoTime) ?: "",
                it.openingCash, it.countedCash ?: "", it.expectedCash ?: "",
                it.cashDifference ?: "", it.cashBreakdown ?: "", it.status.name,
                it.staffId ?: "", it.staffName ?: "", it.closedByStaffId ?: ""
            )
        }
    )

    private fun expensesCsv(s: ExportSnapshot): String = buildCsv(
        header = listOf("id", "shiftId", "createdAt", "description", "amount", "staffId", "staffName"),
        rows = s.expenses.map {
            listOf(it.id, it.shiftId, isoTime(it.createdAtMs), it.description, it.amount, it.staffId ?: "", it.staffName ?: "")
        }
    )

    /** Staff export carries identity and role only — no credential fields. */
    private fun staffCsv(s: ExportSnapshot): String = buildCsv(
        header = listOf("id", "name", "role", "active"),
        rows = s.staff.map { listOf(it.id, it.name, it.role.name, it.active) }
    )

    // ---- helpers ---------------------------------------------------------

    private fun exportDir(): File =
        File(context.getExternalFilesDir(null) ?: context.filesDir, "export")
            .apply { mkdirs() }

    private fun write(file: File, content: String): File =
        file.apply { writeText(content, Charsets.UTF_8) }

    private fun buildCsv(header: List<String>, rows: List<List<Any?>>): String {
        val sb = StringBuilder()
        // BOM so Excel opens UTF-8 Vietnamese text correctly.
        sb.append('\uFEFF')
        sb.append(header.joinToString(",")).append('\n')
        rows.forEach { row ->
            sb.append(row.joinToString(",") { escapeCsv(it?.toString().orEmpty()) }).append('\n')
        }
        return sb.toString()
    }

    /** Quote values containing separators, quotes or newlines; double inner quotes. */
    private fun escapeCsv(value: String): String {
        val needsQuoting = value.any { it == ',' || it == '"' || it == '\n' || it == '\r' }
        val escaped = value.replace("\"", "\"\"")
        return if (needsQuoting) "\"$escaped\"" else escaped
    }

    private fun fileStamp(ms: Long): String =
        SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date(ms))

    private fun isoTime(ms: Long): String =
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date(ms))
}
