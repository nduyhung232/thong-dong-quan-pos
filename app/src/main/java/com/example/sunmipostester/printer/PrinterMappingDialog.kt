package com.example.sunmipostester.printer

import android.app.Activity
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

/** One-time assignment of attached USB printers to receipt and label roles. */
object PrinterMappingDialog {

    fun show(activity: Activity, printers: PrinterProvider) {
        val options = printers.availablePrinterOptions()
        if (options.size < 2) {
            Toast.makeText(
                activity,
                "Cần kết nối đồng thời máy in hóa đơn và máy in tem qua USB để gán riêng.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val labels = options.map { it.label }.toTypedArray()
        val oldReceipt = options.indexOfFirst {
            it.key == printers.savedReceiptPrinterKey()
        }.coerceAtLeast(0)
        AlertDialog.Builder(activity)
            .setTitle("Chọn máy in hóa đơn")
            .setSingleChoiceItems(labels, oldReceipt) { dialog, index ->
                dialog.dismiss()
                chooseLabelPrinter(activity, printers, options, options[index].key)
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun chooseLabelPrinter(
        activity: Activity,
        printers: PrinterProvider,
        options: List<PrinterProvider.PrinterOption>,
        receiptKey: String
    ) {
        val labels = options.filterNot { it.key == receiptKey }
        val oldLabelIndex = labels.indexOfFirst {
            it.key == printers.savedLabelPrinterKey()
        }.coerceAtLeast(0)
        AlertDialog.Builder(activity)
            .setTitle("Chọn máy in tem")
            .setSingleChoiceItems(labels.map { it.label }.toTypedArray(), oldLabelIndex) { dialog, index ->
                val saved = printers.savePrinterRoles(receiptKey, labels[index].key)
                dialog.dismiss()
                Toast.makeText(
                    activity,
                    if (saved) "Đã gán máy in hóa đơn và máy in tem" else "Không thể gán trùng máy in",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }
}