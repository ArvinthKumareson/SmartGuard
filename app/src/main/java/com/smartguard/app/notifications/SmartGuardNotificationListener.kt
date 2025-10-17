package com.smartguard.app.notifications

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.smartguard.app.data.DetectionEngine
import com.smartguard.app.data.HistoryStore
import com.smartguard.app.data.KeywordRepository
import com.smartguard.app.data.ScanRecord
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmartGuardNotificationListener : NotificationListenerService() {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val engine = DetectionEngine(KeywordRepository.getKeywords())
    private lateinit var store: HistoryStore

    override fun onCreate() {
        super.onCreate()
        store = HistoryStore(applicationContext)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return
        val extras = sbn.notification.extras
        val text = listOfNotNull(
            extras.getCharSequence(android.app.Notification.EXTRA_TEXT)?.toString(),
            extras.getCharSequence(android.app.Notification.EXTRA_BIG_TEXT)?.toString()
        ).joinToString(" ").trim()
        if (text.isBlank()) return

        val matches = engine.scan(text)
        if (matches.isNotEmpty()) {
            scope.launch {
                store.add(
                    ScanRecord(
                        message = text,
                        matchedKeywords = matches,
                        sourceApp = sbn.packageName,
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
        }
    }
}