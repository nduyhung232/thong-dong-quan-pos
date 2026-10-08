package com.example.sunmipostester

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.example.sunmipostester.auth.Permission
import com.example.sunmipostester.auth.SecuredActivity
import com.example.sunmipostester.data.PosRepository
import com.example.sunmipostester.data.RevenueReport
import com.example.sunmipostester.data.TextFormat
import com.example.sunmipostester.databinding.ActivityRevenueReportBinding
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * Revenue report for a selectable period (today / last 7 days / last 30 days).
 *
 * Figures come straight from the orders table and only include PAID orders —
 * cancelled orders are excluded so the report matches the money actually taken.
 */
class RevenueReportActivity : SecuredActivity() {

    override val requiredPermission = Permission.VIEW_REPORTS

    private lateinit var binding: ActivityRevenueReportBinding
    private lateinit var repo: PosRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRevenueReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.rep_title)

        repo = PosRepository(this)

        binding.periodGroup.setOnCheckedChangeListener { _, _ -> loadReport() }

        loadReport()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    // ---- Period ----------------------------------------------------------

    /** Number of days the selected period covers, counting today. */
    private fun selectedDays(): Int = when (binding.periodGroup.checkedRadioButtonId) {
        R.id.period7 -> 7
        R.id.period30 -> 30
        else -> 1
    }

    private fun periodRange(): Pair<Long, Long> {
        val startOfToday = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val dayMs = 24L * 60 * 60 * 1000
        val from = startOfToday - (selectedDays() - 1) * dayMs
        val to = startOfToday + dayMs - 1
        return from to to
    }

    // ---- Render ----------------------------------------------------------

    private fun loadReport() {
        val (from, to) = periodRange()
        lifecycleScope.launch {
            render(repo.revenueReport(from, to))
        }
    }

    private fun render(report: RevenueReport) {
        binding.repRevenue.text = TextFormat.vnd(report.totalRevenue)
        binding.repOrders.text = report.totalOrders.toString()
        binding.repAverage.text = TextFormat.vnd(report.averageOrderValue)

        val hasData = report.totalOrders > 0
        binding.repEmpty.visibility = if (hasData) View.GONE else View.VISIBLE
        val detailVisibility = if (hasData) View.VISIBLE else View.GONE
        binding.labelByMethod.visibility = detailVisibility
        binding.repByMethod.visibility = detailVisibility
        binding.labelTopProducts.visibility = detailVisibility
        binding.repTopProducts.visibility = detailVisibility

        if (!hasData) return

        binding.repByMethod.text = report.byMethod.joinToString("\n") { row ->
            "${row.paymentMethod.label}: ${TextFormat.vnd(row.revenue)}  (${row.orderCount} đơn)"
        }

        binding.repTopProducts.text = report.topProducts
            .mapIndexed { index, p ->
                "${index + 1}. ${p.productName} — ${p.quantity} ly · ${TextFormat.vnd(p.revenue)}"
            }
            .joinToString("\n")
    }
}
