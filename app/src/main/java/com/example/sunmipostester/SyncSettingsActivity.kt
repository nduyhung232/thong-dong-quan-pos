package com.example.sunmipostester

import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.sunmipostester.auth.Permission
import com.example.sunmipostester.auth.SecuredActivity
import com.example.sunmipostester.data.PosRepository
import com.example.sunmipostester.databinding.ActivitySyncSettingsBinding
import com.example.sunmipostester.sync.SyncConfig
import com.example.sunmipostester.sync.SyncManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Configure and run server sync (manager only).
 *
 * The manager enters the server URL and the device token they set in the web
 * admin. "Kiểm tra kết nối" verifies the URL+token (a pull that isn't applied).
 * "Đồng bộ ngay" pulls master data then pushes local orders/shifts.
 */
class SyncSettingsActivity : SecuredActivity() {

    override val requiredPermission = Permission.MANAGE_SYNC

    private lateinit var binding: ActivitySyncSettingsBinding
    private lateinit var config: SyncConfig
    private lateinit var manager: SyncManager

    private val timeFmt = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySyncSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.sync_title)

        config = SyncConfig(this)
        manager = SyncManager(PosRepository(this), config)

        binding.inputUrl.setText(config.baseUrl)
        binding.inputToken.setText(config.token)
        refreshLastSync()

        binding.btnSave.setOnClickListener { saveConfig() }
        binding.btnTest.setOnClickListener { testConnection() }
        binding.btnSync.setOnClickListener { runSync() }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun saveConfig(): Boolean {
        val url = binding.inputUrl.text?.toString()?.trim().orEmpty()
        val token = binding.inputToken.text?.toString()?.trim().orEmpty()
        if (url.isBlank() || token.isBlank()) {
            setStatus(getString(R.string.sync_need_config))
            return false
        }
        config.save(url, token)
        com.example.sunmipostester.sync.AutoPushScheduler.scheduleNext(this, forceReplace = true)
        setStatus(getString(R.string.sync_saved))
        refreshLastSync()
        return true
    }

    private fun testConnection() {
        if (!saveConfig()) return
        setBusy(true)
        setStatus(getString(R.string.sync_testing))
        lifecycleScope.launch {
            try {
                val productCount = manager.testConnection()
                setStatus(getString(R.string.sync_test_ok, productCount))
            } catch (e: Exception) {
                setStatus(getString(R.string.sync_test_failed, e.message ?: "?"))
            } finally {
                setBusy(false)
            }
        }
    }

    private fun runSync() {
        if (!saveConfig()) return
        setBusy(true)
        setStatus(getString(R.string.sync_running))
        lifecycleScope.launch {
            try {
                val r = manager.sync()
                val base = getString(
                    R.string.sync_result,
                    r.pulledProducts, r.pulledStaff, r.pulledCodes,
                    r.pushedOrders, r.pushedShifts
                )
                val text = if (r.rejected.isEmpty()) base
                else base + "\n" + getString(R.string.sync_rejected, r.rejected.size) +
                    "\n" + r.rejected.joinToString("\n")
                setStatus(text)
                refreshLastSync()
            } catch (e: Exception) {
                setStatus(getString(R.string.sync_failed, e.message ?: "?"))
            } finally {
                setBusy(false)
            }
        }
    }

    private fun refreshLastSync() {
        val last = config.lastSyncAtMs
        binding.lastSync.text = if (last == 0L) {
            getString(R.string.sync_never)
        } else {
            getString(R.string.sync_last, timeFmt.format(Date(last)))
        }
        val nextTime = com.example.sunmipostester.sync.AutoPushScheduler.getNextScheduledTimeFormatted(this)
        binding.tvNextPushTime.text = "Lần tự động PUSH tiếp theo: $nextTime"
    }

    private fun setBusy(busy: Boolean) {
        binding.btnSave.isEnabled = !busy
        binding.btnTest.isEnabled = !busy
        binding.btnSync.isEnabled = !busy
    }

    private fun setStatus(msg: String) {
        binding.syncStatus.text = msg
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
