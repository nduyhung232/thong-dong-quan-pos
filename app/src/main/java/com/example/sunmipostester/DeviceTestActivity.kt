package com.example.sunmipostester

import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.sunmipostester.auth.Permission
import com.example.sunmipostester.auth.SecuredActivity
import com.example.sunmipostester.databinding.ActivityMainBinding
import com.example.sunmipostester.printer.PrinterProvider
import com.example.sunmipostester.printer.PrinterMappingDialog
import com.example.sunmipostester.printer.ReceiptPrinter
import kotlinx.coroutines.launch

/**
 * Device tester: two buttons (open drawer / print test) and a two-option radio
 * group showing the live drawer status. Reachable from Home > KIỂM TRA THIẾT BỊ.
 */
class DeviceTestActivity : SecuredActivity() {

    override val requiredPermission = Permission.DEVICE_TEST

    private lateinit var binding: ActivityMainBinding
    private lateinit var printers: PrinterProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        printers = PrinterProvider(this)
        printers.register()

        binding.btnOpenDrawer.setOnClickListener { doOpenDrawer() }
        binding.btnPrintTest.setOnClickListener { doPrintTest() }
        binding.btnPrinterMapping.setOnClickListener { PrinterMappingDialog.show(this, printers) }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        printers.unregister()
    }

    private fun doOpenDrawer() = withPrinter { printer ->
        setWorking(true)
        lifecycleScope.launch {
            toast(printer.openDrawer().message)
            refreshDrawerStatus(printer)
            setWorking(false)
        }
    }

    private fun doPrintTest() = withPrinter { printer ->
        setWorking(true)
        lifecycleScope.launch {
            toast(printer.printTest().message)
            setWorking(false)
        }
    }

    private suspend fun refreshDrawerStatus(printer: ReceiptPrinter) {
        when (printer.readDrawerStatus()) {
            true -> setDrawerRadios(open = true, closed = false)
            false -> setDrawerRadios(open = false, closed = true)
            null -> setDrawerRadios(open = false, closed = false)
        }
    }

    private fun withPrinter(onReady: (ReceiptPrinter) -> Unit) {
        printers.withPrinter(
            onError = { err ->
                binding.statusText.text = when (err) {
                    PrinterProvider.Error.NO_DEVICE -> getString(R.string.status_usb_no_device)
                    PrinterProvider.Error.PERMISSION_DENIED -> getString(R.string.status_usb_permission)
                    PrinterProvider.Error.PRINTERS_NOT_CONFIGURED -> "Chưa cấu hình máy in hóa đơn. Hãy gán vai trò máy in trong Cấu hình máy in."
                }
            },
            onReady = onReady
        )
    }

    private fun setWorking(working: Boolean) {
        binding.btnOpenDrawer.isEnabled = !working
        binding.btnPrintTest.isEnabled = !working
        binding.statusText.text =
            getString(if (working) R.string.status_working else R.string.status_ready)
    }

    private fun setDrawerRadios(open: Boolean, closed: Boolean) {
        binding.radioOpen.isChecked = open
        binding.radioClosed.isChecked = closed
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
