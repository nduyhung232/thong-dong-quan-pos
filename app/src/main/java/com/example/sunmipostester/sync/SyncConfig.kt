package com.example.sunmipostester.sync

import android.content.Context

/**
 * Sync connection settings, persisted in SharedPreferences.
 *
 * Holds the server base URL and the device token the manager set in the web
 * admin. The token is what the terminal sends as `Authorization: Bearer`.
 *
 * SECURITY NOTE (accepted trade-off): the token is stored in plain
 * SharedPreferences, not EncryptedSharedPreferences, matching the chosen
 * "moderate security" posture. Anyone with root/ADB access to the device could
 * read it. For a stronger posture, switch to androidx.security EncryptedSharedPreferences.
 */
class SyncConfig(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("pos_sync_prefs", Context.MODE_PRIVATE)

    /** Server base URL, e.g. "http://192.168.1.10:3100". No trailing slash. */
    var baseUrl: String
        get() = prefs.getString(KEY_BASE_URL, "").orEmpty()
        set(value) = prefs.edit().putString(KEY_BASE_URL, normalizeUrl(value)).apply()

    /** Device token chosen by the manager on the web admin. */
    var token: String
        get() = prefs.getString(KEY_TOKEN, "").orEmpty()
        set(value) = prefs.edit().putString(KEY_TOKEN, value.trim()).apply()

    /** Epoch ms of the last successful sync, 0 if never. */
    var lastSyncAtMs: Long
        get() = prefs.getLong(KEY_LAST_SYNC, 0L)
        set(value) = prefs.edit().putLong(KEY_LAST_SYNC, value).apply()

    /** True when both URL and token are set, so sync can be attempted. */
    val isConfigured: Boolean
        get() = baseUrl.isNotBlank() && token.isNotBlank()

    fun save(baseUrl: String, token: String) {
        prefs.edit()
            .putString(KEY_BASE_URL, normalizeUrl(baseUrl))
            .putString(KEY_TOKEN, token.trim())
            .apply()
    }

    private fun normalizeUrl(raw: String): String = raw.trim().trimEnd('/')

    companion object {
        private const val KEY_BASE_URL = "base_url"
        private const val KEY_TOKEN = "token"
        private const val KEY_LAST_SYNC = "last_sync_ms"
    }
}
