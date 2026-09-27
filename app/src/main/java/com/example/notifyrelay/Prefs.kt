package com.example.notifyrelay

import android.content.Context
import android.content.SharedPreferences

object Prefs {
    private const val PREF_NAME = "notify_relay_prefs"

    private const val KEY_API_URL = "api_url"
    private const val KEY_PARAM_TITLE = "param_title"
    private const val KEY_PARAM_BODY = "param_body"
    private const val KEY_ENABLED = "enabled"
    private const val KEY_SELECTED_PACKAGES = "selected_packages"
    private const val KEY_LOG_LIMIT = "log_limit"
    private const val KEY_SKIP_ONGOING = "skip_ongoing"
    private const val KEY_EXCLUDE_KEYWORDS = "exclude_keywords"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun getApiUrl(context: Context): String =
        prefs(context).getString(KEY_API_URL, "") ?: ""

    fun setApiUrl(context: Context, url: String) {
        prefs(context).edit().putString(KEY_API_URL, url).apply()
    }

    fun getParamTitle(context: Context): String =
        prefs(context).getString(KEY_PARAM_TITLE, "title") ?: "title"

    fun setParamTitle(context: Context, name: String) {
        prefs(context).edit().putString(KEY_PARAM_TITLE, name).apply()
    }

    fun getParamBody(context: Context): String =
        prefs(context).getString(KEY_PARAM_BODY, "body") ?: "body"

    fun setParamBody(context: Context, name: String) {
        prefs(context).edit().putString(KEY_PARAM_BODY, name).apply()
    }

    fun isEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_ENABLED, false)

    fun setEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_ENABLED, enabled).apply()
    }

    fun getSelectedPackages(context: Context): MutableSet<String> =
        HashSet(prefs(context).getStringSet(KEY_SELECTED_PACKAGES, emptySet()) ?: emptySet())

    fun setSelectedPackages(context: Context, packages: Set<String>) {
        prefs(context).edit().putStringSet(KEY_SELECTED_PACKAGES, packages).apply()
    }

    /** Max number of log entries to keep in memory. -1 = unlimited, 0 = don't keep any. */
    fun getLogLimit(context: Context): Int =
        prefs(context).getInt(KEY_LOG_LIMIT, 100)

    fun setLogLimit(context: Context, limit: Int) {
        prefs(context).edit().putInt(KEY_LOG_LIMIT, limit).apply()
    }

    /** Whether to skip "ongoing" (progress/status) notifications, e.g. sync/optimize progress. */
    fun getSkipOngoing(context: Context): Boolean =
        prefs(context).getBoolean(KEY_SKIP_ONGOING, true)

    fun setSkipOngoing(context: Context, skip: Boolean) {
        prefs(context).edit().putBoolean(KEY_SKIP_ONGOING, skip).apply()
    }

    /** Raw newline-separated keyword list, as typed in Settings. */
    fun getExcludeKeywordsRaw(context: Context): String =
        prefs(context).getString(KEY_EXCLUDE_KEYWORDS, "") ?: ""

    fun setExcludeKeywordsRaw(context: Context, raw: String) {
        prefs(context).edit().putString(KEY_EXCLUDE_KEYWORDS, raw).apply()
    }

    /** Parsed, non-blank keywords. A notification whose title+body contains any of these is skipped. */
    fun getExcludeKeywords(context: Context): List<String> =
        getExcludeKeywordsRaw(context)
            .split("\n")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
}
