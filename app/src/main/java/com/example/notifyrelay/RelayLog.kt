package com.example.notifyrelay

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class LogEntry(
    val timestamp: Long,
    val packageName: String,
    val title: String,
    val body: String,
    val success: Boolean,
    val statusOrError: String
) {
    fun formattedTime(): String =
        SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(timestamp))
}

/**
 * Keeps recent send attempts in memory so MainActivity can display them.
 * This is intentionally not persisted to disk - it resets when the process
 * is killed, which is fine for a "did it just work?" style log.
 */
object RelayLog {
    // -1 = unlimited, 0 = keep nothing. Initialized from Prefs at app startup
    // (see NotifyRelayApp) and updated whenever the user changes it in Settings.
    private var maxEntries: Int = 100
    private val entries = mutableListOf<LogEntry>()
    private val listeners = mutableListOf<() -> Unit>()

    @Synchronized
    fun setMaxEntries(max: Int) {
        maxEntries = max
        trim()
        listeners.toList().forEach { it() }
    }

    @Synchronized
    fun add(entry: LogEntry) {
        if (maxEntries == 0) return
        entries.add(0, entry)
        trim()
        listeners.toList().forEach { it() }
    }

    private fun trim() {
        if (maxEntries < 0) return // unlimited
        while (entries.size > maxEntries) {
            entries.removeAt(entries.size - 1)
        }
    }

    @Synchronized
    fun getAll(): List<LogEntry> = entries.toList()

    @Synchronized
    fun addListener(listener: () -> Unit) {
        listeners.add(listener)
    }

    @Synchronized
    fun removeListener(listener: () -> Unit) {
        listeners.remove(listener)
    }
}
