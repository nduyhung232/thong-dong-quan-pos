package com.example.sunmipostester

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.example.sunmipostester.auth.Permission
import com.example.sunmipostester.auth.SecuredActivity
import com.example.sunmipostester.auth.Session
import com.example.sunmipostester.data.PosRepository
import com.example.sunmipostester.data.ShiftSummary
import com.example.sunmipostester.data.TextFormat
import com.example.sunmipostester.data.MoneyInput
import com.example.sunmipostester.databinding.ActivityShiftBinding
import com.example.sunmipostester.databinding.DialogCashAmountBinding
import com.example.sunmipostester.manage.ShiftAdapter
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Shift management (mở ca / đóng ca) with cash reconciliation.
 *
 * While a shift is open the screen shows live figures. Closing asks for the
 * physically counted cash and records the variance against the expected amount
 * (openingCash + cash sales), which is how an over/short is detected.
 */
class ShiftActivity : SecuredActivity() {

    override val requiredPermission = Permission.MANAGE_SHIFT

    private lateinit var binding: ActivityShiftBinding
    private lateinit var repo: PosRepository
    private lateinit var adapter: ShiftAdapter

    private var currentSummary: ShiftSummary? = null

    private val dateTime = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShiftBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.shift_title)

        repo = PosRepository(this)
        adapter = ShiftAdapter()
        binding.shiftList.adapter = adapter

        binding.btnShiftAction.setOnClickListener { onShiftAction() }

        loadData()
    }

    override fun onResume() {
        super.onResume()
        loadData() // figures change as sales come in
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    // ---- Data ------------------------------------------------------------

    private fun loadData() {
        lifecycleScope.launch {
            currentSummary = repo.openShiftSummary()
            renderCurrent(currentSummary)
            adapter.submit(repo.recentShifts())
        }
    }

    private fun renderCurrent(summary: ShiftSummary?) {
        if (summary == null) {
            binding.shiftHeader.text = getString(R.string.shift_no_open)
            binding.shiftFigures.visibility = View.GONE
            binding.btnShiftAction.setText(R.string.shift_open_button)
            return
        }

        binding.shiftHeader.text = getString(R.string.shift_current)
        binding.shiftFigures.visibility = View.VISIBLE
        binding.btnShiftAction.setText(R.string.shift_close_button)

        binding.shiftOpenedAt.text =
            "${getString(R.string.shift_opened_at)}: ${dateTime.format(Date(summary.shift.openedAtMs))}"
        binding.shiftOpeningCash.text =
            "${getString(R.string.shift_opening_cash)}: ${TextFormat.vnd(summary.shift.openingCash)}"
        binding.shiftCashSales.text =
            "${getString(R.string.shift_cash_sales)}: ${TextFormat.vnd(summary.cashSales)}"
        binding.shiftCashExpenses.text = "Chi tiền mặt: ${TextFormat.vnd(summary.cashExpenses)}"
        binding.shiftTotalSales.text =
            "${getString(R.string.shift_total_sales)}: ${TextFormat.vnd(summary.totalSales)}"
        binding.shiftOrderCount.text =
            "${getString(R.string.shift_order_count)}: ${summary.orderCount}"
        binding.shiftExpectedCash.text =
            "${getString(R.string.shift_expected_cash)}: ${TextFormat.vnd(summary.expectedCash)}"
    }

    // ---- Open / close ----------------------------------------------------

    private fun onShiftAction() {
        if (currentSummary == null) showOpenShiftDialog() else showCloseShiftDialog()
    }

    private fun showOpenShiftDialog() {
        val dialogBinding = DialogCashAmountBinding.inflate(layoutInflater)
        dialogBinding.amountWrapper.hint = getString(R.string.shift_opening_cash)
        MoneyInput.attach(dialogBinding.inputAmount)

        AlertDialog.Builder(this)
            .setTitle(R.string.shift_dialog_open)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.prod_save) { _, _ ->
                val amount = MoneyInput.parse(dialogBinding.inputAmount.text)
                if (amount == null || amount < 0) {
                    toast(getString(R.string.shift_invalid_amount))
                    return@setPositiveButton
                }
                val user = Session.current
                lifecycleScope.launch {
                    // Record who opened the shift so a cash variance is attributable.
                    val started = repo.startShift(amount, user?.id, user?.name)
                    // Null means a shift was already open (guard against overlap).
                    toast(getString(if (started != null) R.string.shift_opened else R.string.shift_already_open))
                    loadData()
                }
            }
            .setNegativeButton(R.string.prod_cancel, null)
            .show()
    }

    private fun showCloseShiftDialog() {
        val summary = currentSummary ?: return
        CashCountDialog.show(this, summary.expectedCash) { counted, breakdown ->
            val closedBy = Session.current?.id
            lifecycleScope.launch {
                val closed = repo.closeShift(counted, closedBy, breakdown)
                if (closed != null) showCloseResult(closed)
                loadData()
            }
        }
    }

    /** Surface the variance immediately so a discrepancy isn't missed. */
    private fun showCloseResult(closed: ShiftSummary) {
        val diff = closed.difference ?: 0
        val verdict = when {
            diff == 0 -> getString(R.string.shift_diff_ok)
            diff > 0 -> "${getString(R.string.shift_diff_over)} ${TextFormat.vnd(diff)}"
            else -> "${getString(R.string.shift_diff_short)} ${TextFormat.vnd(-diff)}"
        }
        val msg = buildString {
            append("${getString(R.string.shift_expected_cash)}: ${TextFormat.vnd(closed.expectedCash)}\n")
            append("Chi tiền mặt: ${TextFormat.vnd(closed.cashExpenses)}\n")
            append("${getString(R.string.shift_counted_cash)}: ${TextFormat.vnd(closed.shift.countedCash ?: 0)}\n")
            closed.shift.cashBreakdown?.takeIf { it.isNotBlank() }?.let { append("Mệnh giá: $it\n") }
            append("${getString(R.string.shift_difference)}: $verdict")
        }
        AlertDialog.Builder(this)
            .setTitle(R.string.shift_closed)
            .setMessage(msg)
            .setPositiveButton(R.string.ord_close, null)
            .show()
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
