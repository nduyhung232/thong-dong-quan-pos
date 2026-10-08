package com.example.sunmipostester

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.example.sunmipostester.auth.Permission
import com.example.sunmipostester.auth.SecuredActivity
import com.example.sunmipostester.backup.DataExporter
import com.example.sunmipostester.data.PosRepository
import com.example.sunmipostester.databinding.ActivityBackupBinding
import kotlinx.coroutines.launch

/**
 * Backup and export (manager only).
 *
 *  - "XUẤT CSV": one CSV per table, openable in Excel.
 *  - "SAO LƯU DATABASE": raw copy of pos.db (plus WAL/SHM) for full restore.
 *
 * SECURITY: exports contain no PIN hashes or salts, so a leaked file cannot be
 * used to brute-force staff PINs offline. The files DO contain revenue and
 * transaction data, so they must still be treated as internal business records.
 */
class BackupActivity : SecuredActivity() {

    override val requiredPermission = Permission.EXPORT_DATA

    private lateinit var binding: ActivityBackupBinding
    private lateinit var repo: PosRepository
    private lateinit var exporter: DataExporter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBackupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.backup_title)

        repo = PosRepository(this)
        exporter = DataExporter(this)

        binding.btnExportCsv.setOnClickListener { exportCsv() }
        binding.btnBackupDb.setOnClickListener { backupDatabase() }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun exportCsv() {
        setWorking(true)
        lifecycleScope.launch {
            try {
                val snapshot = repo.exportSnapshot()
                val result = exporter.exportCsv(snapshot)
                showResult(result)
            } catch (e: Exception) {
                // Report the failure without leaking internals into logs.
                showFailure()
            } finally {
                setWorking(false)
            }
        }
    }

    private fun backupDatabase() {
        setWorking(true)
        lifecycleScope.launch {
            try {
                showResult(exporter.backupDatabase())
            } catch (e: Exception) {
                showFailure()
            } finally {
                setWorking(false)
            }
        }
    }

    private fun showResult(result: DataExporter.Result) {
        binding.backupStatus.text =
            "${getString(R.string.backup_done)} — ${result.files.size} file"
        binding.backupPath.text =
            "${getString(R.string.backup_location)}\n${result.directory.absolutePath}"
    }

    private fun showFailure() {
        binding.backupStatus.text = getString(R.string.backup_failed)
        binding.backupPath.text = ""
    }

    private fun setWorking(working: Boolean) {
        binding.btnExportCsv.isEnabled = !working
        binding.btnBackupDb.isEnabled = !working
        if (working) binding.backupStatus.text = getString(R.string.backup_working)
    }
}
