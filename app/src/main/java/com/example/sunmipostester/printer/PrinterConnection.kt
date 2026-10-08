package com.example.sunmipostester.printer

/**
 * Transport abstraction for an ESC/POS printer.
 *
 * Implementations: [NetworkPrinterConnection] (LAN, port 9100) and
 * [UsbPrinterConnection] (USB host). The command bytes are identical for both;
 * only the byte pipe differs.
 *
 * All methods are blocking and must be called off the main thread.
 */
interface PrinterConnection {

    /** Open the underlying pipe. Throws on failure. */
    fun open()

    /** Write raw bytes to the printer. */
    fun write(data: ByteArray)

    /**
     * Send a command and read up to [expected] status bytes back.
     * Returns the bytes actually read (may be fewer, or empty on timeout).
     * Used for DLE EOT real-time status. USB read support is device-dependent.
     */
    fun writeAndRead(data: ByteArray, expected: Int, timeoutMs: Int): ByteArray

    /** Close and release resources. Safe to call multiple times. */
    fun close()
}
