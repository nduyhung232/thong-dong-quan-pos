package com.example.sunmipostester.sale

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sunmipostester.data.CartLine
import com.example.sunmipostester.data.ProductEntity
import com.example.sunmipostester.data.TextFormat
import com.example.sunmipostester.databinding.ItemCartLineBinding
import com.example.sunmipostester.databinding.ItemCategoryBinding
import com.example.sunmipostester.databinding.ItemProductBinding

/** Horizontal list of category filter chips. */
class CategoryAdapter(
    private val onSelect: (String) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.VH>() {

    private val categories = mutableListOf<String>()
    private var selected = 0

    fun submit(items: List<String>) {
        categories.clear()
        categories.addAll(items)
        selected = 0
        notifyDataSetChanged()
    }

    inner class VH(val binding: ItemCategoryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val category = categories[position]
        holder.binding.categoryChip.text = category
        holder.binding.categoryChip.isChecked = position == selected
        holder.binding.categoryChip.setOnClickListener {
            val prev = selected
            selected = holder.bindingAdapterPosition
            notifyItemChanged(prev)
            notifyItemChanged(selected)
            onSelect(category)
        }
    }

    override fun getItemCount() = categories.size
}

/** Grid of products for the selected category. */
class ProductAdapter(
    private val onAdd: (ProductEntity) -> Unit
) : RecyclerView.Adapter<ProductAdapter.VH>() {

    private val items = mutableListOf<ProductEntity>()

    fun submit(products: List<ProductEntity>) {
        items.clear()
        items.addAll(products)
        notifyDataSetChanged()
    }

    inner class VH(val binding: ItemProductBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val p = items[position]
        holder.binding.productName.text = p.name
        holder.binding.productPrice.text = TextFormat.vnd(p.price)
        holder.binding.root.setOnClickListener { onAdd(p) }
    }

    override fun getItemCount() = items.size
}

/** Cart lines with quantity steppers and item click for toppings. */
class CartAdapter(
    private val onPlus: (CartLine) -> Unit,
    private val onMinus: (CartLine) -> Unit,
    private val onItemClick: (CartLine) -> Unit
) : RecyclerView.Adapter<CartAdapter.VH>() {

    private val items = mutableListOf<CartLine>()

    fun submit(lines: List<CartLine>) {
        items.clear()
        items.addAll(lines)
        notifyDataSetChanged()
    }

    inner class VH(val binding: ItemCartLineBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemCartLineBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val l = items[position]
        holder.binding.lineName.text = l.product.name
        if (l.selectedToppings.isNotEmpty()) {
            holder.binding.lineToppings.visibility = android.view.View.VISIBLE
            val toppingTotal = l.selectedToppings.sumOf { it.price }
            holder.binding.lineToppings.text = "+ ${l.toppingsDisplay} (+${TextFormat.vnd(toppingTotal)})"
        } else {
            holder.binding.lineToppings.visibility = android.view.View.GONE
        }
        holder.binding.lineTotal.text = TextFormat.vnd(l.lineTotal)
        holder.binding.lineQty.text = l.quantity.toString()
        holder.binding.btnPlus.setOnClickListener { onPlus(l) }
        holder.binding.btnMinus.setOnClickListener { onMinus(l) }
        holder.binding.root.setOnClickListener { onItemClick(l) }
    }

    override fun getItemCount() = items.size
}
