package com.example.sunmipostester.printer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.example.sunmipostester.R
import com.example.sunmipostester.data.OrderEntity
import com.example.sunmipostester.data.OrderItemEntity
import com.example.sunmipostester.data.OrderType
import com.example.sunmipostester.data.OrderWithItems
import com.example.sunmipostester.data.PaymentMethod
import com.example.sunmipostester.data.TextFormat
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Renders invoices from standard Android View templates (R.layout.receipt_template)
 * into high-resolution bitmaps (576px width for 80mm paper) and converts them into
 * safe, chunked ESC/POS raster byte packets that completely prevent printer buffer overflow.
 */
object ReceiptTemplateRenderer {

    const val PRINTER_WIDTH_DOTS = 576 // 80mm printable width at 203 DPI (72mm = 576 dots)
    const val BYTES_PER_ROW = PRINTER_WIDTH_DOTS / 8 // 72 bytes
    const val SAFE_STRIP_HEIGHT = 24 // 24 dots = 1,728 bytes per chunk (< 2KB, safe for 4KB buffer)

    private val GS: Byte = 0x1D

    /**
     * Creates a ConfigurationContext with fixed 240 DPI (1.5x density) so that
     * 384dp XML layout width maps directly to 576 physical dots (80mm paper)
     * identically on any Android terminal or screen resolution.
     */
    private fun getReceiptContext(context: Context): Context {
        val config = android.content.res.Configuration(context.resources.configuration)
        config.densityDpi = 240
        return context.createConfigurationContext(config)
    }

    /**
     * Binds an [OrderWithItems] into [R.layout.receipt_template] and renders it to a [Bitmap].
     */
    fun renderReceiptBitmap(
        context: Context,
        data: OrderWithItems,
        isReprint: Boolean = false,
        tableName: String? = null
    ): Bitmap {
        val receiptContext = getReceiptContext(context)
        val inflater = LayoutInflater.from(receiptContext)
        val root = inflater.inflate(R.layout.receipt_template, null) as LinearLayout

        val order = data.order

        // 1. Header & Logo (respects whatever android:src and size is set in receipt_template.xml)
        val tvReprint = root.findViewById<TextView>(R.id.tvReprintBadge)
        tvReprint.visibility = if (isReprint) View.VISIBLE else View.GONE

        // 2. Metadata
        val tvOrderId = root.findViewById<TextView>(R.id.tvOrderId)
        tvOrderId.text = "Mã đơn: #${order.id}"

        val tvOrderTime = root.findViewById<TextView>(R.id.tvOrderTime)
        tvOrderTime.text = formatPaymentTime(order.createdAtMs)

        val tvOrderType = root.findViewById<TextView>(R.id.tvOrderType)
        val typeLabel = if (order.orderType == OrderType.TAKE_AWAY) "Mang về" else "Tại bàn"
        val serviceLabel = tableName?.takeIf { order.orderType == OrderType.DINE_IN }
            ?.let { "$typeLabel - $it" }
            ?: typeLabel
        tvOrderType.text = "Hình thức: $serviceLabel"

        val tvStaffName = root.findViewById<TextView>(R.id.tvStaffName)
        tvStaffName.text = "Thu ngân: ${order.staffName ?: "Quản lý"}"

        // 3. Line Items
        val itemsContainer = root.findViewById<LinearLayout>(R.id.llItemsContainer)
        itemsContainer.removeAllViews()

        for (item in data.items) {
            val itemView = inflater.inflate(R.layout.item_receipt_row, itemsContainer, false)
            itemView.findViewById<TextView>(R.id.tvItemName).text = item.productName
            itemView.findViewById<TextView>(R.id.tvItemQuantity).text = item.quantity.toString()
            itemView.findViewById<TextView>(R.id.tvItemUnitPrice).text = TextFormat.money(item.unitPrice)
            itemView.findViewById<TextView>(R.id.tvItemLineTotal).text = TextFormat.money(item.lineTotal)
            itemsContainer.addView(itemView)
        }

        // 4. Subtotal & Discount
        val rowSubtotal = root.findViewById<View>(R.id.rowSubtotal)
        val rowDiscount = root.findViewById<View>(R.id.rowDiscount)
        if (order.discountAmount > 0) {
            rowSubtotal.visibility = View.VISIBLE
            root.findViewById<TextView>(R.id.tvSubtotal).text = TextFormat.vnd(order.subtotal)

            rowDiscount.visibility = View.VISIBLE
            val discountCode = order.discountCode?.let { " ($it)" } ?: ""
            root.findViewById<TextView>(R.id.tvDiscountLabel).text = "Giảm giá$discountCode:"
            root.findViewById<TextView>(R.id.tvDiscountAmount).text = "-${TextFormat.vnd(order.discountAmount)}"
        } else {
            rowSubtotal.visibility = View.GONE
            rowDiscount.visibility = View.GONE
        }

        // 5. Total
        root.findViewById<TextView>(R.id.tvTotal).text = TextFormat.vnd(order.total)

        // 6. Payment details
        root.findViewById<TextView>(R.id.tvPaymentMethod).text = order.paymentMethod.label

        val rowCashReceived = root.findViewById<View>(R.id.rowCashReceived)
        val rowChangeAmount = root.findViewById<View>(R.id.rowChangeAmount)
        if (order.paymentMethod == PaymentMethod.CASH) {
            rowCashReceived.visibility = View.VISIBLE
            rowChangeAmount.visibility = View.VISIBLE
            root.findViewById<TextView>(R.id.tvCashReceived).text =
                TextFormat.vnd(order.cashReceived)
            root.findViewById<TextView>(R.id.tvChangeAmount).text =
                TextFormat.vnd(order.changeAmount)
        } else {
            rowCashReceived.visibility = View.GONE
            rowChangeAmount.visibility = View.GONE
        }

        return layoutViewToBitmap(root, PRINTER_WIDTH_DOTS)
    }

