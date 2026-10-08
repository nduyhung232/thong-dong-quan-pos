package com.example.sunmipostester

import android.app.Activity
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.example.sunmipostester.data.TextFormat

/** Cash close count by banknote denomination; callback receives total and audit breakdown. */
object CashCountDialog {
    private val denominations = listOf(500_000, 200_000, 100_000, 50_000, 20_000, 10_000, 5_000, 2_000, 1_000)

    fun show(
        activity: Activity,
        expectedCash: Int,
        onConfirm: (countedCash: Int, breakdown: String) -> Unit
    ) {
        val density = activity.resources.displayMetrics.density
        fun dp(value: Int) = (value * density).toInt()
        val quantities = linkedMapOf<Int, EditText>()
        val content = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(8), dp(20), dp(8))
        }
        val expected = TextView(activity).apply {
            text = "Tiền két dự kiến: ${TextFormat.vnd(expectedCash)}"
            textSize = 15f
            setPadding(0, 0, 0, dp(8))
        }
        content.addView(expected)

        val totalLabel = TextView(activity).apply {
            text = "Tổng kiểm đếm: 0 đ"
            textSize = 18f
            setPadding(0, dp(4), 0, dp(8))
        }
        content.addView(totalLabel)

        fun updateTotal() {
            val total = quantities.entries.sumOf { (value, field) ->
                value.toLong() * (field.text?.toString()?.toIntOrNull() ?: 0)
            }.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
            totalLabel.text = "Tổng kiểm đếm: ${TextFormat.vnd(total)}"
        }

        denominations.forEach { denomination ->
            val row = LinearLayout(activity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }
            row.addView(TextView(activity).apply {
                text = "${TextFormat.money(denomination)} đ"
                textSize = 15f
                layoutParams = LinearLayout.LayoutParams(0, dp(48), 1f)
            })
            val countInput = EditText(activity).apply {
                hint = "Số tờ"
                inputType = android.text.InputType.TYPE_CLASS_NUMBER
                gravity = Gravity.END or Gravity.CENTER_VERTICAL
                setSingleLine(true)
                layoutParams = LinearLayout.LayoutParams(dp(110), dp(48))
            }
            countInput.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = updateTotal()
                override fun afterTextChanged(s: Editable?) = Unit
            })
            quantities[denomination] = countInput
            row.addView(countInput)
            content.addView(row)
        }

        val scroll = ScrollView(activity).apply {
            isFillViewport = true
            addView(content, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        }
        val dialog = AlertDialog.Builder(activity)
            .setTitle("Đếm tiền theo mệnh giá")
            .setView(scroll)
            .setPositiveButton("XÁC NHẬN ĐÓNG CA", null)
            .setNegativeButton("HỦY", null)
            .create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val counts = quantities.mapValues { it.value.text?.toString()?.toIntOrNull() ?: 0 }
                if (counts.values.any { it < 0 }) return@setOnClickListener
                val total = counts.entries.sumOf { (value, count) -> value.toLong() * count }
                if (total > Int.MAX_VALUE) {
                    totalLabel.text = "Tổng tiền vượt mức hợp lệ"
                    return@setOnClickListener
                }
                val breakdown = counts.filterValues { it > 0 }
                    .entries.joinToString(";") { "${it.key}x${it.value}" }
                onConfirm(total.toInt(), breakdown)
                dialog.dismiss()
            }
        }
        dialog.show()
    }
}
