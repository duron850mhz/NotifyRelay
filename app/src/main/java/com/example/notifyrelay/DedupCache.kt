package com.example.notifyrelay

/**
 * Some apps (Outlook included) post the same notification content twice in quick
 * succession - e.g. a group-summary notification alongside the individual one, or an
 * initial post immediately followed by an update with identical text. This treats an
 * identical package+title+body combination seen again within [WINDOW_MS] as a duplicate.
 */
object DedupCache {
    private const val WINDOW_MS = 4000L
    private const val MAX_TRACKED = 200

    private val lastSent = HashMap<String, Long>()

    @Synchronized
    fun isDuplicate(key: String): Boolean {
        val now = System.currentTimeMillis()

        if (lastSent.size > MAX_TRACKED) {
            lastSent.entries.removeAll { now - it.value > WINDOW_MS }
        }

        val last = lastSent[key]
        if (last != null && now - last < WINDOW_MS) {
            return true
        }
        lastSent[key] = now
        return false
    }
}
