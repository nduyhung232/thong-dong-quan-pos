package com.example.sunmipostester.printer

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset
import java.text.Normalizer

/**
 * Builders for standard Epson-compatible ESC/POS command bytes.
 *
 * The RICHTA R200EU label states "Command Support: ESC/POS", so these standard
 * commands apply. Source for command semantics: Epson ESC/POS command reference.
 */
object EscPos {

    private const val ESC: Byte = 0x1B
    private const val GS: Byte = 0x1D
    private const val DLE: Byte = 0x10
    private const val EOT: Byte = 0x04
    private const val LF: Byte = 0x0A

    /** Initialize printer: ESC @ */
    val INIT = byteArrayOf(ESC, '@'.code.toByte())

    /**
     * Open cash drawer: ESC p m t1 t2
     *  m  = connector pin (0 = pin 2, 1 = pin 5)
     *  t1 = ON time  (t1 x 2 ms)
     *  t2 = OFF time (t2 x 2 ms)
     * The RICHTA drawer is wired to the standard kick-out connector; pin 2 (m=0)
     * is the common default. Source: Epson ESC/POS "Generate pulse".
     */
    fun openDrawer(pin: Int = 0, onMs: Int = 50, offMs: Int = 200): ByteArray {
        val t1 = (onMs / 2).coerceIn(0, 255)
        val t2 = (offMs / 2).coerceIn(0, 255)
        val m = if (pin == 1) 1 else 0
        return byteArrayOf(ESC, 'p'.code.toByte(), m.toByte(), t1.toByte(), t2.toByte())
    }

    /**
     * Real-time printer status request: DLE EOT n
     *  n = 1 -> printer status (contains the drawer-kick pin 3 level, bit 2)
     * Source: Epson ESC/POS "Transmit real-time status".
     */
    val REQ_PRINTER_STATUS = byteArrayOf(DLE, EOT, 0x01)

    /** Alignment: ESC a n  (0 left, 1 center, 2 right) */
    fun align(n: Int) = byteArrayOf(ESC, 'a'.code.toByte(), n.toByte())

    /**
     * Text size: GS ! n. Lower nibble = height mult, upper nibble = width mult.
     * 0x00 = normal, 0x11 = double width+height.
     */
    fun textSize(n: Int) = byteArrayOf(GS, '!'.code.toByte(), n.toByte())

    /** Emphasis (bold) on/off: ESC E n */
    fun bold(on: Boolean) = byteArrayOf(ESC, 'E'.code.toByte(), if (on) 1 else 0)

    /** Feed n lines: ESC d n */
    fun feed(lines: Int) = byteArrayOf(ESC, 'd'.code.toByte(), lines.toByte())

    /** Reverse-feed n/180 inch: ESC j n. Used to reduce the fixed leading margin after a cut. */
    fun reverseFeed(dots: Int) = byteArrayOf(ESC, 'j'.code.toByte(), dots.coerceIn(0, 255).toByte())

    /**
     * Full cut: GS V 0. Many 80mm receipt printers support this; harmless if not.
     */
    val CUT = byteArrayOf(GS, 'V'.code.toByte(), 0x00)

    /** Legacy CP1258 encoder; active printing uses [rasterText] to avoid font-table differences. */
    fun text(s: String): ByteArray {
        val charset = Charset.forName("windows-1258")
        val normalized = Normalizer.normalize(s, Normalizer.Form.NFD)
        val out = ByteArrayOutputStream()
        var index = 0
        while (index < normalized.length) {
            val base = normalized[index]
            if (index + 1 < normalized.length) {
                val shape = normalized[index + 1]
                val composed = composeVietnameseBase(base, shape)
                if (composed != null) {
                    out.write(composed.toString().toByteArray(charset))
                    index += 2
                    continue
                }
            }
            out.write(base.toString().toByteArray(charset))
            index++
        }
        return out.toByteArray()
    }

