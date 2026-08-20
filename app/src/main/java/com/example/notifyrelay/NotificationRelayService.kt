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

        val extras = sbn.notification.extras
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""

        // Skip empty/placeholder notifications (e.g. summary/group notifications with no content).
        if (title.isEmpty() && text.isEmpty()) return

        ApiSender.send(context, packageName, title, text)
    }
}
