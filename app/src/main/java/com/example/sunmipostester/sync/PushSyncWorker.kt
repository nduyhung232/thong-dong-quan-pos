package com.example.sunmipostester.sync

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.sunmipostester.data.PosRepository

/**
 * Worker chạy ngầm theo lịch để đẩy các đơn hàng và ca làm việc chưa đồng bộ lên Server.
 */
class PushSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val TAG = "PushSyncWorker"
    }

    override suspend fun doWork(): Result {
        Log.i(TAG, "PushSyncWorker bắt đầu chạy job tự động đẩy dữ liệu")
        val context = applicationContext
        val config = SyncConfig(context)

        if (!config.isConfigured) {
            Log.w(TAG, "Chưa cấu hình URL hoặc Token máy chủ. Bỏ qua lần PUSH này.")
            AutoPushScheduler.scheduleNext(context, forceReplace = true)
            return Result.success()
        }

        try {
            val repo = PosRepository(context)
            val manager = SyncManager(repo, config)
            val result = manager.pushOnly()

            Log.i(TAG, "Tự động PUSH thành công: ${result.orders} đơn, ${result.shifts} ca, ${result.rejected.size} từ chối")

            if (result.orders > 0 || result.shifts > 0) {
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(
                        context,
                        "Tự động PUSH: Đã gửi ${result.orders} đơn, ${result.shifts} ca lên server",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Tự động PUSH thất bại: ${e.message}", e)
        } finally {
            // Luôn luôn lên lịch cho khung giờ tiếp theo (10h, 15h, 18h, 21h)
            AutoPushScheduler.scheduleNext(context, forceReplace = true)
        }

        return Result.success()
    }
}
