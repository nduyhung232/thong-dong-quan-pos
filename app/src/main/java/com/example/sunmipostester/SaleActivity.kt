package com.example.sunmipostester

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sunmipostester.auth.Session
import com.example.sunmipostester.auth.PinHasher
import com.example.sunmipostester.common.DiscountCalculator
import com.example.sunmipostester.common.DiscountType
import com.example.sunmipostester.data.*
import com.example.sunmipostester.data.MoneyInput
import com.example.sunmipostester.databinding.ActivitySaleBinding
import com.example.sunmipostester.databinding.DialogCheckoutBinding
import com.example.sunmipostester.databinding.DialogCashAmountBinding
import com.example.sunmipostester.databinding.DialogOpenShiftBinding
import com.example.sunmipostester.databinding.DialogSyncConfigBinding
import com.example.sunmipostester.printer.PrinterProvider
import com.example.sunmipostester.printer.PrinterMappingDialog
import com.example.sunmipostester.sale.CartAdapter
import com.example.sunmipostester.sale.CategoryAdapter
import com.example.sunmipostester.sale.ProductAdapter
import com.example.sunmipostester.sync.SyncConfig
import com.example.sunmipostester.sync.SyncManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SaleActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySaleBinding
    private lateinit var repo: PosRepository
    private lateinit var syncConfig: SyncConfig
    private lateinit var printerProvider: PrinterProvider

    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var productAdapter: ProductAdapter
    private lateinit var cartAdapter: CartAdapter

    private var allProducts: List<ProductEntity> = emptyList()
    private var selectedCategory: String? = null
    private var openShiftDialog: AlertDialog? = null

    private val timeFormat = SimpleDateFormat("HH:mm dd/MM", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySaleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repo = PosRepository(this)
        syncConfig = SyncConfig(this)
        printerProvider = PrinterProvider(this)
        printerProvider.register()
        setupRecyclerViews()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        checkActiveShiftAndRefresh()
    }

    override fun onDestroy() {
        super.onDestroy()
        printerProvider.unregister()
        openShiftDialog?.dismiss()
    }

    private fun setupRecyclerViews() {
        categoryAdapter = CategoryAdapter { category ->
            selectedCategory = if (category == "Tất cả") null else category
            filterProducts()
        }
        binding.categoryList.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.categoryList.adapter = categoryAdapter

        productAdapter = ProductAdapter { product ->
            OrderStore.add(product)
            refreshCart()
        }
        binding.productGrid.layoutManager = GridLayoutManager(this, 3)
        binding.productGrid.adapter = productAdapter

        cartAdapter = CartAdapter(
            onPlus = { line ->
                OrderStore.increaseLine(line.lineId)
                refreshCart()
            },
            onMinus = { line ->
                OrderStore.decreaseLine(line.lineId)
                refreshCart()
            },
            onItemClick = { line ->
                showToppingDialog(line)
            }
        )
        binding.cartList.layoutManager = LinearLayoutManager(this)
        binding.cartList.adapter = cartAdapter
    }

    private fun setupListeners() {
        binding.btnSyncConfig.setOnClickListener {
            showSyncMenu()
        }

        binding.btnCloseShift.setOnClickListener {
            showCloseShiftDialog()
        }

        binding.btnHistory.setOnClickListener {
            startActivity(android.content.Intent(this, OrderManagementActivity::class.java))
        }

        binding.btnExpense.setOnClickListener { showExpenseDialog() }

        binding.btnPayOrder.setOnClickListener {
            if (OrderStore.isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn món trước", Toast.LENGTH_SHORT).show()
            } else {
                showCheckoutDialog()
            }
        }
    }

    private fun checkActiveShiftAndRefresh() {
        lifecycleScope.launch {
            val summary = repo.openShiftSummary()
            if (summary == null) {
                binding.shiftStatusText.text = "Ca làm: Chưa mở ca"
                binding.shiftStatusText.setTextColor(Color.parseColor("#EF4444"))
                binding.btnCloseShift.visibility = View.GONE
                showMandatoryOpenShiftDialog()
            } else {
                openShiftDialog?.dismiss()
                openShiftDialog = null
                binding.btnCloseShift.visibility = View.VISIBLE
                val staffName = summary.shift.staffName ?: "Nhân viên"
                val timeStr = timeFormat.format(Date(summary.shift.openedAtMs))
                binding.shiftStatusText.text = "NV: $staffName (Mở lúc: $timeStr)"
                binding.shiftStatusText.setTextColor(Color.parseColor("#10B981"))

                if (Session.current == null || Session.current?.id != summary.shift.staffId) {
                    val staffList = repo.allStaff()
                    val staff = staffList.firstOrNull { it.id == summary.shift.staffId }
                    if (staff != null) {
                        Session.signIn(staff)
                    }
                }
                loadProducts()
                refreshCart()
            }
        }
    }

    private fun showMandatoryOpenShiftDialog() {
        if (openShiftDialog?.isShowing == true) return

        lifecycleScope.launch {
            var staffList = repo.allStaff()
            if (staffList.isEmpty()) {
                repo.seedStaffIfEmpty()
                staffList = repo.allStaff()
            }

            val dialogBinding = DialogOpenShiftBinding.inflate(layoutInflater)
            MoneyInput.attach(dialogBinding.inputOpeningCash)
            val staffNames = staffList.map { "${it.name} (${it.role})" }
            val adapter = ArrayAdapter(this@SaleActivity, android.R.layout.simple_spinner_dropdown_item, staffNames)
            dialogBinding.staffSpinner.adapter = adapter

            val dialog = AlertDialog.Builder(this@SaleActivity)
                .setTitle("MỞ CA BÁN HÀNG")
                .setView(dialogBinding.root)
                .setCancelable(false)
                .setPositiveButton("BẮT ĐẦU CA", null)
                .create()

            dialog.setOnShowListener {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                    val selectedIndex = dialogBinding.staffSpinner.selectedItemPosition
                    if (selectedIndex < 0 || selectedIndex >= staffList.size) {
                        Toast.makeText(this@SaleActivity, "Vui lòng chọn nhân viên", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    val selectedStaff = staffList[selectedIndex]
                    val floatAmount = MoneyInput.parse(dialogBinding.inputOpeningCash.text) ?: 0

                    lifecycleScope.launch {
                        val started = repo.startShift(floatAmount, selectedStaff.id, selectedStaff.name)
                        if (started != null) {
                            Session.signIn(selectedStaff)
                            Toast.makeText(this@SaleActivity, "Mở ca thành công: ${selectedStaff.name}", Toast.LENGTH_SHORT).show()
                            dialog.dismiss()
                            checkActiveShiftAndRefresh()
                        } else {
                            Toast.makeText(this@SaleActivity, "Ca làm việc đã được mở trước đó", Toast.LENGTH_SHORT).show()
                            dialog.dismiss()
                            checkActiveShiftAndRefresh()
                        }
                    }
                }
            }

            openShiftDialog = dialog
            dialog.show()
        }
    }

    private fun showCloseShiftDialog() {
        lifecycleScope.launch {
            val summary = repo.openShiftSummary() ?: return@launch

            val infoMsg = """
                Nhân viên: ${summary.shift.staffName ?: "Chưa rõ"}
                Tiền đầu ca: ${TextFormat.vnd(summary.shift.openingCash)}
                Thu tiền mặt: ${TextFormat.vnd(summary.cashSales)}
                Thu chuyển khoản: ${TextFormat.vnd(summary.transferSales)}
                Tổng chi tiền mặt: ${TextFormat.vnd(summary.cashExpenses)}
                Tổng tiền mặt dự kiến: ${TextFormat.vnd(summary.expectedCash)}
                Tổng doanh thu: ${TextFormat.vnd(summary.totalSales)} (${summary.orderCount} đơn)
            """.trimIndent()

            AlertDialog.Builder(this@SaleActivity)
                .setTitle("Đóng ca & Chốt két")
                .setMessage(infoMsg)
                .setPositiveButton("ĐẾM TIỀN") { _, _ ->
                    CashCountDialog.show(this@SaleActivity, summary.expectedCash) { countedCash, breakdown ->
                        lifecycleScope.launch {
                            val closed = repo.closeShift(countedCash, Session.current?.id, breakdown)
                            Session.signOut()
                            if (closed != null) {
                                val diff = closed.difference ?: 0
                                val diffMsg = when {
                                    diff > 0 -> "(Thừa: +${TextFormat.vnd(diff)})"
                                    diff < 0 -> "(Thiếu: -${TextFormat.vnd(-diff)})"
                                    else -> "(Khớp hoàn toàn)"
                                }
                                AlertDialog.Builder(this@SaleActivity)
                                    .setTitle("Đã đóng ca thành công")
                                    .setMessage(buildString {
                                        append("Doanh thu tiền mặt: ${TextFormat.vnd(closed.cashSales)}\n")
                                        append("Thu chuyển khoản: ${TextFormat.vnd(closed.transferSales)}\n")
                                        append("Chi tiền mặt: ${TextFormat.vnd(closed.cashExpenses)}\n")
                                        append("Tổng doanh thu: ${TextFormat.vnd(closed.totalSales)}\n")
                                        append("Thực nộp tiền két: ${TextFormat.vnd(closed.shift.countedCash ?: 0)} $diffMsg\n")
                                        closed.shift.cashBreakdown?.takeIf { it.isNotBlank() }?.let { append("Mệnh giá: $it") }
                                    })
                                    .setPositiveButton("OK") { _, _ -> checkActiveShiftAndRefresh() }
                                    .setCancelable(false)
                                    .show()
                            } else {
                                checkActiveShiftAndRefresh()
                            }
                        }
                    }
                }
                .setNegativeButton("HỦY", null)
                .show()
        }
    }

    private fun loadProducts() {
        lifecycleScope.launch {
            allProducts = repo.allProducts()
            val categories = listOf("Tất cả") + allProducts.map { it.category.ifBlank { "Khác" } }.distinct()
            categoryAdapter.submit(categories)
            filterProducts()
        }
    }

    private fun filterProducts() {
        val filtered = if (selectedCategory == null) {
            allProducts
        } else {
            allProducts.filter { it.category == selectedCategory || (selectedCategory == "Khác" && it.category.isBlank()) }
        }
        productAdapter.submit(filtered)
    }

    private fun refreshCart() {
        val lines = OrderStore.currentLines()
        cartAdapter.submit(lines)
        binding.cartTotal.text = TextFormat.vnd(OrderStore.total())
        binding.cartEmpty.visibility = if (lines.isEmpty()) View.VISIBLE else View.GONE
        binding.btnPayOrder.isEnabled = lines.isNotEmpty()
    }

    private fun showCheckoutDialog() {
        val dialogBinding = DialogCheckoutBinding.inflate(layoutInflater)
        val orderLines = OrderStore.currentLines()
        val orderSubtotal = orderLines.sumOf { it.lineTotal }
        var discountValue = 0
        var payable = orderSubtotal

        dialogBinding.checkoutSubtotal.text = "Tạm tính: ${TextFormat.vnd(orderSubtotal)}"
        dialogBinding.checkoutTotal.text = "Tổng thanh toán: ${TextFormat.vnd(payable)}"
        MoneyInput.attach(dialogBinding.cashReceivedInput)
        MoneyInput.attach(dialogBinding.discountInput)
        dialogBinding.cashHint100.setOnClickListener { dialogBinding.cashReceivedInput.setText("100000") }
        dialogBinding.cashHint50.setOnClickListener { dialogBinding.cashReceivedInput.setText("50000") }

        fun selectedDiscountType(): DiscountType =
            if (dialogBinding.discountTypeGroup.checkedRadioButtonId == R.id.discountPercentType) {
                DiscountType.PERCENT
            } else DiscountType.AMOUNT

        fun refreshPreview() {
            val enteredValue = MoneyInput.parse(dialogBinding.discountInput.text) ?: 0
            val type = if (enteredValue > 0) selectedDiscountType() else DiscountType.NONE
            val result = DiscountCalculator.compute(orderSubtotal, type, enteredValue)
            payable = result.total
            discountValue = if (enteredValue > 0) enteredValue else 0
            dialogBinding.discountResult.text =
                "Tiền giảm: ${TextFormat.vnd(result.discountAmount)} · Cần trả: ${TextFormat.vnd(result.total)}"
            dialogBinding.checkoutTotal.text = "Tổng thanh toán: ${TextFormat.vnd(result.total)}"
            val tendered = MoneyInput.parse(dialogBinding.cashReceivedInput.text) ?: 0
            dialogBinding.changeLabel.text = "Tiền thừa: ${TextFormat.vnd((tendered - result.total).coerceAtLeast(0))}"
        }

        fun refreshPaymentVisibility() {
            val cashSelected = dialogBinding.paymentMethodGroup.checkedRadioButtonId == R.id.paymentCash
            dialogBinding.cashControls.visibility = if (cashSelected) View.VISIBLE else View.GONE
            if (!cashSelected) {
                dialogBinding.cashReceivedInput.clearFocus()
                dialogBinding.root.requestFocus()
                val inputManager = getSystemService(INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                inputManager.hideSoftInputFromWindow(dialogBinding.cashReceivedInput.windowToken, 0)
            } else {
                dialogBinding.cashReceivedInput.clearFocus()
            }
        }

        dialogBinding.paymentMethodGroup.setOnCheckedChangeListener { _, _ -> refreshPaymentVisibility() }
        dialogBinding.discountTypeGroup.setOnCheckedChangeListener { _, _ -> refreshPreview() }
        dialogBinding.discountInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = refreshPreview()
            override fun afterTextChanged(s: Editable?) = Unit
        })
        dialogBinding.cashReceivedInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = refreshPreview()
            override fun afterTextChanged(s: Editable?) = Unit
        })

        refreshPaymentVisibility()
        val checkoutDialog = AlertDialog.Builder(this)
            .setTitle("Xác nhận đơn hàng")
            .setView(dialogBinding.root)
            .setPositiveButton("THANH TOÁN", null)
            .setNegativeButton("HỦY", null)
            .create()
        checkoutDialog.setOnShowListener {
            checkoutDialog.window?.setSoftInputMode(
                android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
            )
            dialogBinding.root.requestFocus()
            checkoutDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val paymentMethod = if (
                    dialogBinding.paymentMethodGroup.checkedRadioButtonId == R.id.paymentTransfer
                ) PaymentMethod.TRANSFER else PaymentMethod.CASH
                val enteredCashAmount = MoneyInput.parse(dialogBinding.cashReceivedInput.text)
                val cashAmount = enteredCashAmount ?: payable
                if (discountValue > 0 && selectedDiscountType() == DiscountType.PERCENT &&
                    (discountValue !in 0..DiscountCalculator.MAX_PERCENT)
                ) {
                    Toast.makeText(this, "Phần trăm giảm phải từ 0 đến 100", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (paymentMethod == PaymentMethod.CASH && enteredCashAmount != null && cashAmount < payable) {
                    Toast.makeText(this, R.string.pay_cash_not_enough, Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val type = if (dialogBinding.orderTypeGroup.checkedRadioButtonId == R.id.typeTakeAway) {
                    OrderType.TAKE_AWAY
                } else OrderType.DINE_IN
                val discountType = if (discountValue > 0) selectedDiscountType() else DiscountType.NONE
                val finalDiscountInput = if (discountValue > 0) discountValue else 0
                val finalPayable = payable
                if (paymentMethod == PaymentMethod.TRANSFER) {
                    AlertDialog.Builder(this)
                        .setTitle("Xác nhận chuyển khoản")
                        .setMessage("Đã nhận đủ ${TextFormat.vnd(finalPayable)} chưa?")
                        .setPositiveButton("ĐÃ NHẬN ĐỦ · XÁC NHẬN") { _, _ ->
                            checkoutDialog.dismiss()
                            persistAndPrintOrder(
                                orderLines, type, paymentMethod, finalPayable,
                                discountType, finalDiscountInput
                            )
                        }
                        .setNegativeButton("CHƯA", null)
                        .show()
                } else {
                    checkoutDialog.dismiss()
                    persistAndPrintOrder(
                        orderLines, type, paymentMethod, cashAmount,
                        discountType, finalDiscountInput
                    )
                }
            }
        }
        checkoutDialog.show()
    }

    private fun showToppingDialog(line: CartLine) {
        lifecycleScope.launch {
            var availableToppings = repo.getToppingsForProduct(line.product.syncId)
            if (availableToppings.isEmpty()) {
                availableToppings = repo.getAllActiveToppings()
            }
            if (availableToppings.isEmpty()) {
                Toast.makeText(this@SaleActivity, "Chưa có topping nào trên hệ thống", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val selectedIds = line.selectedToppings.map { it.syncId }.toMutableSet()
            val labels = availableToppings.map {
                "${it.name} (+${TextFormat.vnd(it.price)})"
            }.toTypedArray()
            val checkedStates = availableToppings.map { selectedIds.contains(it.syncId) }.toBooleanArray()

            AlertDialog.Builder(this@SaleActivity)
                .setTitle("Thêm Topping: ${line.product.name}")
                .setMultiChoiceItems(labels, checkedStates) { _, which, isChecked ->
                    val item = availableToppings[which]
                    if (isChecked) selectedIds.add(item.syncId)
                    else selectedIds.remove(item.syncId)
                }
                .setPositiveButton("XÁC NHẬN") { _, _ ->
                    val chosen = availableToppings.filter { selectedIds.contains(it.syncId) }
                    OrderStore.updateToppings(line.lineId, chosen)
                    refreshCart()
                }
                .setNegativeButton("HỦY", null)
                .show()
        }
    }

    private fun persistAndPrintOrder(
        lines: List<CartLine>,
        orderType: OrderType,
        method: PaymentMethod,
        cashReceived: Int,
        discountType: DiscountType,
        discountInput: Int
    ) {
        binding.btnPayOrder.isEnabled = false
        val staff = Session.current
        lifecycleScope.launch {
            try {
                val orderId = repo.savePaidOrder(
                    lines = lines,
                    method = method,
                    cashReceived = cashReceived,
                    staffId = staff?.id,
                    staffName = staff?.name,
                    orderType = orderType,
                    discountType = discountType,
                    discountInput = discountInput
                )
                val saved = repo.orderWithItems(orderId)
                if (saved == null) {
                    Toast.makeText(this@SaleActivity, R.string.pay_save_error, Toast.LENGTH_SHORT).show()
                    binding.btnPayOrder.isEnabled = true
                    return@launch
                }
                printPaidOrder(saved, orderType)
            } catch (error: Exception) {
                Toast.makeText(this@SaleActivity, "Lỗi thanh toán: ${error.message}", Toast.LENGTH_LONG).show()
                binding.btnPayOrder.isEnabled = true
            }
        }
    }

    private fun printPaidOrder(saved: OrderWithItems, orderType: OrderType) {
        if (orderType == OrderType.TAKE_AWAY) {
            printerProvider.withLabelPrinter(
                onError = { error ->
                    Toast.makeText(this, "${printerError(error)} · Bỏ qua in tem", Toast.LENGTH_LONG).show()
                    printReceipt(saved)
                },
                onReady = { labelPrinter ->
                    lifecycleScope.launch {
                        Toast.makeText(this@SaleActivity, labelPrinter.printTakeAwayLabels(saved).message, Toast.LENGTH_LONG).show()
                        printReceipt(saved)
                    }
                }
            )
        } else {
            printKitchenAndReceipt(saved)
        }
    }

    /** Always send a prep slip to the receipt printer after payment, then the customer copy. */
    private fun printKitchenAndReceipt(saved: OrderWithItems) {
        printerProvider.withPrinter(
            onError = { error ->
                Toast.makeText(this, printerError(error), Toast.LENGTH_LONG).show()
                finishPaidOrder()
            },
            onReady = { printer ->
                lifecycleScope.launch {
                    val kitchenResult = printer.printKitchenCopy(saved)
                    Toast.makeText(this@SaleActivity, kitchenResult.message, Toast.LENGTH_LONG).show()
                    kotlinx.coroutines.delay(600)
                    val receiptResult = printer.printReceipt(saved)
                    Toast.makeText(this@SaleActivity, receiptResult.message, Toast.LENGTH_LONG).show()
                    finishPaidOrder()
                }
            }
        )
    }

    private fun printReceipt(saved: OrderWithItems) {
        printerProvider.withPrinter(
            onError = { error ->
                Toast.makeText(this, printerError(error), Toast.LENGTH_LONG).show()
                finishPaidOrder()
            },
            onReady = { printer ->
                lifecycleScope.launch {
                    Toast.makeText(this@SaleActivity, printer.printReceipt(saved).message, Toast.LENGTH_LONG).show()
                    finishPaidOrder()
                }
            }
        )
    }

    private fun printerError(error: PrinterProvider.Error): String = when (error) {
        PrinterProvider.Error.NO_DEVICE -> getString(R.string.status_usb_no_device)
        PrinterProvider.Error.PERMISSION_DENIED -> getString(R.string.status_usb_permission)
        PrinterProvider.Error.PRINTERS_NOT_CONFIGURED -> "Chưa cấu hình vai trò máy in. Vào Kiểm tra thiết bị để gán máy in hóa đơn và máy in tem."
    }

    private fun finishPaidOrder() {
        OrderStore.clear()
        refreshCart()
    }

    private fun performPullSync() {
        if (!syncConfig.isConfigured) {
            Toast.makeText(this, "Vui lòng bấm nút Cấu hình để nhập URL Server trước khi PULL", Toast.LENGTH_LONG).show()
            showSyncConfigDialog()
            return
        }

        binding.btnPull.isEnabled = false
        binding.btnPull.text = "ĐANG PULL..."
        lifecycleScope.launch {
            try {
                val syncMgr = SyncManager(repo, syncConfig)
                val res = syncMgr.pullOnly()
                Toast.makeText(this@SaleActivity, "PULL thành công: Nhận ${res.staff} NV, ${res.products} món", Toast.LENGTH_LONG).show()
                loadProducts()
            } catch (e: Exception) {
                Toast.makeText(this@SaleActivity, "PULL thất bại: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                binding.btnPull.isEnabled = true
                binding.btnPull.text = "⬇ PULL"
            }
        }
    }

    private fun performPushSync() {
        if (!syncConfig.isConfigured) {
            Toast.makeText(this, "Vui lòng bấm nút Cấu hình để nhập URL Server trước khi PUSH", Toast.LENGTH_LONG).show()
            showSyncConfigDialog()
            return
        }

        binding.btnPush.isEnabled = false
        binding.btnPush.text = "ĐANG PUSH..."
        lifecycleScope.launch {
            try {
                val syncMgr = SyncManager(repo, syncConfig)
                val res = syncMgr.pushOnly()
                val rejMsg = if (res.rejected.isNotEmpty()) " (${res.rejected.size} từ chối)" else ""
                Toast.makeText(this@SaleActivity, "PUSH thành công: Đã gửi ${res.orders} đơn, ${res.shifts} ca$rejMsg", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(this@SaleActivity, "PUSH thất bại: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                binding.btnPush.isEnabled = true
                binding.btnPush.text = "⬆ PUSH"
            }
        }
    }

    private fun showSyncConfigDialog() {
        val dialogBinding = DialogSyncConfigBinding.inflate(layoutInflater)
        dialogBinding.inputServerUrl.setText(syncConfig.baseUrl)
        dialogBinding.inputDeviceToken.setText(syncConfig.token)

        AlertDialog.Builder(this)
            .setTitle("CẤU HÌNH ĐỒNG BỘ SERVER")
            .setView(dialogBinding.root)
            .setPositiveButton("LƯU") { _, _ ->
                val url = dialogBinding.inputServerUrl.text?.toString()?.trim().orEmpty()
                val token = dialogBinding.inputDeviceToken.text?.toString()?.trim().orEmpty()
                if (url.isBlank()) {
                    Toast.makeText(this, "Vui lòng nhập URL máy chủ", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                syncConfig.save(url, token)
                com.example.sunmipostester.sync.AutoPushScheduler.scheduleNext(this, forceReplace = true)
                Toast.makeText(this, "Đã lưu cấu hình đồng bộ", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("HỦY", null)
            .show()
    }

    private fun showSyncMenu() {
        AlertDialog.Builder(this)
            .setTitle("CẤU HÌNH")
            .setItems(arrayOf("Cấu hình máy in hóa đơn / máy in tem", "Cấu hình máy chủ", "PULL dữ liệu", "PUSH đơn/ca")) { _, which ->
                when (which) {
                    0 -> PrinterMappingDialog.show(this, printerProvider)
                    1 -> showSyncConfigDialog()
                    2 -> performPullSync()
                    3 -> performPushSync()
                }
            }
            .setNegativeButton("ĐÓNG", null)
            .show()
    }

    private fun showExpenseDialog() {
        val content = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(24, 8, 24, 0)
        }
        val description = com.google.android.material.textfield.TextInputEditText(this).apply {
            hint = "Nội dung chi"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            minLines = 1
            maxLines = 3
        }
        val amount = com.google.android.material.textfield.TextInputEditText(this).apply {
            hint = "Số tiền chi (đ)"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setSingleLine(true)
        }
        MoneyInput.attach(amount)
        content.addView(description)
        content.addView(amount)
        val dialog = AlertDialog.Builder(this)
            .setTitle("GHI NHẬN KHOẢN CHI")
            .setMessage("Khoản chi sẽ được trừ khỏi tiền két dự kiến và lưu vào ca đang mở.")
            .setView(content)
            .setPositiveButton("XÁC NHẬN CHI", null)
            .setNegativeButton("HỦY", null)
            .create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val reason = description.text?.toString()?.trim().orEmpty()
                val value = MoneyInput.parse(amount.text) ?: 0
                if (reason.isBlank() || value <= 0) {
                    Toast.makeText(this, "Nhập nội dung và số tiền chi hợp lệ", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
                lifecycleScope.launch {
                    try {
                        val user = Session.current
                        val expense = repo.recordCashExpense(reason, value, user?.id, user?.name)
                        dialog.dismiss()
                        Toast.makeText(this@SaleActivity, "Đã ghi khoản chi ${TextFormat.vnd(expense.amount)}", Toast.LENGTH_LONG).show()
                        printerProvider.withPrinter(
                            onError = { err ->
                                Toast.makeText(this@SaleActivity, "Đã lưu khoản chi nhưng không mở được két: ${printerError(err)}", Toast.LENGTH_LONG).show()
                            },
                            onReady = { printer ->
                                lifecycleScope.launch {
                                    val result = printer.openDrawer()
                                    if (!result.success) Toast.makeText(this@SaleActivity, result.message, Toast.LENGTH_LONG).show()
                                }
                            }
                        )
                    } catch (error: Exception) {
                        Toast.makeText(this@SaleActivity, error.message ?: "Không ghi được khoản chi", Toast.LENGTH_LONG).show()
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
                    }
                }
            }
        }
        dialog.show()
    }
}
