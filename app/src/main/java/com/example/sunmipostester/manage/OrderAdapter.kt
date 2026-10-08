package com.example.sunmipostester.manage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sunmipostester.data.OrderEntity
import com.example.sunmipostester.data.OrderStatus
import com.example.sunmipostester.data.TextFormat
import com.example.sunmipostester.databinding.ItemOrderBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Order history list. Tapping a row opens the detail dialog. */
class OrderAdapter(
    private val onClick: (OrderEntity) -> Unit
) : RecyclerView.Adapter<OrderAdapter.VH>() {

    private val items = mutableListOf<OrderEntity>()
    private val timeFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    fun submit(orders: List<OrderEntity>) {
        items.clear()
        items.addAll(orders)
        notifyDataSetChanged()
    }

    fun append(orders: List<OrderEntity>) {
        if (orders.isEmpty()) return
        val start = items.size
        items.addAll(orders)
        notifyItemRangeInserted(start, orders.size)
    }

    inner class VH(val binding: ItemOrderBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val o = items[position]
        holder.binding.oNumber.text = "Đơn #${o.id}"
        holder.binding.oMeta.text =
            "${timeFormat.format(Date(o.createdAtMs))} · ${o.paymentMethod.label}"
        holder.binding.oTotal.text = TextFormat.vnd(o.total)
        holder.binding.oStatus.visibility =
            if (o.status == OrderStatus.CANCELLED) View.VISIBLE else View.GONE
        holder.binding.root.setOnClickListener { onClick(o) }
    }

    override fun getItemCount() = items.size
}
