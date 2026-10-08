package com.example.sunmipostester.printer

import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbEndpoint
import android.hardware.usb.UsbInterface
import android.hardware.usb.UsbManager

/**
 * ESC/POS over USB using Android's USB host API.
 *
 * Finds the printer's bulk OUT (and optional bulk IN) endpoints and pipes raw
 * ESC/POS bytes to them. The caller is responsible for having already been
 * granted USB permission for [device].
 */
class UsbPrinterConnection(
    private val usbManager: UsbManager,
    private val device: UsbDevice
) : PrinterConnection {

    private var connection: UsbDeviceConnection? = null
    private var usbInterface: UsbInterface? = null
    private var endpointOut: UsbEndpoint? = null
    private var endpointIn: UsbEndpoint? = null

    override fun open() {
        // Prefer a printer-class interface (class 7); fall back to the first
        // interface exposing a bulk endpoint.
        val iface = findPrinterInterface(device) ?: error("No printer USB interface found")

        for (i in 0 until iface.endpointCount) {
            val ep = iface.getEndpoint(i)
            if (ep.type == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                if (ep.direction == UsbConstants.USB_DIR_OUT) endpointOut = ep
                if (ep.direction == UsbConstants.USB_DIR_IN) endpointIn = ep
            }
        }
        checkNotNull(endpointOut) { "No bulk OUT endpoint on printer" }

        val conn = usbManager.openDevice(device) ?: error("openDevice failed (permission?)")
        if (!conn.claimInterface(iface, true)) {
            conn.close()
            error("claimInterface failed")
        }
        connection = conn
        usbInterface = iface
    }

    override fun write(data: ByteArray) {
        val conn = connection ?: error("Not connected")
        val ep = endpointOut ?: error("No OUT endpoint")
        var offset = 0
        while (offset < data.size) {
            val chunk = minOf(ep.maxPacketSize.coerceAtLeast(64), data.size - offset)
            val sent = conn.bulkTransfer(ep, data.copyOfRange(offset, offset + chunk), chunk, 2000)
            if (sent < 0) error("bulkTransfer OUT failed at offset $offset")
            offset += sent
        }
    }

    override fun writeAndRead(data: ByteArray, expected: Int, timeoutMs: Int): ByteArray {
        write(data)
        val conn = connection ?: return ByteArray(0)
        val ep = endpointIn ?: return ByteArray(0) // no IN endpoint -> cannot read status
        val buf = ByteArray(expected.coerceAtLeast(1))
        val n = conn.bulkTransfer(ep, buf, buf.size, timeoutMs)
        return if (n <= 0) ByteArray(0) else buf.copyOf(n)
    }

    override fun close() {
        val conn = connection
        val iface = usbInterface
        if (conn != null && iface != null) runCatching { conn.releaseInterface(iface) }
        runCatching { conn?.close() }
        connection = null
        usbInterface = null
        endpointOut = null
        endpointIn = null
    }

    companion object {
        private const val USB_CLASS_PRINTER = 7

        /** Pick the USB interface most likely to be the printer. */
        fun findPrinterInterface(device: UsbDevice): UsbInterface? {
            // First pass: a dedicated printer-class interface.
            for (i in 0 until device.interfaceCount) {
                val iface = device.getInterface(i)
                if (iface.interfaceClass == USB_CLASS_PRINTER) return iface
            }
            // Second pass: any interface that has a bulk endpoint.
            for (i in 0 until device.interfaceCount) {
                val iface = device.getInterface(i)
                for (j in 0 until iface.endpointCount) {
                    if (iface.getEndpoint(j).type == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                        return iface
                    }
                }
            }
            return null
        }
    }
}
