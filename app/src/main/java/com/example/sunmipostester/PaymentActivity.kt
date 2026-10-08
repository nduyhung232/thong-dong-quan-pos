package com.example.sunmipostester

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.appcompat.app.AlertDialog
import com.example.sunmipostester.auth.Permission
import com.example.sunmipostester.auth.SecuredActivity
import com.example.sunmipostester.auth.Session
import com.example.sunmipostester.common.DiscountCalculator
import com.example.sunmipostester.common.DiscountType
import com.example.sunmipostester.data.DiscountCodeEntity
import com.example.sunmipostester.data.OrderStore
import com.example.sunmipostester.data.OrderType
import com.example.sunmipostester.data.OrderWithItems
import com.example.sunmipostester.data.PaymentMethod
import com.example.sunmipostester.data.PosRepository
import com.example.sunmipostester.data.TextFormat
import com.example.sunmipostester.data.MoneyInput
import com.example.sunmipostester.databinding.ActivityPaymentBinding
import com.example.sunmipostester.printer.PrinterProvider
import com.example.sunmipostester.printer.ReceiptPrinter
import kotlinx.coroutines.launch

/**
 * Payment screen.
 *
 * Flow (payment itself is MOCKED — no real gateway):
 *   1. Show total, pick method (cash / transfer / card).
 *   2. For cash, enter amount received and see change.
 *   3. Confirm -> assume success -> PERSIST the order to the database ->
 *      print receipt -> open drawer if CASH -> clear cart.
 *
 * The order is saved before printing so a printer failure never loses the sale.
 *
 * NOTE: Real payment integration (card/QR/bank) is security-sensitive and must
 * follow PCI-DSS / SBV requirements with a dedicated security review.
 */
class PaymentActivity : SecuredActivity() {

    override val requiredPermission = Permission.SELL

    private lateinit var binding: ActivityPaymentBinding
    private lateinit var printers: PrinterProvider
    private lateinit var repo: PosRepository

