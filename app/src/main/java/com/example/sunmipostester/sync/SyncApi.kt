package com.example.sunmipostester.sync

import org.json.JSONObject
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.URL

/**
 * Thin HTTP client for the sync API. Uses the JDK's HttpURLConnection and org.json
 * so no third-party networking/JSON dependency is added to the build.
 *
 * AUTH: every request carries `Authorization: Bearer <token>` only — the token
 * identifies the terminal (there is no deviceId). Calls are blocking; run them on
 * a background dispatcher (SyncManager does).
 */
class SyncApi(baseUrl: String, private val token: String) {

    private val base = baseUrl.trim().trimEnd('/')

    /** Non-2xx response, carrying the server's status and body for diagnostics. */
    class HttpException(val code: Int, val bodyText: String) :
        Exception("HTTP $code: $bodyText")

    /** GET a JSON object from [path] (e.g. "/api/sync/pull"). */
    fun getJson(path: String): JSONObject = request("GET", path, null)

    /** POST [body] as JSON to [path] and return the JSON response. */
    fun postJson(path: String, body: JSONObject): JSONObject = request("POST", path, body)

    private fun request(method: String, path: String, body: JSONObject?): JSONObject {
        val conn = (URL(base + path).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 15_000
            readTimeout = 20_000
            setRequestProperty("Authorization", "Bearer $token")
            setRequestProperty("Accept", "application/json")
            if (body != null) {
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
            }
        }

        try {
            if (body != null) {
                conn.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
            }

            val code = conn.responseCode
            val text = readBody(conn, success = code in 200..299)
            if (code !in 200..299) throw HttpException(code, text)
            return if (text.isBlank()) JSONObject() else JSONObject(text)
        } finally {
            conn.disconnect()
        }
    }

    private fun readBody(conn: HttpURLConnection, success: Boolean): String {
        val stream = if (success) conn.inputStream else conn.errorStream
        return stream?.bufferedReader(Charsets.UTF_8)?.use(BufferedReader::readText).orEmpty()
    }
}