    /** Render a line with Android's Unicode font and send it as ESC/POS raster graphics. */
    fun rasterText(s: String, fontSize: Float = 20f, bold: Boolean = false): ByteArray {
        val maxWidth = 576
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
            color = Color.BLACK
            textSize = fontSize
            typeface = Typeface.create(Typeface.MONOSPACE, if (bold) Typeface.BOLD else Typeface.NORMAL)
        }
        while (paint.measureText(s) > maxWidth - 8 && paint.textSize > 10f) {
            paint.textSize -= 1f
        }

        val widthPixels = (paint.measureText(s).toInt() + 8).coerceIn(8, maxWidth)
        val bytesPerRow = (widthPixels + 7) / 8
        val imageWidth = bytesPerRow * 8
        val metrics = paint.fontMetrics
        val height = (metrics.descent - metrics.ascent).toInt() + 4
        val bitmap = Bitmap.createBitmap(imageWidth, height, Bitmap.Config.ARGB_8888)
        try {
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.WHITE)
            val baseline = (height - (metrics.descent - metrics.ascent)) / 2f - metrics.ascent
            canvas.drawText(s, 4f, baseline, paint)

            val pixels = IntArray(imageWidth)
            val imageData = ByteArrayOutputStream(bytesPerRow * height)
            for (y in 0 until height) {
                bitmap.getPixels(pixels, 0, imageWidth, 0, y, imageWidth, 1)
                for (byteIndex in 0 until bytesPerRow) {
                    var value = 0
                    for (bit in 0 until 8) {
                        val color = pixels[byteIndex * 8 + bit]
                        val luminance = (Color.red(color) * 299 + Color.green(color) * 587 + Color.blue(color) * 114) / 1000
                        if (luminance < 160) value = value or (0x80 shr bit)
                    }
                    imageData.write(value)
                }
            }

