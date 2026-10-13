package com.example.sunmipostester

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.sunmipostester.auth.Permission
import com.example.sunmipostester.auth.SecuredActivity
import com.example.sunmipostester.common.StorageHelper
import com.example.sunmipostester.common.StorageReport
import com.example.sunmipostester.data.PosRepository
import com.example.sunmipostester.databinding.ActivitySyncSettingsBinding
import com.example.sunmipostester.sync.SyncConfig
import com.example.sunmipostester.sync.SyncManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    private lateinit var repo: PosRepository

    private val timeFmt = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySyncSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.sync_title)

        repo = PosRepository(this)
        config = SyncConfig(this)
        manager = SyncManager(repo, config)

        binding.inputUrl.setText(config.baseUrl)
        binding.inputToken.setText(config.token)
        refreshLastSync()

        binding.btnSave.setOnClickListener { saveConfig() }
        binding.btnTest.setOnClickListener { testConnection() }
        binding.btnSync.setOnClickListener { runSync() }

        binding.btnStorageBackup.setOnClickListener {
            startActivity(Intent(this, BackupActivity::class.java))
        }

        binding.btnStorageClearCache.setOnClickListener {
            val freed = StorageHelper.clearAppCache(this)
            val freedStr = StorageHelper.format(this, freed)
            toast("Đã dọn dẹp $freedStr bộ nhớ đệm")
            refreshStorageInfo()
        }
    }

    override fun onResume() {
        super.onResume()
        refreshStorageInfo()
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

    private fun refreshStorageInfo() {
        lifecycleScope.launch {
            val report = withContext(Dispatchers.IO) {
                StorageHelper.getReport(this@SyncSettingsActivity, repo)
            }
            bindStorageReport(report)
        }
    }

    private fun bindStorageReport(report: StorageReport) {
        when (report.statusLevel) {
            StorageReport.StatusLevel.SAFE -> {
                binding.tvStorageStatusBadge.text = "🟢 AN TOÀN"
                binding.tvStorageStatusBadge.setBackgroundColor(Color.parseColor("#D1FAE5"))
                binding.tvStorageStatusBadge.setTextColor(Color.parseColor("#065F46"))
            }
            StorageReport.StatusLevel.WARNING -> {
                binding.tvStorageStatusBadge.text = "🟡 CẢNH BÁO"
                binding.tvStorageStatusBadge.setBackgroundColor(Color.parseColor("#FEF3C7"))
                binding.tvStorageStatusBadge.setTextColor(Color.parseColor("#92400E"))
            }
            StorageReport.StatusLevel.DANGER -> {
                binding.tvStorageStatusBadge.text = "🔴 NGUY CẤP: GẦN ĐẦY"
                binding.tvStorageStatusBadge.setBackgroundColor(Color.parseColor("#FEE2E2"))
                binding.tvStorageStatusBadge.setTextColor(Color.parseColor("#991B1B"))
            }
        }
        binding.tvStorageRecommendation.text = report.statusRecommendation

        binding.pbStorageProgress.progress = report.systemUsedPercent
        val usedStr = StorageHelper.format(this, report.systemUsedBytes)
        val totalStr = StorageHelper.format(this, report.systemTotalBytes)
        val availStr = StorageHelper.format(this, report.systemAvailableBytes)
        binding.tvSystemStorageStats.text =
            "Đã dùng: $usedStr / $totalStr (${report.systemUsedPercent}%) · Trống: $availStr"

        val dbStr = StorageHelper.format(this, report.dbSizeBytes)
        val cacheStr = StorageHelper.format(this, report.cacheSizeBytes)
        val appTotalStr = StorageHelper.format(this, report.totalAppBytes)

        binding.tvAppDatabaseSize.text = "• Dữ liệu cơ sở dữ liệu (pos.db): $dbStr"
        binding.tvAppCacheSize.text = "• Bộ nhớ đệm tạm (Cache): $cacheStr"
        binding.tvAppTotalSize.text = "• Tổng dữ liệu ứng dụng POS: $appTotalStr"
        binding.tvRecordStats.text =
            "Thống kê: ${report.orderCount} đơn hàng · ${report.productCount} món · ${report.shiftCount} ca làm việc"
    }
}
