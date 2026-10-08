package com.example.sunmipostester.printer

import com.example.sunmipostester.data.CartLine
import com.example.sunmipostester.data.OrderWithItems
import com.example.sunmipostester.data.PaymentMethod
import com.example.sunmipostester.data.TextFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * High-level ESC/POS actions used by the UI. Transport-agnostic: give it any
 * [PrinterConnection] (USB or network).
 *
 * Every public action is a suspend fun that runs the blocking socket/USB I/O on
 * [Dispatchers.IO], so the UI never blocks.
 */
class ReceiptPrinter(private val connectionFactory: () -> PrinterConnection) {

    /** Result of a fire-and-forget action. */
    data class ActionResult(val success: Boolean, val message: String)

    /** Dine-in kitchen slip: table name, item names, and quantities only. */
    suspend fun printTableTicket(tableName: String, lines: List<CartLine>): ActionResult = io {
        val conn = connectionFactory()
        try {
            conn.open()
            val context = com.example.sunmipostester.PosApplication.instance
            val bitmap = ReceiptTemplateRenderer.renderKitchenTicketBitmap(context, 0, lines, tableName)
            val chunks = ReceiptTemplateRenderer.bitmapToChunkedEscPos(bitmap, stripHeight = 24)

            conn.write(EscPos.INIT)
            for (chunk in chunks) {
                conn.write(chunk)
                Thread.sleep(8)
            }
            conn.write(EscPos.feed(4))
            conn.write(EscPos.CUT)
            ActionResult(true, "Đã in phiếu bàn")
        } catch (e: Exception) {
            ActionResult(false, "Lỗi in phiếu bàn: ${e.message}")
        } finally {
            conn.close()
        }
    }

    /** Kitchen copy for a completed sale: order identifier plus names and quantities only. */
    suspend fun printKitchenCopy(data: OrderWithItems, tableName: String? = null): ActionResult = io {
        val conn = connectionFactory()
        try {
            conn.open()
            val context = com.example.sunmipostester.PosApplication.instance
            val bitmap = ReceiptTemplateRenderer.renderKitchenCopyBitmap(context, data, tableName)
            val chunks = ReceiptTemplateRenderer.bitmapToChunkedEscPos(bitmap, stripHeight = 24)

            conn.write(EscPos.INIT)
            for (chunk in chunks) {
                conn.write(chunk)
                Thread.sleep(8)
            }
            conn.write(EscPos.feed(4))
            conn.write(EscPos.CUT)
            ActionResult(true, "Đã in phiếu bếp")
        } catch (e: Exception) {
            ActionResult(false, "Lỗi in phiếu bếp: ${e.message}")
        } finally {
            conn.close()
        }
    }

    /** Open the cash drawer wired to the receipt printer. */
    suspend fun openDrawer(): ActionResult = io {
        val conn = connectionFactory()
        try {
            conn.open()
            conn.write(EscPos.openDrawer())
            ActionResult(true, "Đã gửi lệnh mở két")
        } catch (e: Exception) {
            ActionResult(false, "Lỗi mở két: ${e.message}")
        } finally {
            conn.close()
        }
    }

    /** Print a diagnostic receipt using the visual XML template and safe chunked rendering. */
    suspend fun printTest(): ActionResult = io {
        val conn = connectionFactory()
        try {
            conn.open()
            val context = com.example.sunmipostester.PosApplication.instance
            val bitmap = ReceiptTemplateRenderer.renderTestReceiptBitmap(context)
            val chunks = ReceiptTemplateRenderer.bitmapToChunkedEscPos(bitmap, stripHeight = 24)

            conn.write(EscPos.INIT)
            for (chunk in chunks) {
                conn.write(chunk)
                Thread.sleep(8)
            }
            conn.write(EscPos.feed(4))
            conn.write(EscPos.CUT)
            ActionResult(true, "Đã gửi lệnh in thử mẫu hóa đơn")
        } catch (e: Exception) {
            ActionResult(false, "Lỗi in: ${e.message}")
        } finally {
            conn.close()
        }
    }

    /**
     * Print the kitchen/bar order ticket (KOT) for the barista. Shows items and
     * quantities only — no prices, since it's a preparation slip.
     */
    suspend fun printKitchenTicket(
        orderNumber: Int,
        lines: List<CartLine>,
        tableName: String? = null
    ): ActionResult = io {
        val conn = connectionFactory()
        try {
            conn.open()
            val context = com.example.sunmipostester.PosApplication.instance
            val bitmap = ReceiptTemplateRenderer.renderKitchenTicketBitmap(context, orderNumber, lines, tableName)
            val chunks = ReceiptTemplateRenderer.bitmapToChunkedEscPos(bitmap, stripHeight = 24)

            conn.write(EscPos.INIT)
            for (chunk in chunks) {
                conn.write(chunk)
                Thread.sleep(8)
            }
            conn.write(EscPos.feed(4))
            conn.write(EscPos.CUT)
            ActionResult(true, "Đã in phiếu pha chế")
        } catch (e: Exception) {
            ActionResult(false, "Lỗi in phiếu: ${e.message}")
        } finally {
            conn.close()
        }
    }

