package com.example.sunmipostester.manage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sunmipostester.R
import com.example.sunmipostester.data.ProductEntity
import com.example.sunmipostester.data.TextFormat
import com.example.sunmipostester.databinding.ItemManageProductBinding

/**
 * Product list for the management screen. Shows inactive (stopped) items too, so
 * the shop can bring them back without re-creating them.
 */
class ManageProductAdapter(
    private val onEdit: (ProductEntity) -> Unit,
    private val onToggleActive: (ProductEntity) -> Unit
) : RecyclerView.Adapter<ManageProductAdapter.VH>() {

    private val items = mutableListOf<ProductEntity>()

    fun submit(products: List<ProductEntity>) {
        items.clear()
        items.addAll(products)
        notifyDataSetChanged()
    }

    inner class VH(val binding: ItemManageProductBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemManageProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val p = items[position]
        val ctx = holder.itemView.context

        holder.binding.pName.text = p.name
        holder.binding.pMeta.text = "${p.category} · ${TextFormat.vnd(p.price)}"
        holder.binding.pInactive.visibility = if (p.active) View.GONE else View.VISIBLE

        holder.binding.btnToggleActive.setText(
            if (p.active) R.string.prod_stop_selling else R.string.prod_resume_selling
        )

        holder.binding.btnEdit.setOnClickListener { onEdit(p) }
        holder.binding.btnToggleActive.setOnClickListener { onToggleActive(p) }
    }

    override fun getItemCount() = items.size
}
