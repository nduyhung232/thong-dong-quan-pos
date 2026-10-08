package com.example.sunmipostester

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.example.sunmipostester.auth.Permission
import com.example.sunmipostester.auth.PinHasher
import com.example.sunmipostester.auth.SecuredActivity
import com.example.sunmipostester.data.PosRepository
import com.example.sunmipostester.data.StaffEntity
import com.example.sunmipostester.data.StaffRole
import com.example.sunmipostester.databinding.ActivityStaffManagementBinding
import com.example.sunmipostester.databinding.DialogCashAmountBinding
import com.example.sunmipostester.databinding.DialogEditStaffBinding
import com.example.sunmipostester.manage.StaffAdapter
import kotlinx.coroutines.launch

/**
 * Staff administration (manager only): create staff, change name/role, reset PIN,
 * deactivate/reactivate.
 *
 * SECURITY
 *  - PINs are only ever entered here and hashed immediately; an existing PIN is
 *    never displayed or retrievable, only replaced.
 *  - The app refuses to remove or demote the last active manager, which would lock
 *    everyone out of administration.
 */
class StaffManagementActivity : SecuredActivity() {

    override val requiredPermission = Permission.MANAGE_STAFF

    private lateinit var binding: ActivityStaffManagementBinding
    private lateinit var repo: PosRepository
    private lateinit var adapter: StaffAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStaffManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.staff_title)

        repo = PosRepository(this)

        adapter = StaffAdapter(
            onEdit = { showStaffDialog(it) },
            onResetPin = { showResetPinDialog(it) },
            onToggleActive = { toggleActive(it) }
        )
        binding.staffList.adapter = adapter

        binding.btnAddStaff.setOnClickListener { showStaffDialog(null) }

        loadStaff()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun loadStaff() {
        lifecycleScope.launch {
            val staff = repo.allStaff()
            adapter.submit(staff)
            binding.staffEmpty.visibility = if (staff.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    /** Add/edit dialog. PIN fields only appear when creating. */
    private fun showStaffDialog(existing: StaffEntity?) {
        val d = DialogEditStaffBinding.inflate(layoutInflater)

        if (existing != null) {
            d.inputStaffName.setText(existing.name)
            if (existing.role == StaffRole.MANAGER) d.roleManager.isChecked = true
            else d.roleCashier.isChecked = true
            // Editing never exposes or re-enters a PIN; use "Đổi PIN" for that.
            d.pinBox.visibility = View.GONE
        }

        AlertDialog.Builder(this)
            .setTitle(if (existing == null) R.string.staff_dialog_add else R.string.staff_dialog_edit)
            .setView(d.root)
            .setPositiveButton(R.string.prod_save) { _, _ ->
                val name = d.inputStaffName.text?.toString()?.trim().orEmpty()
                val role = if (d.roleManager.isChecked) StaffRole.MANAGER else StaffRole.CASHIER

                if (name.isEmpty()) {
                    toast(getString(R.string.setup_name_required))
                    return@setPositiveButton
                }
                if (existing == null) {
                    val pin = d.inputStaffPin.text?.toString().orEmpty()
                    val confirm = d.inputStaffPinConfirm.text?.toString().orEmpty()
                    createStaff(name, role, pin, confirm)
                } else {
                    updateStaff(existing, name, role)
                }
                // Clear PIN inputs so values don't linger in the view tree.
                d.inputStaffPin.text?.clear()
                d.inputStaffPinConfirm.text?.clear()
            }
            .setNegativeButton(R.string.prod_cancel, null)
            .show()
    }

    private fun createStaff(name: String, role: StaffRole, pin: String, confirm: String) {
        if (!PinHasher.isPolicyValid(pin)) {
            toast(getString(R.string.setup_pin_policy))
            return
        }
        if (pin != confirm) {
            toast(getString(R.string.setup_pin_mismatch))
            return
        }
        lifecycleScope.launch {
            repo.createStaff(name, role, pin)
            toast(getString(R.string.staff_saved))
            loadStaff()
        }
    }

    private fun updateStaff(existing: StaffEntity, name: String, role: StaffRole) {
        lifecycleScope.launch {
            val ok = repo.updateStaffProfile(existing, name, role)
            toast(getString(if (ok) R.string.staff_saved else R.string.staff_last_manager))
            loadStaff()
        }
    }

    /** Reset someone's PIN. The old PIN is not needed and cannot be read. */
    private fun showResetPinDialog(staff: StaffEntity) {
        val d = DialogCashAmountBinding.inflate(layoutInflater)
        d.amountWrapper.hint = getString(R.string.staff_new_pin)
        d.inputAmount.inputType =
            android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD
        d.amountHint.visibility = View.VISIBLE
        d.amountHint.text = staff.name

        AlertDialog.Builder(this)
            .setTitle(R.string.staff_dialog_reset_pin)
            .setView(d.root)
            .setPositiveButton(R.string.prod_save) { _, _ ->
                val newPin = d.inputAmount.text?.toString().orEmpty()
                d.inputAmount.text?.clear()
                lifecycleScope.launch {
                    val ok = repo.resetStaffPin(staff, newPin)
                    toast(getString(if (ok) R.string.staff_pin_changed else R.string.setup_pin_policy))
                }
            }
            .setNegativeButton(R.string.prod_cancel, null)
            .show()
    }

    private fun toggleActive(staff: StaffEntity) {
        lifecycleScope.launch {
            if (staff.active) {
                val ok = repo.deactivateStaff(staff)
                if (!ok) toast(getString(R.string.staff_last_manager))
            } else {
                repo.reactivateStaff(staff.id)
            }
            loadStaff()
        }
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
