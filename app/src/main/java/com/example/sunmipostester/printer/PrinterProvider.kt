package com.example.sunmipostester.printer

import android.app.Activity
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build

/**
 * Centralizes discovery of the USB receipt printer and USB permission handling,
 * so every screen (test, sale, payment) resolves a [ReceiptPrinter] the same way.
 *
 * Usage:
 *   val provider = PrinterProvider(activity)
 *   provider.register()                 // in onCreate
 *   provider.withPrinter(onError) { p -> ... }
 *   provider.unregister()               // in onDestroy
 */
class PrinterProvider(private val activity: Activity) {

    enum class Error { NO_DEVICE, PERMISSION_DENIED, PRINTERS_NOT_CONFIGURED }

    data class PrinterOption(val key: String, val label: String)

    private val usbManager =
        activity.getSystemService(Context.USB_SERVICE) as UsbManager
    private val printerPrefs = activity.getSharedPreferences("printer_devices", Context.MODE_PRIVATE)

    private var pendingAction: (() -> Unit)? = null
    private var onErrorCallback: ((Error) -> Unit)? = null

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action != ACTION_USB_PERMISSION) return
            val granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
            if (granted) {
                pendingAction?.invoke()
            } else {
                onErrorCallback?.invoke(Error.PERMISSION_DENIED)
            }
            pendingAction = null
        }
    }

    fun register() {
        val filter = IntentFilter(ACTION_USB_PERMISSION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            activity.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            activity.registerReceiver(receiver, filter)
        }
    }

    fun unregister() {
        runCatching { activity.unregisterReceiver(receiver) }
        pendingAction = null
        onErrorCallback = null
    }

    /**
     * Resolve the USB printer and invoke [onReady]. If permission is missing, a
     * system prompt is shown and [onReady] runs once granted. [onError] fires when
     * no printer is attached or permission is denied.
     */
    fun withPrinter(onError: (Error) -> Unit, onReady: (ReceiptPrinter) -> Unit) {
        onErrorCallback = onError
        val devices = usbPrinters()
        val receiptKey = configuredKey(KEY_RECEIPT_DEVICE, devices)
        val device = devices.firstOrNull { deviceKey(it) == receiptKey }
        if (device == null) {
            onError(if (devices.isEmpty()) Error.NO_DEVICE else Error.PRINTERS_NOT_CONFIGURED)
            return
        }
        withDevice(device, onError) {
            onReady(ReceiptPrinter { UsbPrinterConnection(usbManager, device) })
        }
    }

    /** Resolve the explicitly assigned label printer (must be different from the receipt printer). */
    fun withLabelPrinter(onError: (Error) -> Unit, onReady: (LabelPrinter) -> Unit) {
        val devices = usbPrinters()
        val receiptKey = configuredKey(KEY_RECEIPT_DEVICE, devices)
        val labelKey = configuredKey(KEY_LABEL_DEVICE, devices)
        val device = devices.firstOrNull { deviceKey(it) == labelKey && deviceKey(it) != receiptKey }
        if (device == null) {
            onError(if (devices.isEmpty()) Error.NO_DEVICE else Error.PRINTERS_NOT_CONFIGURED)
            return
        }
        withDevice(device, onError) {
            onReady(LabelPrinter { UsbPrinterConnection(usbManager, device) })
        }
    }

    /** Devices currently attached, for the device-configuration screen. */
    fun availablePrinterOptions(): List<PrinterOption> = usbPrinters().map { device ->
        PrinterOption(deviceKey(device), deviceLabel(device))
    }

    fun savedReceiptPrinterKey(): String? = printerPrefs.getString(KEY_RECEIPT_DEVICE, null)

    fun savedLabelPrinterKey(): String? = printerPrefs.getString(KEY_LABEL_DEVICE, null)

    /** Save both roles atomically; a single USB device cannot be assigned to both. */
    fun savePrinterRoles(receiptKey: String, labelKey: String): Boolean {
        val availableKeys = availablePrinterOptions().map { it.key }.toSet()
        if (receiptKey == labelKey || receiptKey !in availableKeys || labelKey !in availableKeys) {
            return false
        }
        printerPrefs.edit()
            .putString(KEY_RECEIPT_DEVICE, receiptKey)
            .putString(KEY_LABEL_DEVICE, labelKey)
            .apply()
        return true
    }

    private fun configuredKey(preferenceKey: String, devices: List<UsbDevice>): String? {
        val configured = printerPrefs.getString(preferenceKey, null)
        if (devices.any { deviceKey(it) == configured }) return configured

        // Upgrade the older VID:PID-only mapping only when it resolves unambiguously.
        val legacy = devices.filter { "${it.vendorId}:${it.productId}" == configured }
        if (legacy.size == 1) {
            val upgraded = deviceKey(legacy.single())
            printerPrefs.edit().putString(preferenceKey, upgraded).apply()
            return upgraded
        }

        // A single attached printer can safely serve receipt output; label printing
        // still requires a second separately assigned device.
        if (devices.size == 1 && preferenceKey == KEY_RECEIPT_DEVICE && configured == null) {
            val key = deviceKey(devices.single())
            printerPrefs.edit().putString(preferenceKey, key).apply()
            return key
        }
        return null
    }

    private fun withDevice(device: UsbDevice, onError: (Error) -> Unit, action: () -> Unit) {
        onErrorCallback = onError
        if (usbManager.hasPermission(device)) {
            action()
        } else {
            pendingAction = action
            requestPermission(device)
        }
    }

    private fun usbPrinters(): List<UsbDevice> = usbManager.deviceList.values.filter { device ->
        UsbPrinterConnection.findPrinterInterface(device) != null
    }

    /** deviceName identifies the attached USB port/device instance, not just its model. */
    private fun deviceKey(device: UsbDevice): String =
        "${device.vendorId}:${device.productId}:${device.deviceName}"

    private fun deviceLabel(device: UsbDevice): String =
        "${device.productName ?: "USB Printer"} · VID ${device.vendorId} PID ${device.productId} · ${device.deviceName}"

    private fun requestPermission(device: UsbDevice) {
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_MUTABLE
        } else 0
        val pi = PendingIntent.getBroadcast(
            activity, 0, Intent(ACTION_USB_PERMISSION).setPackage(activity.packageName), flags
        )
        usbManager.requestPermission(device, pi)
    }

    companion object {
        private const val ACTION_USB_PERMISSION = "com.example.sunmipostester.USB_PERMISSION"
        private const val KEY_RECEIPT_DEVICE = "receipt_device"
        private const val KEY_LABEL_DEVICE = "label_device"
    }
}
