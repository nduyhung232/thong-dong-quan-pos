package com.example.sunmipostester.printer

import android.content.Context
import com.example.sunmipostester.PosApplication
import java.io.InputStream
import java.util.concurrent.ConcurrentHashMap

/**
 * Provides pre-rendered, high-quality ESC/POS raster byte streams for the
 * "Thong Dong - Tiệm Chè & Đá Bào" logo on 80mm thermal receipt printers.
 */
object ReceiptLogo {

    enum class Style(val assetFileName: String, val description: String) {
        /** Full vertical logo with boy emblem and stylized typography (384x440 dots centered in 576 dots). */
        FULL("receipt_logo_80mm.bin", "Đầy đủ (Logo đứng + chữ Tiệm Chè)"),

        /** Compact vertical logo, optimized for paper saving (288x328 dots centered in 576 dots). */
        COMPACT("receipt_logo_compact_80mm.bin", "Gọn (Tiết kiệm giấy in)"),

        /** Horizontal banner format (emblem on the left, typography on the right, 480x192 dots). */
        BANNER("receipt_logo_banner_80mm.bin", "Dạng ngang (Banner ngắn)")
    }

    private val cache = ConcurrentHashMap<Style, ByteArray>()

    /**
     * Retrieves the pre-compiled ESC/POS raster command bytes for the given [style].
     * Data is cached in memory after the first read for instant (0ms) receipt generation.
     */
    fun getBytes(context: Context? = null, style: Style = Style.COMPACT): ByteArray {
        val cached = cache[style]
        if (cached != null) return cached

        val ctx = context ?: runCatching { PosApplication.instance }.getOrNull() ?: return ByteArray(0)
        return try {
            val stream: InputStream = ctx.assets.open(style.assetFileName)
            val bytes = stream.use { it.readBytes() }
            cache[style] = bytes
            bytes
        } catch (e: Exception) {
            ByteArray(0)
        }
    }
}
