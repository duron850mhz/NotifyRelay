package com.example.notifyrelay

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class NotificationRelayService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)

        val context = applicationContext

        // Global enable/disable toggle from the main screen.
        if (!Prefs.isEnabled(context)) return

        val packageName = sbn.packageName

        // Never forward our own notifications (avoids potential loops).
        if (packageName == context.packageName) return

        // Only forward notifications from apps the user selected in Settings.
        val selected = Prefs.getSelectedPackages(context)
        if (packageName !in selected) return

        // "Ongoing" notifications are typically progress/status updates (sync in progress,
        // "optimizing database...", downloads, etc.) that fire onNotificationPosted repeatedly
        // and rarely represent something worth relaying. Skip them unless the user opted out.
        if (Prefs.getSkipOngoing(context) && sbn.isOngoing) return

        // A group-summary notification is a rollup (e.g. "3 new emails"). When there's only
        // one item in the group it often carries the exact same title/text as the individual
        // notification, producing an identical-looking duplicate. Skip summaries entirely -
        // the individual notification always carries the real content.
        if ((sbn.notification.flags and Notification.FLAG_GROUP_SUMMARY) != 0) return

        val extras = sbn.notification.extras
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""

        // Skip empty/placeholder notifications (e.g. summary/group notifications with no content).
        if (title.isEmpty() && text.isEmpty()) return

        // Some apps post the same content twice in quick succession (initial post + an
        // update with identical text). Treat that as a duplicate and skip the second one.
        val dedupKey = "$packageName|$title|$text"
        if (DedupCache.isDuplicate(dedupKey)) return

        ApiSender.send(context, packageName, title, text)
    }
}