    /**
     * Print the customer payment receipt using the visual XML template (576px / 80mm).
     * Slices the rendered bitmap into 24-dot strips (<2KB each) with pacing to completely
     * prevent printer hardware buffer overflow.
     */
    suspend fun printReceipt(
        data: OrderWithItems,
        isReprint: Boolean = false,
        tableName: String? = null
    ): ActionResult = io {
        val order = data.order
        val conn = connectionFactory()
        try {
            conn.open()
            val context = com.example.sunmipostester.PosApplication.instance
            val bitmap = ReceiptTemplateRenderer.renderReceiptBitmap(context, data, isReprint, tableName)
            val chunks = ReceiptTemplateRenderer.bitmapToChunkedEscPos(bitmap, stripHeight = 24)

            conn.write(EscPos.INIT)
            for (chunk in chunks) {
                conn.write(chunk)
                Thread.sleep(8)
            }
            if (order.paymentMethod == PaymentMethod.CASH && !isReprint) {
                conn.write(EscPos.openDrawer())
            }
            conn.write(EscPos.feed(4))
            conn.write(EscPos.CUT)
            ActionResult(true, if (isReprint) "Đã in lại hóa đơn" else "Đã in hóa đơn")
        } catch (e: Exception) {
            ActionResult(false, "Lỗi in hóa đơn: ${e.message}")
        } finally {
            conn.close()
        }
    }

    /**
     * Read the cash-drawer status via DLE EOT 1.
     * @return true = open, false = closed, null = could not determine
     *         (printer didn't answer, or transport can't read back).
     */
    suspend fun readDrawerStatus(): Boolean? = io {
        val conn = connectionFactory()
        try {
            conn.open()
            val resp = conn.writeAndRead(EscPos.REQ_PRINTER_STATUS, expected = 1, timeoutMs = 1200)
            if (resp.isEmpty()) null
            else EscPos.drawerOpenFromStatus(resp[0].toInt() and 0xFF)
        } catch (e: Exception) {
            null
        } finally {
            conn.close()
        }
    }

    private suspend fun <T> io(block: () -> T): T = withContext(Dispatchers.IO) { block() }

    // ---- receipt text helpers -------------------------------------------

    private companion object {
        /** 80mm ESC/POS receipt paper at normal Font A is typically 48 columns. */
        const val LINE_WIDTH = 44
        const val ITEM_WIDTH = 16
        const val QUANTITY_WIDTH = 4
        const val UNIT_PRICE_WIDTH = 12
        const val LINE_TOTAL_WIDTH = 12
    }

    /** Render a line as a Unicode bitmap; thermal head fonts/code pages are bypassed. */
    private fun line(text: String, fontSize: Float = 25f, bold: Boolean = false): ByteArray =
        EscPos.rasterText(text, fontSize = fontSize, bold = bold)

    /** Wrap long shop/item text at word boundaries so it cannot run off the paper. */
    private fun ByteArrayOutputStream.writeWrapped(text: String, width: Int, fontSize: Float = 25f) {
        val words = text.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
        var current = StringBuilder()
        for (word in words) {
            if (current.isNotEmpty() && current.length + 1 + word.length > width) {
                write(line(current.toString(), fontSize = fontSize))
                current = StringBuilder()
            }
            if (current.isNotEmpty()) current.append(' ')
            if (word.length <= width) {
                current.append(word)
            } else {
                var offset = 0
                while (offset < word.length) {
                    val part = word.substring(offset, minOf(offset + width, word.length))
                    if (offset + width < word.length) write(line(part, fontSize = fontSize)) else current.append(part)
                    offset += width
                }
            }
        }
        if (current.isNotEmpty()) write(line(current.toString(), fontSize = fontSize))
    }

    private fun ByteArrayOutputStream.writeProductColumn(productName: String) {
        val words = productName.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
        var current = StringBuilder()
        for (word in words) {
            if (current.isNotEmpty() && current.length + 1 + word.length > ITEM_WIDTH) {
                write(line(current.toString()))
                current = StringBuilder()
            }
            if (current.isNotEmpty()) current.append(' ')
            current.append(word)
        }
        if (current.isNotEmpty()) write(line(current.toString()))
    }

    private fun receiptHeaderRow(): ByteArray {
        val header = fitColumn("Mặt hàng", ITEM_WIDTH) +
            fitColumn("SL", QUANTITY_WIDTH) +
            fitColumn("Đơn giá", UNIT_PRICE_WIDTH) +
            fitColumn("Thành tiền", LINE_TOTAL_WIDTH)
        return line(header)
    }

    private fun receiptItemRow(quantity: String, unitPrice: String, lineTotal: String): ByteArray =
        line(" ".repeat(ITEM_WIDTH) +
            fitColumn(quantity, QUANTITY_WIDTH, rightAlign = true) +
            fitColumn(unitPrice, UNIT_PRICE_WIDTH, rightAlign = true) +
            fitColumn(lineTotal, LINE_TOTAL_WIDTH, rightAlign = true))

    private fun fitColumn(value: String, width: Int, rightAlign: Boolean = false): String {
        val safe = if (value.length > width) value.takeLast(width) else value
        return if (rightAlign) safe.padStart(width) else safe.padEnd(width)
    }

    /**
     * Right-align [right] on the same line as [left], padding with spaces so the
     * total column lines up. Truncates the left part if the line would overflow.
     */
    private fun padTotal(left: String, right: String): String {
        val maxLeft = (LINE_WIDTH - right.length - 1).coerceAtLeast(0)
        val leftTrimmed = if (left.length > maxLeft) left.take(maxLeft) else left
        val spaces = (LINE_WIDTH - leftTrimmed.length - right.length).coerceAtLeast(1)
        return leftTrimmed + " ".repeat(spaces) + right
    }

    private fun timeNow(): String = formatTime(System.currentTimeMillis())

    private fun formatTime(ms: Long): String =
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(ms))

    private fun formatPaymentTime(ms: Long): String =
        SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault()).format(Date(ms))
}

