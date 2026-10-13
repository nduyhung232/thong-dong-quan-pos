package com.example.sunmipostester.common

import android.content.Context
import android.os.Environment
import android.os.StatFs
import android.text.format.Formatter
import com.example.sunmipostester.data.PosRepository
import java.io.File

/**
 * Diagnostic information regarding device and app storage.
 */
data class StorageReport(
    val dbSizeBytes: Long,
    val cacheSizeBytes: Long,
    val totalAppBytes: Long,
    val systemTotalBytes: Long,
    val systemAvailableBytes: Long,
    val systemUsedBytes: Long,
    val systemUsedPercent: Int,
    val orderCount: Int,
    val productCount: Int,
    val shiftCount: Int
) {
    enum class StatusLevel {
        SAFE, WARNING, DANGER
    }

    val statusLevel: StatusLevel
        get() = when {
            systemUsedPercent >= 90 -> StatusLevel.DANGER
            systemUsedPercent >= 80 -> StatusLevel.WARNING
            else -> StatusLevel.SAFE
        }

    val statusLabel: String
        get() = when (statusLevel) {
            StatusLevel.SAFE -> "🟢 BỘ NHỚ AN TOÀN (<80%)"
            StatusLevel.WARNING -> "🟡 CẢNH BÁO BỘ NHỚ (80% - 90%)"
            StatusLevel.DANGER -> "🔴 NGUY CẤP: BỘ NHỚ GẦN ĐẦY (≥90%)"
        }

    val statusRecommendation: String
        get() = when (statusLevel) {
            StatusLevel.SAFE -> "Bộ nhớ còn dồi dào, hệ thống hoạt động ổn định."
            StatusLevel.WARNING -> "Bộ nhớ thiết bị sắp đầy. Bạn nên xuất sao lưu dữ liệu và kiểm tra dọn bớt file rác."
            StatusLevel.DANGER -> "Bộ nhớ gần đầy! Rất nguy hiểm vì có thể gây lỗi ghi cơ sở dữ liệu. Khuyến nghị Sao lưu ngay lập tức!"
        }
}

object StorageHelper {

    suspend fun getReport(context: Context, repo: PosRepository): StorageReport {
        // 1. Database size (pos.db + WAL + SHM)
        var dbSize = 0L
        val dbFile = context.getDatabasePath("pos.db")
        if (dbFile.exists()) dbSize += dbFile.length()
        val walFile = File(dbFile.path + "-wal")
        if (walFile.exists()) dbSize += walFile.length()
        val shmFile = File(dbFile.path + "-shm")
        if (shmFile.exists()) dbSize += shmFile.length()

        // 2. Cache size
        val cacheSize = getFolderSize(context.cacheDir) + getFolderSize(context.codeCacheDir)

        // 3. App data directory total
        val appDataDir = File(context.applicationInfo.dataDir)
        val totalAppBytes = getFolderSize(appDataDir)

        // 4. Device storage via StatFs
        val stat = StatFs(Environment.getDataDirectory().path)
        val blockSize = stat.blockSizeLong
        val totalBlocks = stat.blockCountLong
        val availableBlocks = stat.availableBlocksLong

        val systemTotalBytes = totalBlocks * blockSize
        val systemAvailableBytes = availableBlocks * blockSize
        val systemUsedBytes = (systemTotalBytes - systemAvailableBytes).coerceAtLeast(0L)
        val systemUsedPercent = if (systemTotalBytes > 0) {
            ((systemUsedBytes.toDouble() / systemTotalBytes.toDouble()) * 100).toInt().coerceIn(0, 100)
        } else {
            0
        }

        // 5. Database record counts
        val (orderCount, productCount, shiftCount) = try {
            repo.recordCounts()
        } catch (_: Exception) {
            Triple(0, 0, 0)
        }

        return StorageReport(
            dbSizeBytes = dbSize,
            cacheSizeBytes = cacheSize,
            totalAppBytes = totalAppBytes,
            systemTotalBytes = systemTotalBytes,
            systemAvailableBytes = systemAvailableBytes,
            systemUsedBytes = systemUsedBytes,
            systemUsedPercent = systemUsedPercent,
            orderCount = orderCount,
            productCount = productCount,
            shiftCount = shiftCount
        )
    }

    fun clearAppCache(context: Context): Long {
        val before = getFolderSize(context.cacheDir)
        deleteFolderContents(context.cacheDir)
        return before
    }

    private fun deleteFolderContents(dir: File?): Boolean {
        if (dir == null || !dir.exists()) return true
        var ok = true
        dir.listFiles()?.forEach { file ->
            if (file.isDirectory) {
                ok = deleteFolderContents(file) && file.delete() && ok
            } else {
                ok = file.delete() && ok
            }
        }
        return ok
    }

    fun getFolderSize(dir: File?): Long {
        if (dir == null || !dir.exists()) return 0L
        var size = 0L
        dir.listFiles()?.forEach { file ->
            size += if (file.isDirectory) getFolderSize(file) else file.length()
        }
        return size
    }

    fun format(context: Context, bytes: Long): String {
        return Formatter.formatFileSize(context, bytes)
    }
}
