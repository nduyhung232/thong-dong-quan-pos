package com.example.sunmipostester

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.sunmipostester.auth.PinHasher
import com.example.sunmipostester.data.PosRepository
import com.example.sunmipostester.data.StaffRole
import com.example.sunmipostester.databinding.ActivitySetupBinding
import kotlinx.coroutines.launch

/**
 * First-run setup: creates the initial MANAGER account.
 *
 * SECURITY: there is deliberately no default or hardcoded PIN — a shipped default
 * credential is the most commonly exploited weakness in POS deployments. The owner
 * must choose their own PIN here before the app can be used.
 */
class SetupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySetupBinding
    private lateinit var repo: PosRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repo = PosRepository(this)

        binding.btnCreate.setOnClickListener { createManager() }
    }

    private fun createManager() {
        val name = binding.inputName.text?.toString()?.trim().orEmpty()
        val pin = binding.inputPin.text?.toString().orEmpty()
        val confirm = binding.inputPinConfirm.text?.toString().orEmpty()

        if (name.isEmpty()) {
            toast(getString(R.string.setup_name_required))
            return
        }
        if (!PinHasher.isPolicyValid(pin)) {
            toast(getString(R.string.setup_pin_policy))
            return
        }
        if (pin != confirm) {
            toast(getString(R.string.setup_pin_mismatch))
            return
        }

        binding.btnCreate.isEnabled = false
        lifecycleScope.launch {
            val id = repo.createStaff(name, StaffRole.MANAGER, pin)
            // Clear the PIN fields so the value doesn't sit on screen.
            binding.inputPin.text?.clear()
            binding.inputPinConfirm.text?.clear()

            if (id == null) {
                toast(getString(R.string.setup_pin_policy))
                binding.btnCreate.isEnabled = true
                return@launch
            }
            toast(getString(R.string.setup_done))
            goToLogin()
        }
    }

    private fun goToLogin() {
        startActivity(
            Intent(this, LoginActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        )
        finish()
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