            val out = ByteArrayOutputStream()
            // GS v 0 already advances by the bitmap's height. The synchronization
            // LF after it should add only a small gap, not the printer's full default.
            out.write(byteArrayOf(ESC, '3'.code.toByte(), 8))
            out.write(byteArrayOf(
                GS, 'v'.code.toByte(), '0'.code.toByte(), 0,
                (bytesPerRow and 0xFF).toByte(), (bytesPerRow shr 8).toByte(),
                (height and 0xFF).toByte(), (height shr 8).toByte()
            ))
            out.write(imageData.toByteArray())
            // Terminate every raster row explicitly. Some R200EU firmware revisions
            // fail to synchronize consecutive GS v 0 payloads without a line feed.
            out.write(byteArrayOf(LF))
            out.write(byteArrayOf(ESC, '2'.code.toByte()))
            return out.toByteArray()
        } finally {
            bitmap.recycle()
        }
    }

    /**
     * Converts an arbitrary Android [Bitmap] into ESC/POS raster bit image bytes (GS v 0).
     *
     * @param source The input bitmap.
     * @param targetWidth The desired width in dots (e.g. 384 for standard logo or 288 for compact).
     * @param centerOn576 If true, centers the image within the standard 80mm printable width (576 dots / 72 bytes).
     * @param dither If true, applies Floyd-Steinberg dithering for smooth shading.
     */
    fun rasterBitmap(
        source: Bitmap,
        targetWidth: Int = 384,
        centerOn576: Boolean = true,
        dither: Boolean = true
    ): ByteArray {
        val totalWidth = if (centerOn576) 576 else ((targetWidth + 7) / 8) * 8
        val scale = targetWidth.toFloat() / source.width.toFloat()
        val scaledHeight = (source.height * scale).toInt().coerceAtLeast(1)
        val scaledWidth = targetWidth

        val scaled = Bitmap.createScaledBitmap(source, scaledWidth, scaledHeight, true)
        val height = scaledHeight
        val bytesPerRow = totalWidth / 8
        val marginDots = if (centerOn576) (totalWidth - scaledWidth) / 2 else 0

        val pixels = IntArray(scaledWidth * height)
        scaled.getPixels(pixels, 0, scaledWidth, 0, 0, scaledWidth, height)
        if (scaled != source) scaled.recycle()

        // 2D luminance array
        val gray = Array(height) { FloatArray(scaledWidth) }
        for (y in 0 until height) {
            for (x in 0 until scaledWidth) {
                val c = pixels[y * scaledWidth + x]
                val a = Color.alpha(c)
                if (a < 50) {
                    gray[y][x] = 255f
                } else {
                    val r = Color.red(c)
                    val g = Color.green(c)
                    val b = Color.blue(c)
                    gray[y][x] = (r * 299 + g * 587 + b * 114) / 1000f
                }
            }
        }

        val outData = ByteArrayOutputStream(bytesPerRow * height)
        for (y in 0 until height) {
            val rowBits = BooleanArray(totalWidth)
            for (x in 0 until scaledWidth) {
                val oldVal = gray[y][x]
                val isBlack = oldVal < 160f
                val newVal = if (isBlack) 0f else 255f
                rowBits[marginDots + x] = isBlack

                if (dither) {
                    val error = oldVal - newVal
                    if (x + 1 < scaledWidth) gray[y][x + 1] += error * 7f / 16f
                    if (y + 1 < height) {
                        if (x - 1 >= 0) gray[y + 1][x - 1] += error * 3f / 16f
                        gray[y + 1][x] += error * 5f / 16f
                        if (x + 1 < scaledWidth) gray[y + 1][x + 1] += error * 1f / 16f
                    }
                }
            }

            for (byteIdx in 0 until bytesPerRow) {
                var byteVal = 0
                val startBit = byteIdx * 8
                for (bit in 0 until 8) {
                    if (rowBits[startBit + bit]) {
                        byteVal = byteVal or (0x80 shr bit)
                    }
                }
                outData.write(byteVal)
            }
        }

        val out = ByteArrayOutputStream()
        out.write(byteArrayOf(ESC, '3'.code.toByte(), 8))
        out.write(byteArrayOf(
            GS, 'v'.code.toByte(), '0'.code.toByte(), 0,
            (bytesPerRow and 0xFF).toByte(), (bytesPerRow shr 8).toByte(),
            (height and 0xFF).toByte(), (height shr 8).toByte()
        ))
        out.write(outData.toByteArray())
        out.write(byteArrayOf(LF))
        out.write(byteArrayOf(ESC, '2'.code.toByte()))
        return out.toByteArray()
    }

    private fun composeVietnameseBase(base: Char, mark: Char): Char? = when (mark) {
        '\u0302' -> when (base) {
            'a' -> 'â'; 'A' -> 'Â'
            'e' -> 'ê'; 'E' -> 'Ê'
            'o' -> 'ô'; 'O' -> 'Ô'
            else -> null
        }
        '\u0306' -> when (base) {
            'a' -> 'ă'; 'A' -> 'Ă'
            else -> null
        }
        '\u031B' -> when (base) {
            'o' -> 'ơ'; 'O' -> 'Ơ'
            'u' -> 'ư'; 'U' -> 'Ư'
            else -> null
        }
        else -> null
    }

    /**
     * Interpret the DLE EOT 1 status byte for the cash-drawer kick pin 3 level.
     *
     * Epson spec: bit 2 (mask 0x04) reflects the drawer-kick connector pin 3 level.
     *   level 0 (bit clear) -> drawer typically CLOSED
     *   level 1 (bit set)   -> drawer typically OPEN
     * The physical high/low-to-open mapping is switch-dependent, so callers should
     * treat this as best-effort and verify against the real RICHTA + drawer.
     *
     * @return true = open, false = closed
     */
    fun drawerOpenFromStatus(statusByte: Int): Boolean = (statusByte and 0x04) != 0

    /** Convenience: concatenate several command chunks. */
    fun buildReceipt(block: ByteArrayOutputStream.() -> Unit): ByteArray {
        val out = ByteArrayOutputStream()
        out.block()
        return out.toByteArray()
    }
}
