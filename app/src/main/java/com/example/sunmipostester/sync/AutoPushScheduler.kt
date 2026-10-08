package com.example.sunmipostester.sync

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

/**
 * Lịch tự động đẩy dữ liệu (Orders, Shifts) từ POS lên Server.
 * Các khung giờ cố định trong ngày: 10:00, 15:00, 18:00, 21:00 (giờ Việt Nam, UTC+07:00).
 */
object AutoPushScheduler {
    private const val TAG = "AutoPushScheduler"
    const val UNIQUE_WORK_NAME = "AutoPushSyncWork"
    val TARGET_HOURS = listOf(10, 15, 18, 21)
    private val TIME_ZONE = TimeZone.getTimeZone("Asia/Ho_Chi_Minh")

    /**
     * Tính toán mốc thời gian tiếp theo trong danh sách [10, 15, 18, 21].
     * Thêm ngưỡng đệm 60 giây để tránh việc kích hoạt lại cùng mốc nếu chạy sớm vài giây.
     */
    fun getNextSlot(now: Calendar = Calendar.getInstance(TIME_ZONE)): Calendar {
        val minFutureMs = now.timeInMillis + 60_000L

        for (hour in TARGET_HOURS) {
            val candidate = Calendar.getInstance(TIME_ZONE).apply {
                timeInMillis = now.timeInMillis
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            if (candidate.timeInMillis > minFutureMs) {
                return candidate
            }
        }

        // Nếu đã qua mốc cuối trong ngày (21:00), mốc tiếp theo là 10:00 ngày hôm sau
        return Calendar.getInstance(TIME_ZONE).apply {
            timeInMillis = now.timeInMillis
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, TARGET_HOURS.first())
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    /**
     * Lên lịch chạy OneTimeWorkRequest cho mốc giờ kế tiếp.
     * @param forceReplace true nếu muốn ghi đè lịch hiện tại (dùng khi worker hoàn thành hoặc cấu hình lại)
     */
    fun scheduleNext(context: Context, forceReplace: Boolean = false) {
        val now = Calendar.getInstance(TIME_ZONE)
        val nextSlot = getNextSlot(now)
        val delayMs = maxOf(1000L, nextSlot.timeInMillis - now.timeInMillis)

        // Lưu thời điểm dự kiến vào SharedPreferences để giao diện hiển thị
        val prefs = context.getSharedPreferences("auto_push_prefs", Context.MODE_PRIVATE)
        prefs.edit().putLong("next_push_time_ms", nextSlot.timeInMillis).apply()

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<PushSyncWorker>()
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .setConstraints(constraints)
            .addTag("AutoPushSync")
            .build()

        val policy = if (forceReplace) ExistingWorkPolicy.REPLACE else ExistingWorkPolicy.KEEP
        WorkManager.getInstance(context).enqueueUniqueWork(UNIQUE_WORK_NAME, policy, request)

        val fmt = SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault()).apply {
            timeZone = TIME_ZONE
        }
        Log.i(TAG, "Đã đặt lịch tự động PUSH lúc ${fmt.format(nextSlot.time)} (sau ${delayMs / 1000 / 60} phút), policy=$policy")
    }

    /**
     * Lấy chuỗi mô tả thời gian PUSH kế tiếp để hiển thị trên màn hình Cấu hình Đồng bộ.
     */
    fun getNextScheduledTimeFormatted(context: Context): String {
        val prefs = context.getSharedPreferences("auto_push_prefs", Context.MODE_PRIVATE)
        var ms = prefs.getLong("next_push_time_ms", 0L)
        if (ms <= 0L || ms < System.currentTimeMillis()) {
            ms = getNextSlot().timeInMillis
        }
        val fmt = SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault()).apply {
            timeZone = TIME_ZONE
        }
        return fmt.format(ms)
    }
}
