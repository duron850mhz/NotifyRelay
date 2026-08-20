package com.example.notifyrelay

import android.app.Application

class NotifyRelayApp : Application() {
    override fun onCreate() {
        super.onCreate()
        RelayLog.setMaxEntries(Prefs.getLogLimit(this))
    }
}
