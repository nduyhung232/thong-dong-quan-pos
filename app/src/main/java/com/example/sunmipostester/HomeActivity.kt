package com.example.sunmipostester

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.sunmipostester.auth.Permission
import com.example.sunmipostester.auth.Session
import com.example.sunmipostester.data.StaffRole
import com.example.sunmipostester.databinding.ActivityHomeBinding

/**
 * Main menu. Buttons are shown only when the signed-in role holds the matching
 * permission, so a cashier never sees manager-only functions.
 *
 * Hiding the button is UX, not the control itself — each target screen also
 * enforces its own permission via [requirePermission], so navigating there by
 * other means still fails closed.
 */
class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSale.setOnClickListener { open(SaleActivity::class.java) }
        binding.btnShift.setOnClickListener { open(ShiftActivity::class.java) }
        binding.btnOrders.setOnClickListener { open(OrderManagementActivity::class.java) }
        binding.btnReport.setOnClickListener { open(RevenueReportActivity::class.java) }
        binding.btnProducts.setOnClickListener { open(ProductManagementActivity::class.java) }
        binding.btnStaff.setOnClickListener { open(StaffManagementActivity::class.java) }
        binding.btnBackup.setOnClickListener { open(BackupActivity::class.java) }
        binding.btnSync.setOnClickListener { open(SyncSettingsActivity::class.java) }
        binding.btnDeviceTest.setOnClickListener { open(DeviceTestActivity::class.java) }
        binding.btnSignOut.setOnClickListener { signOut() }
    }

    override fun onResume() {
        super.onResume()
        // No session (app restarted, or signed out) -> back to login.
        val user = Session.current
        if (user == null) {
            signOut()
            return
        }
        binding.sessionInfo.text =
            "${getString(R.string.session_signed_in_as)} ${user.name} · ${roleLabel(user.role)}"
        applyPermissions()
    }

    /** Show only what this role is allowed to do. */
    private fun applyPermissions() {
        binding.btnSale.visibility = visibleIf(Session.can(Permission.SELL))
        binding.btnShift.visibility = visibleIf(Session.can(Permission.MANAGE_SHIFT))
        // Order history is viewable by anyone who can sell; cancelling inside it is
        // separately gated by CANCEL_ORDER.
        binding.btnOrders.visibility = visibleIf(Session.can(Permission.SELL))
        binding.btnReport.visibility = visibleIf(Session.can(Permission.VIEW_REPORTS))
        binding.btnProducts.visibility = visibleIf(Session.can(Permission.MANAGE_PRODUCTS))
        binding.btnStaff.visibility = visibleIf(Session.can(Permission.MANAGE_STAFF))
        binding.btnBackup.visibility = visibleIf(Session.can(Permission.EXPORT_DATA))
        binding.btnSync.visibility = visibleIf(Session.can(Permission.MANAGE_SYNC))
        binding.btnDeviceTest.visibility = visibleIf(Session.can(Permission.DEVICE_TEST))
    }

    private fun visibleIf(allowed: Boolean) = if (allowed) View.VISIBLE else View.GONE

    private fun signOut() {
        Session.signOut()
        startActivity(
            Intent(this, LoginActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        )
        finish()
    }

    private fun open(target: Class<*>) {
        startActivity(Intent(this, target))
    }

    private fun roleLabel(role: StaffRole): String = getString(
        if (role == StaffRole.MANAGER) R.string.role_manager else R.string.role_cashier
    )
}
