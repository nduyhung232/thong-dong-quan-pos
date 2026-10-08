package com.example.sunmipostester

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.sunmipostester.auth.Session
import com.example.sunmipostester.data.PosRepository
import com.example.sunmipostester.data.StaffEntity
import com.example.sunmipostester.data.StaffRole
import com.example.sunmipostester.databinding.ActivityLoginBinding
import kotlinx.coroutines.launch

/**
 * Launcher screen. Routes to first-time setup when no staff exist, otherwise asks
 * for staff + PIN.
 *
 * SECURITY NOTES
 *  - The PIN field is cleared after every attempt, successful or not.
 *  - A failed attempt shows one generic message and never reveals whether the
 *    account or the PIN was the problem.
 *  - PIN values are never written to logs.
 *
 * NOT IMPLEMENTED (recommend before production): rate limiting / lockout after
 * repeated failures. A 4-digit PIN with unlimited attempts is brute-forceable by
 * anyone holding the terminal.
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var repo: PosRepository

    private var staffList: List<StaffEntity> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repo = PosRepository(this)

        binding.btnLogin.setOnClickListener { attemptLogin() }
    }

    override fun onResume() {
        super.onResume()
        Session.signOut() // returning here always means a new sign-in
        binding.inputPin.text?.clear()
        binding.loginError.visibility = View.GONE
        loadStaff()
    }

    private fun loadStaff() {
        lifecycleScope.launch {
            // Seed the sample menu and default managers on first launch; harmless afterwards.
            repo.seedMenuIfEmpty()
            repo.seedStaffIfEmpty()

            staffList = repo.activeStaff()
            if (staffList.isEmpty()) {
                showError(getString(R.string.login_no_staff))
                binding.btnLogin.isEnabled = false
                return@launch
            }

            binding.btnLogin.isEnabled = true
            binding.staffSpinner.adapter = ArrayAdapter(
                this@LoginActivity,
                android.R.layout.simple_spinner_dropdown_item,
                staffList.map { "${it.name} (${roleLabel(it.role)})" }
            )
        }
    }

    private fun attemptLogin() {
        val position = binding.staffSpinner.selectedItemPosition
        val staff = staffList.getOrNull(position) ?: return
        val pin = binding.inputPin.text?.toString().orEmpty()

        binding.btnLogin.isEnabled = false
        lifecycleScope.launch {
            val authenticated = repo.authenticate(staff.id, pin)
            binding.inputPin.text?.clear() // never leave the PIN on screen
            binding.btnLogin.isEnabled = true

            if (authenticated == null) {
                // Single generic message: don't disclose which part failed.
                showError(getString(R.string.login_failed))
                return@launch
            }
            binding.loginError.visibility = View.GONE
            Session.signIn(authenticated)
            startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
        }
    }

    private fun showError(message: String) {
        binding.loginError.text = message
        binding.loginError.visibility = View.VISIBLE
    }

    private fun roleLabel(role: StaffRole): String = getString(
        if (role == StaffRole.MANAGER) R.string.role_manager else R.string.role_cashier
    )
}