    /**
     * Renders a mock test receipt for device testing.
     */
    fun renderTestReceiptBitmap(context: Context): Bitmap {
        val mockOrder = OrderEntity(
            id = 999,
            subtotal = 55000,
            discountAmount = 5000,
            total = 50000,
            paymentMethod = PaymentMethod.CASH,
            cashReceived = 100000,
            changeAmount = 50000,
            status = com.example.sunmipostester.data.OrderStatus.PAID,
            staffName = "Admin POS",
            createdAtMs = System.currentTimeMillis()
        )
        val mockItems = listOf(
            OrderItemEntity(
                id = 1,
                orderId = 999,
                productId = 1,
                productName = "Chè Bưởi Thốt Nốt",
                unitPrice = 30000,
                quantity = 1
            ),
            OrderItemEntity(
                id = 2,
                orderId = 999,
                productId = 2,
                productName = "Milo Đá Bào Đặc Biệt",
                unitPrice = 25000,
                quantity = 1
            )
        )
        return renderReceiptBitmap(
            context = context,
            data = OrderWithItems(mockOrder, mockItems),
            isReprint = false,
            tableName = "Test Printer"
        )
    }

    /**
     * Renders a kitchen preparation slip from [OrderWithItems] using [R.layout.kitchen_ticket_template].
     */
    fun renderKitchenCopyBitmap(
        context: Context,
        data: OrderWithItems,
        tableName: String? = null
    ): Bitmap {
        val receiptContext = getReceiptContext(context)
        val inflater = LayoutInflater.from(receiptContext)
        val root = inflater.inflate(R.layout.kitchen_ticket_template, null) as LinearLayout
        val order = data.order

        val tvType = root.findViewById<TextView>(R.id.tvKitchenOrderType)
        tvType.text = if (order.orderType == OrderType.TAKE_AWAY) "MANG VỀ" else "TẠI CHỖ"

        val tvTable = root.findViewById<TextView>(R.id.tvKitchenTableName)
        if (tableName != null && order.orderType == OrderType.DINE_IN) {
            tvTable.visibility = View.VISIBLE
            tvTable.text = "BÀN: $tableName"
        } else {
            tvTable.visibility = View.GONE
        }

        root.findViewById<TextView>(R.id.tvKitchenOrderId).text = "Order #${order.id}"
        root.findViewById<TextView>(R.id.tvKitchenTime).text = formatPaymentTime(order.createdAtMs)

        val itemsContainer = root.findViewById<LinearLayout>(R.id.llKitchenItems)
        itemsContainer.removeAllViews()

        for (item in data.items) {
            val row = inflater.inflate(R.layout.item_kitchen_row, itemsContainer, false)
            row.findViewById<TextView>(R.id.tvKitchenItemQuantity).text = "${item.quantity}x"
            row.findViewById<TextView>(R.id.tvKitchenItemName).text = item.productName
            itemsContainer.addView(row)
        }

        return layoutViewToBitmap(root, PRINTER_WIDTH_DOTS)
    }

