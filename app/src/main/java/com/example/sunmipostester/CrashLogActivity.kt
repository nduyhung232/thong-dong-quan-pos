package com.example.sunmipostester

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sunmipostester.databinding.ActivityCrashLogBinding

/** Displays the last saved uncaught exception after the operator relaunches the app. */
class CrashLogActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCrashLogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCrashLogBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        supportActionBar?.title = "Báo cáo sự cố"

        val report = CrashLogStore.read(this)
        binding.crashLog.text = report ?: "Không tìm thấy báo cáo sự cố đã lưu."
        binding.btnCopyCrashLog.setOnClickListener { copyReport(report) }
        binding.btnShareCrashLog.setOnClickListener { shareReport(report) }
        binding.btnContinue.setOnClickListener {
            CrashLogStore.clear(this)
            startActivity(Intent(this, SaleActivity::class.java))
            finish()
        }
    }

    private fun copyReport(report: String?) {
        if (report.isNullOrBlank()) return
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("POS crash report", report))
        Toast.makeText(this, "Đã sao chép log sự cố", Toast.LENGTH_SHORT).show()
    }

    private fun shareReport(report: String?) {
        if (report.isNullOrBlank()) return
        startActivity(Intent.createChooser(
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "POS crash report")
                putExtra(Intent.EXTRA_TEXT, report)
            },
            "Gửi báo cáo sự cố"
        ))
    }
}
