package com.example.sunmipostester.printer

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import com.example.sunmipostester.data.OrderType
import com.example.sunmipostester.data.OrderWithItems
import com.example.sunmipostester.data.TextFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets

/** Sends TSPL label commands over USB to a dedicated label printer. */
class LabelPrinter(private val connectionFactory: () -> PrinterConnection) {

    data class ActionResult(val success: Boolean, val message: String)

    /** Prints one cup label per item count; labels omit the quantity field. */
    suspend fun printTakeAwayLabels(data: OrderWithItems): ActionResult =
        withContext(Dispatchers.IO) {
            val connection = connectionFactory()
            try {
                connection.open()
                connection.write("DIRECTION 1,0\r\n".toByteArray(StandardCharsets.US_ASCII))
                for (item in data.items) {
                    if (item.quantity <= 0) continue
                    val labelBytes = buildLabel(
                        productName = item.productName,
                        unitPrice = item.unitPrice,
                        orderId = data.order.id,
                        orderType = data.order.orderType
                    )
                    repeat(item.quantity) {
                        connection.write(labelBytes)
                        Thread.sleep(80)
                    }
                }
                ActionResult(true, "Đã gửi tem mang về")
            } catch (e: Exception) {
                ActionResult(false, "Lỗi in tem: ${e.message}")
            } finally {
                connection.close()
            }
        }

    /** TSPL BITMAP bytes are sent with white pixels set and black glyph pixels clear. */
    private fun buildLabel(
        productName: String,
        unitPrice: Int,
        orderId: Long,
        orderType: OrderType
    ): ByteArray {
        val width = LABEL_WIDTH_DOTS
        val textWidth = width - HORIZONTAL_PADDING * 2
        val rows = mutableListOf<LabelText>()

        // 1. Tên món (tự động xuống dòng theo bề rộng con tem để không mất chữ)
        rows += wrap(productName, textWidth, NAME_PAINT).map { LabelText(it, NAME_PAINT) }

        // 2. Giá tiền sản phẩm
        rows += LabelText("Giá: ${TextFormat.vnd(unitPrice)}", PRICE_PAINT)

        // 3. Thông tin đơn hàng
        val typeLabel = if (orderType == OrderType.TAKE_AWAY) "Mang về" else "Tại chỗ"
        rows += LabelText("Đơn #$orderId - $typeLabel", DETAIL_PAINT)

        val height = rows.sumOf { row ->
            row.paint.fontMetrics.run { (descent - ascent).toInt() + ROW_GAP }
        } + VERTICAL_PADDING * 2
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        try {
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.WHITE)
            var baseline = VERTICAL_PADDING.toFloat()
            rows.forEach { row ->
                val metrics = row.paint.fontMetrics
                baseline += -metrics.ascent
                canvas.drawText(row.text, HORIZONTAL_PADDING.toFloat(), baseline, row.paint)
                baseline += metrics.descent + ROW_GAP
            }

            val bytesPerRow = width / 8
            val pixels = IntArray(width)
            val raster = ByteArrayOutputStream(bytesPerRow * height)
            for (y in 0 until height) {
                bitmap.getPixels(pixels, 0, width, 0, y, width, 1)
                for (byteIndex in 0 until bytesPerRow) {
                    var value = 0
                    for (bit in 0 until 8) {
                        val color = pixels[byteIndex * 8 + bit]
                        val luminance = (Color.red(color) * 299 + Color.green(color) * 587 + Color.blue(color) * 114) / 1000
                        if (luminance >= 160) value = value or (0x80 shr bit)
                    }
                    raster.write(value)
                }
            }

            val commands = ByteArrayOutputStream()
            commands.write("CLS\r\nBITMAP 16,16,$bytesPerRow,$height,0,".toByteArray(StandardCharsets.US_ASCII))
            commands.write(raster.toByteArray())
            commands.write("\r\nPRINT 1,1\r\n".toByteArray(StandardCharsets.US_ASCII))
            return commands.toByteArray()
        } finally {
            bitmap.recycle()
        }
    }

    private fun wrap(text: String, maxWidth: Int, paint: Paint): List<String> {
        val lines = mutableListOf<String>()
        val paragraphs = text.split('\n')
        for (para in paragraphs) {
            var current = StringBuilder()
            val words = para.trim().split(Regex("\\s+")).filter(String::isNotBlank)
            for (word in words) {
                val candidate = if (current.isEmpty()) word else "$current $word"
                if (current.isNotEmpty() && paint.measureText(candidate) > maxWidth) {
                    lines += current.toString()
                    current = StringBuilder()
                    if (paint.measureText(word) > maxWidth) {
                        for (ch in word) {
                            val cCandidate = if (current.isEmpty()) ch.toString() else "$current$ch"
                            if (paint.measureText(cCandidate) > maxWidth) {
                                lines += current.toString()
                                current = StringBuilder(ch.toString())
                            } else {
                                current.append(ch)
                            }
                        }
                    } else {
                        current.append(word)
                    }
                } else {
                    if (current.isNotEmpty()) current.append(' ')
                    current.append(word)
                }
            }
            if (current.isNotEmpty()) lines += current.toString()
        }
        return lines.ifEmpty { listOf("") }
    }

    private data class LabelText(val text: String, val paint: Paint)

    private companion object {
        // Khổ tem trà sữa/cà phê chuẩn: 40mm x 30mm (320 dots ở 203 DPI, 40 bytes/hàng)
        const val LABEL_WIDTH_DOTS = 320
        const val HORIZONTAL_PADDING = 6
        const val VERTICAL_PADDING = 8
        const val ROW_GAP = 6
        val NAME_PAINT = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 28f
            typeface = Typeface.create("sans-serif", Typeface.BOLD)
        }
        val PRICE_PAINT = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 25f
            typeface = Typeface.create("sans-serif", Typeface.BOLD)
        }
        val DETAIL_PAINT = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 24f
            typeface = Typeface.create("sans-serif", Typeface.NORMAL)
        }
    }
}
