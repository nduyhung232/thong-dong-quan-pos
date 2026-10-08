package com.example.sunmipostester.printer

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import com.example.sunmipostester.R
import com.example.sunmipostester.data.OrderWithItems

/**
 * Modal dialog that displays a visual preview of the receipt before sending it to the printer.
 */
object ReceiptPreviewDialog {

    fun show(
        context: Context,
        data: OrderWithItems,
        isReprint: Boolean = false,
        tableName: String? = null,
        onPrintConfirmed: () -> Unit
    ) {
        val bitmap = ReceiptTemplateRenderer.renderReceiptBitmap(context, data, isReprint, tableName)
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_receipt_preview, null)
        val imgPreview = dialogView.findViewById<ImageView>(R.id.imgReceiptPreview)
        imgPreview.setImageBitmap(bitmap)

        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .create()

        dialogView.findViewById<View>(R.id.btnClosePreview).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<View>(R.id.btnPrintNow).setOnClickListener {
            dialog.dismiss()
            onPrintConfirmed()
        }

        dialog.show()
    }
}
