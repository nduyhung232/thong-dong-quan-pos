package com.example.sunmipostester

import android.os.Bundle
import android.app.DatePickerDialog
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.example.sunmipostester.auth.Permission
import com.example.sunmipostester.auth.SecuredActivity
import com.example.sunmipostester.auth.Session
import com.example.sunmipostester.data.OrderEntity
import com.example.sunmipostester.data.OrderStatus
import com.example.sunmipostester.data.OrderWithItems
import com.example.sunmipostester.data.PaymentMethod
import com.example.sunmipostester.data.OrderType
import com.example.sunmipostester.data.PosRepository
import com.example.sunmipostester.data.TextFormat
import com.example.sunmipostester.sync.AutoPushScheduler
import com.example.sunmipostester.databinding.ActivityOrderManagementBinding
import com.example.sunmipostester.manage.OrderAdapter
import com.example.sunmipostester.printer.PrinterProvider
import com.example.sunmipostester.printer.ReceiptPreviewDialog
import kotlinx.coroutines.launch
import java.util.Calendar
import androidx.recyclerview.widget.LinearLayoutManager
import android.content.Intent
import android.graphics.Color
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import com.example.sunmipostester.data.CartLine
import com.example.sunmipostester.data.ProductEntity
import com.example.sunmipostester.data.OrderStore
import com.example.sunmipostester.databinding.DialogOrderDetailBinding

/**
 * Order history: today's revenue summary, the list of recent orders, and per-order
 * actions (view detail, reprint receipt, cancel).
 *
 * Cancelling sets status = CANCELLED rather than deleting the row, so the audit
 * trail stays intact; cancelled orders are excluded from revenue.
 */
class OrderManagementActivity : SecuredActivity() {

    override val requiredPermission = Permission.SELL

    private lateinit var binding: ActivityOrderManagementBinding
    private lateinit var repo: PosRepository
    private lateinit var printers: PrinterProvider
    private lateinit var adapter: OrderAdapter
    private lateinit var listLayoutManager: LinearLayoutManager

