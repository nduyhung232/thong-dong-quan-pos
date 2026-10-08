package com.example.sunmipostester

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Collections
import java.util.Date
import java.util.IdentityHashMap
import java.util.Locale

/** Stores the most recent uncaught app exception in private internal storage. */
object CrashLogStore {
    private const val FILE_NAME = "last_crash.log"
    private const val MAX_CHARS = 120_000
    private val sensitiveNumber = Regex("(?<!\\d)\\d{4,8}(?!\\d)")

    fun read(context: Context): String? = runCatching {
        crashFile(context).takeIf { it.isFile }?.readText(Charsets.UTF_8)
    }.getOrNull()

    fun clear(context: Context) {
        runCatching { crashFile(context).delete() }
    }

    fun write(context: Context, thread: Thread, error: Throwable) {
        val body = buildString {
            appendLine("POS app crash report")
            appendLine("Time: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(Date())}")
            appendLine("Thread: ${thread.name}")
            appendLine()

            val seen = Collections.newSetFromMap(IdentityHashMap<Throwable, Boolean>())
            var current: Throwable? = error
            while (current != null && seen.add(current) && length < MAX_CHARS) {
                appendLine("Exception: ${current.javaClass.name}")
                current.message?.let { appendLine("Message: ${redact(it)}") }
                current.stackTrace.forEach { frame ->
                    if (length < MAX_CHARS) appendLine("    at $frame")
                }
                current.suppressed.forEach { suppressed ->
                    if (length < MAX_CHARS) appendLine("Suppressed: ${suppressed.javaClass.name}: ${redact(suppressed.message.orEmpty())}")
                }
                current = current.cause
                if (current != null) appendLine("Caused by:")
            }
        }.take(MAX_CHARS)

        val file = crashFile(context)
        val temp = File(file.parentFile, "$FILE_NAME.tmp")
        FileOutputStream(temp).use { stream ->
            stream.write(body.toByteArray(Charsets.UTF_8))
            stream.fd.sync()
        }
        if (file.exists()) file.delete()
        check(temp.renameTo(file)) { "Could not save crash report" }
    }

    private fun crashFile(context: Context) = File(context.filesDir, FILE_NAME)

    /** Avoid accidentally persisting a numeric PIN embedded in an exception message. */
    private fun redact(value: String): String = sensitiveNumber.replace(value, "[REDACTED]")
}