    /**
     * Renders a KOT ticket (from in-progress cart before payment) using [R.layout.kitchen_ticket_template].
     */
    fun renderKitchenTicketBitmap(
        context: Context,
        orderNumber: Int,
        lines: List<com.example.sunmipostester.data.CartLine>,
        tableName: String? = null
    ): Bitmap {
        val receiptContext = getReceiptContext(context)
        val inflater = LayoutInflater.from(receiptContext)
        val root = inflater.inflate(R.layout.kitchen_ticket_template, null) as LinearLayout

        val tvType = root.findViewById<TextView>(R.id.tvKitchenOrderType)
        tvType.text = if (tableName != null) "TẠI BÀN" else "MANG VỀ"

        val tvTable = root.findViewById<TextView>(R.id.tvKitchenTableName)
        if (tableName != null) {
            tvTable.visibility = View.VISIBLE
            tvTable.text = "BÀN: $tableName"
        } else {
            tvTable.visibility = View.GONE
        }

        val tvOrderId = root.findViewById<TextView>(R.id.tvKitchenOrderId)
        if (orderNumber > 0) {
            tvOrderId.text = "Order #$orderNumber"
        } else {
            tvOrderId.text = "Phiếu gọi món"
        }
        root.findViewById<TextView>(R.id.tvKitchenTime).text = formatPaymentTime(System.currentTimeMillis())

        val itemsContainer = root.findViewById<LinearLayout>(R.id.llKitchenItems)
        itemsContainer.removeAllViews()

        for (l in lines) {
            val row = inflater.inflate(R.layout.item_kitchen_row, itemsContainer, false)
            row.findViewById<TextView>(R.id.tvKitchenItemQuantity).text = "${l.quantity}x"
            row.findViewById<TextView>(R.id.tvKitchenItemName).text = l.product.name
            itemsContainer.addView(row)
        }

        return layoutViewToBitmap(root, PRINTER_WIDTH_DOTS)
    }

    /**
     * Measures, layouts, and draws any Android [View] to an ARGB_8888 [Bitmap].
     */
    fun layoutViewToBitmap(view: View, targetWidth: Int): Bitmap {
        view.layoutParams = LinearLayout.LayoutParams(targetWidth, LinearLayout.LayoutParams.WRAP_CONTENT)
        val widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(targetWidth, View.MeasureSpec.EXACTLY)
        val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        view.measure(widthMeasureSpec, heightMeasureSpec)
        val targetHeight = view.measuredHeight.coerceAtLeast(1)
        view.layout(0, 0, targetWidth, targetHeight)

        val bitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        view.draw(canvas)
        return bitmap
    }

    /**
     * Slices a [Bitmap] into horizontal bands of [stripHeight] dots and converts each
     * band into an ESC/POS `GS v 0` raster command payload with Floyd-Steinberg dithering.
     */
    fun bitmapToChunkedEscPos(bitmap: Bitmap, stripHeight: Int = SAFE_STRIP_HEIGHT): List<ByteArray> {
        val width = bitmap.width
        val height = bitmap.height
        val bytesPerRow = width / 8

        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val gray = Array(height) { FloatArray(width) }
        for (y in 0 until height) {
            for (x in 0 until width) {
                val c = pixels[y * width + x]
                val a = Color.alpha(c)
                if (a < 50) {
                    gray[y][x] = 255f
                } else {
                    val r = Color.red(c)
                    val g = Color.green(c)
                    val b = Color.blue(c)
                    gray[y][x] = (r * 299 + g * 587 + b * 114) / 1000f
                }
            }
        }

        val isBlack = BooleanArray(width * height)
        for (y in 0 until height) {
            for (x in 0 until width) {
                val oldVal = gray[y][x]
                val black = oldVal < 165f
                val newVal = if (black) 0f else 255f
                isBlack[y * width + x] = black

                val error = oldVal - newVal
                if (x + 1 < width) gray[y][x + 1] += error * 7f / 16f
                if (y + 1 < height) {
                    if (x - 1 >= 0) gray[y + 1][x - 1] += error * 3f / 16f
                    gray[y + 1][x] += error * 5f / 16f
                    if (x + 1 < width) gray[y + 1][x + 1] += error * 1f / 16f
                }
            }
        }

        val chunks = mutableListOf<ByteArray>()
        var yStart = 0
        while (yStart < height) {
            val curHeight = minOf(stripHeight, height - yStart)
            val chunkBytes = ByteArrayOutputStream(bytesPerRow * curHeight + 8)

            // Header: GS v 0 0 xL xH yL yH
            val xL = (bytesPerRow and 0xFF).toByte()
            val xH = ((bytesPerRow shr 8) and 0xFF).toByte()
            val yL = (curHeight and 0xFF).toByte()
            val yH = ((curHeight shr 8) and 0xFF).toByte()

            chunkBytes.write(byteArrayOf(GS, 'v'.code.toByte(), '0'.code.toByte(), 0, xL, xH, yL, yH))

            for (row in 0 until curHeight) {
                val y = yStart + row
                for (byteIdx in 0 until bytesPerRow) {
                    var byteVal = 0
                    val startBit = byteIdx * 8
                    for (bit in 0 until 8) {
                        val x = startBit + bit
                        if (x < width && isBlack[y * width + x]) {
                            byteVal = byteVal or (0x80 shr bit)
                        }
                    }
                    chunkBytes.write(byteVal)
                }
            }

            chunks.add(chunkBytes.toByteArray())
            yStart += curHeight
        }

        return chunks
    }

    private fun formatPaymentTime(timeMs: Long): String {
        val sdf = SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault())
        return sdf.format(Date(timeMs))
    }
}
