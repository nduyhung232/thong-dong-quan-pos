package com.example.sunmipostester.manage

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sunmipostester.data.ShiftEntity
import com.example.sunmipostester.data.ShiftStatus
import com.example.sunmipostester.data.TextFormat
import com.example.sunmipostester.databinding.ItemShiftBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Shift history list. Shows the cash variance for closed shifts so an over/short
 * is visible at a glance.
 */
class ShiftAdapter : RecyclerView.Adapter<ShiftAdapter.VH>() {

    private val items = mutableListOf<ShiftEntity>()
    private val dayTime = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
    private val timeOnly = SimpleDateFormat("HH:mm", Locale.getDefault())

    fun submit(shifts: List<ShiftEntity>) {
        items.clear()
        items.addAll(shifts)
        notifyDataSetChanged()
    }

    inner class VH(val binding: ItemShiftBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemShiftBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val s = items[position]

        val opened = dayTime.format(Date(s.openedAtMs))
        val closed = s.closedAtMs?.let { timeOnly.format(Date(it)) } ?: "…"
        holder.binding.sTime.text = "$opened → $closed"

        val counted = s.countedCash?.let { TextFormat.vnd(it) } ?: "-"
        holder.binding.sFigures.text =
            "Đầu ca ${TextFormat.vnd(s.openingCash)} · Két $counted"

        bindDifference(holder, s)
    }

    /** Colour-code the variance: green = matched, red = short, orange = over. */
    private fun bindDifference(holder: VH, s: ShiftEntity) {
        val view = holder.binding.sDiff
        if (s.status == ShiftStatus.OPEN) {
            view.text = holder.itemView.context.getString(
                com.example.sunmipostester.R.string.shift_status_open
            )
            view.setTextColor(Color.parseColor("#1565C0"))
            return
        }
        val diff = s.cashDifference ?: 0
        when {
            diff == 0 -> {
                view.text = holder.itemView.context.getString(
                    com.example.sunmipostester.R.string.shift_diff_ok
                )
                view.setTextColor(Color.parseColor("#2E7D32"))
            }
            diff > 0 -> {
                view.text = "+${TextFormat.money(diff)}"
                view.setTextColor(Color.parseColor("#EF6C00"))
            }
            else -> {
                view.text = "-${TextFormat.money(-diff)}"
                view.setTextColor(Color.parseColor("#C62828"))
            }
        }
    }

    override fun getItemCount() = items.size
}