    private val selectedDay = Calendar.getInstance()
    private var nextOffset = 0
    private var loadingPage = false
    private var hasMorePages = true
    private var pageGeneration = 0
    private val pageSize = 10
    private val historyDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.ord_title)

        repo = PosRepository(this)
        printers = PrinterProvider(this)
        printers.register()

        adapter = OrderAdapter { order -> showOrderDetail(order) }
        listLayoutManager = LinearLayoutManager(this)
        binding.orderList.layoutManager = listLayoutManager
        binding.orderList.adapter = adapter
        binding.btnHistoryDate.setOnClickListener { chooseHistoryDate() }
        binding.orderList.addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
                if (dy <= 0) return
                val lastVisible = listLayoutManager.findLastVisibleItemPosition()
                if (lastVisible >= adapter.itemCount - 3) loadNextPage()
            }
        })
        setDateButtonLabel()
        resetHistory()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        printers.unregister()
    }

    // ---- Data ------------------------------------------------------------

    private fun resetHistory() {
        pageGeneration++
        nextOffset = 0
        hasMorePages = true
        loadingPage = false
        adapter.submit(emptyList())
        binding.orderEmpty.visibility = View.GONE
        lifecycleScope.launch {
            val (fromMs, toMs) = selectedDayRange()
            val (revenue, count) = repo.revenueSummary(fromMs, toMs)
            binding.todayRevenue.text = TextFormat.vnd(revenue)
            binding.todayCount.text = count.toString()
            loadNextPage()
        }
    }

    private fun loadNextPage() {
        if (loadingPage || !hasMorePages) return
        loadingPage = true
        val generation = pageGeneration
        lifecycleScope.launch {
            try {
                val (fromMs, toMs) = selectedDayRange()
                val page = repo.ordersBetween(fromMs, toMs, pageSize, nextOffset)
                if (generation != pageGeneration) return@launch
                adapter.append(page)
                nextOffset += page.size
                hasMorePages = page.size == pageSize
                binding.orderEmpty.visibility = if (adapter.itemCount == 0) View.VISIBLE else View.GONE
            } finally {
                if (generation == pageGeneration) loadingPage = false
            }
        }
    }

    private fun chooseHistoryDate() {
        DatePickerDialog(
            this,
            { _, year, month, day ->
                selectedDay.set(year, month, day, 0, 0, 0)
                selectedDay.set(Calendar.MILLISECOND, 0)
                setDateButtonLabel()
                resetHistory()
            },
            selectedDay.get(Calendar.YEAR),
            selectedDay.get(Calendar.MONTH),
            selectedDay.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun setDateButtonLabel() {
        val today = Calendar.getInstance()
        val isToday = today.get(Calendar.YEAR) == selectedDay.get(Calendar.YEAR) &&
            today.get(Calendar.DAY_OF_YEAR) == selectedDay.get(Calendar.DAY_OF_YEAR)
        binding.btnHistoryDate.text = if (isToday) "HÔM NAY · ${historyDateFormat.format(selectedDay.time)}"
        else "NGÀY ${historyDateFormat.format(selectedDay.time)} · CHỌN NGÀY KHÁC"
    }

    private fun selectedDayRange(): Pair<Long, Long> {
        val start = selectedDay.clone() as Calendar
        start.set(Calendar.HOUR_OF_DAY, 0)
        start.set(Calendar.MINUTE, 0)
        start.set(Calendar.SECOND, 0)
        start.set(Calendar.MILLISECOND, 0)
        val end = start.timeInMillis + 24L * 60 * 60 * 1000 - 1
        return start.timeInMillis to end
    }

    // ---- Order detail ----------------------------------------------------

    private fun showOrderDetail(order: OrderEntity) {
        lifecycleScope.launch {
            val data = repo.orderWithItems(order.id) ?: return@launch
            val dialogBinding = DialogOrderDetailBinding.inflate(layoutInflater)
            val dialog = AlertDialog.Builder(this@OrderManagementActivity)
                .setView(dialogBinding.root)
                .create()

            val o = data.order
            dialogBinding.tvOrderTitle.text = "${getString(R.string.ord_detail_title)} #${o.id}"
            val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(o.createdAtMs))
            val staffStr = o.staffName?.let { " · Thu ngân: $it" } ?: ""
            dialogBinding.tvOrderSubtitle.text = "$dateStr$staffStr"

            if (o.status == OrderStatus.PAID) {
                dialogBinding.tvOrderStatusBadge.text = getString(R.string.ord_status_paid)
                dialogBinding.tvOrderStatusBadge.setBackgroundColor(Color.parseColor("#D1FAE5"))
                dialogBinding.tvOrderStatusBadge.setTextColor(Color.parseColor("#065F46"))
            } else {
                dialogBinding.tvOrderStatusBadge.text = getString(R.string.ord_status_cancelled)
                dialogBinding.tvOrderStatusBadge.setBackgroundColor(Color.parseColor("#FEE2E2"))
                dialogBinding.tvOrderStatusBadge.setTextColor(Color.parseColor("#991B1B"))
            }

            val itemsSb = StringBuilder()
            data.items.forEachIndexed { idx, item ->
                itemsSb.append("${idx + 1}. ${item.productName}\n")
                itemsSb.append("   ${item.quantity} x ${TextFormat.vnd(item.unitPrice)} = ${TextFormat.vnd(item.lineTotal)}\n")
            }
            dialogBinding.tvOrderItems.text = itemsSb.toString().trimEnd()

            dialogBinding.tvOrderTotal.text = TextFormat.vnd(o.total)
            val paySb = StringBuilder()
            paySb.append("Hình thức: ${o.paymentMethod.label}")
            if (o.paymentMethod == PaymentMethod.CASH) {
                paySb.append(" | Khách đưa: ${TextFormat.vnd(o.cashReceived)}")
                paySb.append(" | Tiền thừa: ${TextFormat.vnd(o.changeAmount)}")
            }
            dialogBinding.tvPaymentDetails.text = paySb.toString()

            var currentOrderType = o.orderType
            var currentPaymentMethod = o.paymentMethod
            var currentData = data

            if (currentOrderType == OrderType.TAKE_AWAY) {
                dialogBinding.rbTakeAway.isChecked = true
            } else {
                dialogBinding.rbDineIn.isChecked = true
            }

            if (currentPaymentMethod == PaymentMethod.TRANSFER) {
                dialogBinding.rbPayTransfer.isChecked = true
            } else {
                dialogBinding.rbPayCash.isChecked = true
            }

            if (o.status == OrderStatus.CANCELLED) {
                dialogBinding.rbDineIn.isEnabled = false
                dialogBinding.rbTakeAway.isEnabled = false
                dialogBinding.rbPayCash.isEnabled = false
                dialogBinding.rbPayTransfer.isEnabled = false
            } else {
                dialogBinding.rgOrderType.setOnCheckedChangeListener { _, checkedId ->
                    val newType = if (checkedId == R.id.rbTakeAway) OrderType.TAKE_AWAY else OrderType.DINE_IN
                    if (newType != currentOrderType) {
                        currentOrderType = newType
                        currentData = currentData.copy(order = currentData.order.copy(orderType = newType))
                        lifecycleScope.launch {
                            repo.updateOrderType(o.id, newType)
                            AutoPushScheduler.triggerImmediatePush(this@OrderManagementActivity)
                            val label = if (newType == OrderType.TAKE_AWAY) "Mang về" else "Ăn tại chỗ"
                            toast("Đã đổi hình thức thành: $label (Đang đồng bộ lên Web...)")
                            resetHistory()
                        }
                    }
                }

                dialogBinding.rgPaymentMethod.setOnCheckedChangeListener { _, checkedId ->
                    val newMethod = if (checkedId == R.id.rbPayTransfer) PaymentMethod.TRANSFER else PaymentMethod.CASH
                    if (newMethod != currentPaymentMethod) {
                        currentPaymentMethod = newMethod
                        lifecycleScope.launch {
                            val updated = repo.updateOrderPaymentMethod(o.id, newMethod)
                            if (updated != null) {
                                currentData = updated.copy(order = updated.order.copy(orderType = currentOrderType))
                            }
                            AutoPushScheduler.triggerImmediatePush(this@OrderManagementActivity)
                            val label = newMethod.label
                            toast("Đã đổi sang: $label (Đã cập nhật tiền ca & đang đồng bộ...)")

                            val updatedPaySb = StringBuilder()
                            updatedPaySb.append("Hình thức: $label")
                            if (newMethod == PaymentMethod.CASH) {
                                updatedPaySb.append(" | Khách đưa: ${TextFormat.vnd(currentData.order.cashReceived)}")
                                updatedPaySb.append(" | Tiền thừa: ${TextFormat.vnd(currentData.order.changeAmount)}")
                            }
                            dialogBinding.tvPaymentDetails.text = updatedPaySb.toString()

                            resetHistory()
                        }
                    }
                }
            }

            // 1. SAO CHÉP ĐƠN HÀNG VÀO GIỎ
            dialogBinding.btnCopyOrder.setOnClickListener {
                dialog.dismiss()
                copyOrderToActiveCart(currentData)
            }

            // 2. IN LẠI HÓA ĐƠN
            dialogBinding.btnReprintOrder.setOnClickListener {
                ReceiptPreviewDialog.show(
                    context = this@OrderManagementActivity,
                    data = currentData,
                    isReprint = true,
                    onPrintConfirmed = { reprint(currentData) }
                )
            }

            // 3. IN LẠI TEM
            dialogBinding.btnReprintLabel.setOnClickListener {
                reprintLabels(currentData)
            }

            // 4. HỦY ĐƠN HÀNG
            if (o.status == OrderStatus.PAID && Session.can(Permission.CANCEL_ORDER)) {
                dialogBinding.btnCancelOrder.visibility = View.VISIBLE
                dialogBinding.btnCancelOrder.setOnClickListener {
                    dialog.dismiss()
                    confirmCancel(o)
                }
            } else {
                dialogBinding.btnCancelOrder.visibility = View.GONE
            }

            // 4. ĐÓNG DIALOG
            dialogBinding.btnCloseDialog.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }
    }

    private fun copyOrderToActiveCart(data: OrderWithItems) {
        lifecycleScope.launch {
            val allLiveProducts = repo.allProducts().associateBy { it.id }
            val linesToCopy = data.items.map { item ->
                val live = allLiveProducts[item.productId]
                val prod = live?.copy(price = item.unitPrice) ?: ProductEntity(
                    id = item.productId,
                    syncId = item.productSyncId ?: UUID.randomUUID().toString(),
                    name = item.productName,
                    price = item.unitPrice,
                    category = "Món sao chép"
                )
                CartLine(
                    product = prod,
                    quantity = item.quantity
                )
            }

            OrderStore.replaceWith(linesToCopy)
            val totalQty = linesToCopy.sumOf { it.quantity }
            toast("Đã sao chép ${linesToCopy.size} món ($totalQty phần) vào màn hình order")

            val intent = Intent(this@OrderManagementActivity, SaleActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
            finish()
        }
    }

    /** Human-readable order breakdown for the detail dialog. */
    private fun buildDetailText(data: OrderWithItems): String {
        val o = data.order
        val sb = StringBuilder()
        for (item in data.items) {
            sb.append("${item.quantity} x ${item.productName}")
                .append("   ${TextFormat.vnd(item.lineTotal)}\n")
        }
        sb.append("\n")
        sb.append("Tổng cộng: ${TextFormat.vnd(o.total)}\n")
        sb.append("Hình thức: ${o.paymentMethod.label}\n")
        o.staffName?.let { sb.append("Thu ngân: $it\n") }
        if (o.paymentMethod == PaymentMethod.CASH) {
            sb.append("Khách đưa: ${TextFormat.vnd(o.cashReceived)}\n")
            sb.append("Tiền thừa: ${TextFormat.vnd(o.changeAmount)}\n")
        }
        sb.append("Trạng thái: ")
        sb.append(
            getString(
                if (o.status == OrderStatus.PAID) R.string.ord_status_paid
                else R.string.ord_status_cancelled
            )
        )
        return sb.toString()
    }

    // ---- Actions ---------------------------------------------------------

    private fun reprint(data: OrderWithItems) {
        printers.withPrinter(
            onError = { err -> toast(errorMessage(err)) },
            onReady = { printer ->
                lifecycleScope.launch {
                    toast(printer.printReceipt(data, isReprint = true).message)
                }
            }
        )
    }

    private fun reprintLabels(data: OrderWithItems) {
        if (data.items.isEmpty()) {
            toast("Đơn hàng không có món ăn để in tem")
            return
        }
        printers.withLabelPrinter(
            onError = { err -> toast(errorMessage(err)) },
            onReady = { labelPrinter ->
                lifecycleScope.launch {
                    val result = labelPrinter.printTakeAwayLabels(data, forceTakeAway = (data.order.orderType == OrderType.TAKE_AWAY))
                    toast(result.message)
                }
            }
        )
    }

    private fun confirmCancel(order: OrderEntity) {
        AlertDialog.Builder(this)
            .setMessage(R.string.ord_cancel_confirm)
            .setPositiveButton(R.string.ord_cancel_order) { _, _ ->
                val approvedBy = Session.current?.id
                lifecycleScope.launch {
                    // Records which manager authorised the void.
                    repo.cancelOrder(order.id, approvedBy)
                    toast(getString(R.string.ord_cancelled))
                    resetHistory()
                }
            }
            .setNegativeButton(R.string.prod_cancel, null)
            .show()
    }

    // ---- helpers ---------------------------------------------------------

    private fun errorMessage(err: PrinterProvider.Error): String = when (err) {
        PrinterProvider.Error.NO_DEVICE -> getString(R.string.status_usb_no_device)
        PrinterProvider.Error.PERMISSION_DENIED -> getString(R.string.status_usb_permission)
        PrinterProvider.Error.PRINTERS_NOT_CONFIGURED -> "Chưa cấu hình vai trò máy in trong Kiểm tra thiết bị."
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