    /** Sum before discount. */
    private var subtotal = 0
    /** Amount actually due (subtotal - discount). */
    private var dueTotal = 0
    /** The discount code being applied, or null. */
    private var appliedCode: DiscountCodeEntity? = null
    private var orderType: OrderType = OrderType.DINE_IN
    private var tableName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.pay_title)

        repo = PosRepository(this)
        printers = PrinterProvider(this)
        printers.register()
        MoneyInput.attach(binding.cashReceived)
        binding.cashHint100.setOnClickListener { binding.cashReceived.setText("100000") }
        binding.cashHint50.setOnClickListener { binding.cashReceived.setText("50000") }
        orderType = runCatching {
            OrderType.valueOf(intent.getStringExtra(EXTRA_ORDER_TYPE) ?: OrderType.DINE_IN.name)
        }.getOrDefault(OrderType.DINE_IN)
        tableName = intent.getStringExtra(EXTRA_TABLE_NAME)

        subtotal = OrderStore.total()
        dueTotal = subtotal
        binding.payTotal.text = TextFormat.vnd(dueTotal)

        setupMethodSwitching()
        setupCashCalculator()

        binding.btnApplyCode.setOnClickListener {
            if (appliedCode == null) showCodePicker() else removeCode()
        }
        binding.btnConfirm.setOnClickListener { confirmPayment() }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        printers.unregister()
    }

    // ---- Method selection ------------------------------------------------

    private fun setupMethodSwitching() {
        binding.methodGroup.setOnCheckedChangeListener { _, _ ->
            updateCashInputVisibility()
        }
        updateCashInputVisibility()
    }

    private fun updateCashInputVisibility() {
        val cashSelected = selectedMethod() == PaymentMethod.CASH
        binding.cashBox.visibility = if (cashSelected) View.VISIBLE else View.GONE
        if (!cashSelected) {
            binding.cashReceived.clearFocus()
            val inputManager = getSystemService(INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            inputManager.hideSoftInputFromWindow(binding.cashReceived.windowToken, 0)
        } else {
            binding.cashReceived.clearFocus()
        }
    }

    private fun selectedMethod(): PaymentMethod = when (binding.methodGroup.checkedRadioButtonId) {
        R.id.methodTransfer -> PaymentMethod.TRANSFER
        R.id.methodCard -> PaymentMethod.CARD
        else -> PaymentMethod.CASH
    }

    // ---- Cash change calculation ----------------------------------------

    private fun setupCashCalculator() {
        binding.cashReceived.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val change = ((enteredCashReceived() ?: dueTotal) - dueTotal).coerceAtLeast(0)
                binding.changeAmount.text = TextFormat.vnd(change)
            }
        })
    }

    private fun enteredCashReceived(): Int? = MoneyInput.parse(binding.cashReceived.text)

    // ---- Discount code ---------------------------------------------------

    /** Show the codes pulled from the server that are still available to spend. */
    private fun showCodePicker() {
        lifecycleScope.launch {
            val codes = repo.availableDiscountCodes()
            if (codes.isEmpty()) {
                toast(getString(R.string.pay_no_codes_available))
                return@launch
            }
            val labels = codes.map { c ->
                val v = if (c.valueType == DiscountType.PERCENT) "${c.value}%" else TextFormat.vnd(c.value)
                "${c.code} · ${c.campaignName} ($v)"
            }.toTypedArray()

            AlertDialog.Builder(this@PaymentActivity)
                .setTitle(R.string.pay_pick_code)
                .setItems(labels) { _, which -> applyCode(codes[which]) }
                .setNegativeButton(R.string.prod_cancel, null)
                .show()
        }
    }

    private fun applyCode(code: DiscountCodeEntity) {
        // Compute with the SHARED calculator so the figure matches the server.
        val result = DiscountCalculator.compute(
            subtotal = subtotal,
            type = DiscountType.CODE,
            codeValueType = code.valueType,
            codeValue = code.value
        )
        appliedCode = code
        dueTotal = result.total
        binding.payTotal.text = TextFormat.vnd(dueTotal)
        binding.discountInfo.text =
            getString(R.string.pay_code_applied, code.code, TextFormat.vnd(result.discountAmount))
        binding.btnApplyCode.setText(R.string.pay_remove_code)
        refreshChange()
    }

    private fun removeCode() {
        appliedCode = null
        dueTotal = subtotal
        binding.payTotal.text = TextFormat.vnd(dueTotal)
        binding.discountInfo.setText(R.string.pay_no_code)
        binding.btnApplyCode.setText(R.string.pay_apply_code)
        refreshChange()
    }

    private fun refreshChange() {
        val change = ((enteredCashReceived() ?: dueTotal) - dueTotal).coerceAtLeast(0)
        binding.changeAmount.text = TextFormat.vnd(change)
    }

    // ---- Confirm ---------------------------------------------------------

    private fun confirmPayment() {
        val method = selectedMethod()
        val enteredCash = enteredCashReceived()

        if (method == PaymentMethod.CASH && enteredCash != null && enteredCash < dueTotal) {
            toast(getString(R.string.pay_cash_not_enough))
            return
        }

        binding.btnConfirm.isEnabled = false
        val lines = OrderStore.currentLines()
        val received = if (method == PaymentMethod.CASH) enteredCash ?: dueTotal else dueTotal
        val code = appliedCode
        val discountType = if (code != null) DiscountType.CODE else DiscountType.NONE

        val cashier = Session.current
        lifecycleScope.launch {
            // Persist first: the sale must survive a printing failure.
            // Staff identity is snapshotted so the sale is attributable.
            // The discount is recomputed inside the repository with the shared rule.
            val orderId = repo.savePaidOrder(
                lines = lines,
                method = method,
                cashReceived = received,
                staffId = cashier?.id,
                staffName = cashier?.name,
                orderType = orderType,
                discountType = discountType,
                discountInput = 0,
                appliedCode = code
            )
            toast(getString(R.string.pay_success))

            val saved = repo.orderWithItems(orderId)
            if (saved == null) {
                // Should not happen; fail loudly rather than silently.
                toast(getString(R.string.pay_save_error))
                binding.btnConfirm.isEnabled = true
                return@launch
            }
            tableName?.let { repo.deleteTableDraft(it) }
            printReceiptThenFinish(saved)
        }
    }

    /**
     * Print the receipt, open the drawer for cash, clear the cart, then return to
     * the sale screen. Printing failures are surfaced but the sale still completes
     * because the order is already persisted.
     */
    private fun printReceiptThenFinish(saved: OrderWithItems) {
        if (orderType == OrderType.TAKE_AWAY) {
            printers.withLabelPrinter(
                onError = { err ->
                    toast("${errorMessage(err)} · Bỏ qua in tem")
                    printCustomerReceipt(saved)
                },
                onReady = { labelPrinter ->
                    lifecycleScope.launch {
                        toast(labelPrinter.printTakeAwayLabels(saved).message)
                        printCustomerReceipt(saved)
                    }
                }
            )
            return
        }
        printKitchenThenCustomerReceipt(saved)
    }

    private fun printKitchenThenCustomerReceipt(saved: OrderWithItems) {
        printers.withPrinter(
            onError = { err ->
                toast(errorMessage(err))
                completeSale()
            },
            onReady = { printer ->
                lifecycleScope.launch {
                    val receiptTable = if (orderType == OrderType.DINE_IN) tableName else null
                    val kitchenResult = printer.printKitchenCopy(saved, tableName = receiptTable)
                    toast(kitchenResult.message)
                    kotlinx.coroutines.delay(600) // Allow printer hardware cutter to finish cleanly
                    val receiptResult = printer.printReceipt(saved, tableName = receiptTable)
                    toast(receiptResult.message)
                    completeSale()
                }
            }
        )
    }

    private fun printCustomerReceipt(saved: OrderWithItems) {
        printers.withPrinter(
            onError = { err ->
                toast(errorMessage(err))
                completeSale()
            },
            onReady = { printer ->
                lifecycleScope.launch {
                    val receiptTable = if (orderType == OrderType.DINE_IN) tableName else null
                    toast(printer.printReceipt(saved, tableName = receiptTable).message)
                    completeSale()
                }
            }
        )
    }

    private fun completeSale() {
        OrderStore.clear()
        finish() // back to SaleActivity, which refreshes to an empty cart
    }

    // ---- helpers ---------------------------------------------------------

    private fun errorMessage(err: PrinterProvider.Error): String = when (err) {
        PrinterProvider.Error.NO_DEVICE -> getString(R.string.status_usb_no_device)
        PrinterProvider.Error.PERMISSION_DENIED -> getString(R.string.status_usb_permission)
        PrinterProvider.Error.PRINTERS_NOT_CONFIGURED -> "Chưa cấu hình vai trò máy in trong Kiểm tra thiết bị."
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

    companion object {
        const val EXTRA_ORDER_TYPE = "order_type"
        const val EXTRA_TABLE_NAME = "table_name"
    }
}
