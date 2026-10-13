package com.example.sunmipostester.common

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.sunmipostester.BackupActivity
import com.example.sunmipostester.data.PosRepository
import com.example.sunmipostester.databinding.DialogStorageInfoBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object StorageInfoDialog {

    fun show(context: Context, repo: PosRepository, scope: CoroutineScope) {
        val binding = DialogStorageInfoBinding.inflate(LayoutInflater.from(context))
        val dialog = AlertDialog.Builder(context)
            .setView(binding.root)
            .create()

        fun bindReport(report: StorageReport) {
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
            val usedStr = StorageHelper.format(context, report.systemUsedBytes)
            val totalStr = StorageHelper.format(context, report.systemTotalBytes)
            val availStr = StorageHelper.format(context, report.systemAvailableBytes)
            binding.tvSystemStorageStats.text =
                "Đã dùng: $usedStr / $totalStr (${report.systemUsedPercent}%) · Trống: $availStr"

            val dbStr = StorageHelper.format(context, report.dbSizeBytes)
            val cacheStr = StorageHelper.format(context, report.cacheSizeBytes)
            val appTotalStr = StorageHelper.format(context, report.totalAppBytes)

            binding.tvAppDatabaseSize.text = "• Dữ liệu cơ sở dữ liệu (pos.db): $dbStr"
            binding.tvAppCacheSize.text = "• Bộ nhớ đệm tạm (Cache): $cacheStr"
            binding.tvAppTotalSize.text = "• Tổng dữ liệu ứng dụng POS: $appTotalStr"
            binding.tvRecordStats.text =
                "Thống kê: ${report.orderCount} đơn hàng · ${report.productCount} món · ${report.shiftCount} ca làm việc"
        }

        fun loadData() {
            scope.launch {
                val report = withContext(Dispatchers.IO) {
                    StorageHelper.getReport(context, repo)
                }
                bindReport(report)
            }
        }

        binding.btnBackupNow.setOnClickListener {
            dialog.dismiss()
            context.startActivity(Intent(context, BackupActivity::class.java))
        }

        binding.btnClearCache.setOnClickListener {
            val freed = StorageHelper.clearAppCache(context)
            val freedStr = StorageHelper.format(context, freed)
            Toast.makeText(context, "Đã dọn dẹp $freedStr bộ nhớ đệm", Toast.LENGTH_SHORT).show()
            loadData()
        }

        binding.btnCloseStorage.setOnClickListener {
            dialog.dismiss()
        }

        loadData()
        dialog.show()
    }
}
