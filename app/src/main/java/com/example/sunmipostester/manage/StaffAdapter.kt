package com.example.sunmipostester.manage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sunmipostester.R
import com.example.sunmipostester.data.StaffEntity
import com.example.sunmipostester.data.StaffRole
import com.example.sunmipostester.databinding.ItemStaffBinding

/**
 * Staff list for the management screen.
 *
 * Shows identity and role only — no credential material is ever bound to a view.
 */
class StaffAdapter(
    private val onEdit: (StaffEntity) -> Unit,
    private val onResetPin: (StaffEntity) -> Unit,
    private val onToggleActive: (StaffEntity) -> Unit
) : RecyclerView.Adapter<StaffAdapter.VH>() {

    private val items = mutableListOf<StaffEntity>()

    fun submit(staff: List<StaffEntity>) {
        items.clear()
        items.addAll(staff)
        notifyDataSetChanged()
    }

    inner class VH(val binding: ItemStaffBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemStaffBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val s = items[position]
        val ctx = holder.itemView.context

        holder.binding.stName.text = s.name
        holder.binding.stRole.text = ctx.getString(
            if (s.role == StaffRole.MANAGER) R.string.role_manager else R.string.role_cashier
        )
        holder.binding.stInactive.visibility = if (s.active) View.GONE else View.VISIBLE

        holder.binding.btnToggleStaff.setText(
            if (s.active) R.string.staff_deactivate else R.string.staff_reactivate
        )

        holder.binding.btnEditStaff.setOnClickListener { onEdit(s) }
        holder.binding.btnResetPin.setOnClickListener { onResetPin(s) }
        holder.binding.btnToggleStaff.setOnClickListener { onToggleActive(s) }
    }

    override fun getItemCount() = items.size
}
