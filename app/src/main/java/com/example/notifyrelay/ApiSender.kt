package com.example.notifyrelay

import android.content.Context
import android.os.Handler
import android.os.Looper
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

object ApiSender {
    private val client = OkHttpClient()
    private val retryHandler = Handler(Looper.getMainLooper())
    private const val RETRY_DELAY_MS = 2000L

    fun send(context: Context, packageName: String, title: String, body: String) {
        sendInternal(context, packageName, title, body, isRetry = false)
    }

    private fun sendInternal(
        context: Context,
        packageName: String,
        title: String,
        body: String,
        isRetry: Boolean
    ) {
        val url = Prefs.getApiUrl(context)
        if (url.isBlank()) {
            RelayLog.add(
                LogEntry(System.currentTimeMillis(), packageName, title, body, false, "APIのURLが未設定です")
            )
            return
        }

        val titleParam = Prefs.getParamTitle(context)
        val bodyParam = Prefs.getParamBody(context)

        val json = JSONObject()
        json.put(titleParam, title)
        json.put(bodyParam, body)

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = json.toString().toRequestBody(mediaType)

        val request = try {
            Request.Builder()
                .url(url)
                .post(requestBody)
                .build()
        } catch (e: IllegalArgumentException) {
            RelayLog.add(
                LogEntry(System.currentTimeMillis(), packageName, title, body, false, "URLが不正です: ${e.message}")
            )
            return
        }

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Transient network blips (DNS not yet resolved right after Wi-Fi/mobile
                // handoff, brief connectivity gap, etc.) are common and usually self-resolve
                // within a second or two. Retry once before logging it as a failure.
                if (!isRetry) {
                    retryHandler.postDelayed({
                        sendInternal(context, packageName, title, body, isRetry = true)
                    }, RETRY_DELAY_MS)
                } else {
                    RelayLog.add(
                        LogEntry(System.currentTimeMillis(), packageName, title, body, false, e.message ?: "通信エラー")
                    )
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val success = response.isSuccessful
                RelayLog.add(
                    LogEntry(System.currentTimeMillis(), packageName, title, body, success, "HTTP ${response.code}")
                )
                response.close()
            }
        })
    }
}
