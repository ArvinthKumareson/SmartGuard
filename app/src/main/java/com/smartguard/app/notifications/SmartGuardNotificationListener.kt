package com.smartguard.app.notifications

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.smartguard.app.data.DetectionEngine
import com.smartguard.app.data.EncryptedKeywords
import com.smartguard.app.data.ScanRecord
import com.smartguard.app.data.UserHistoryStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmartGuardNotificationListener : NotificationListenerService() {
    private val scope = CoroutineScope(Dispatchers.IO)
    private lateinit var engine: DetectionEngine

    override fun onCreate() {
        super.onCreate()
        val keywords = EncryptedKeywords.getKeywords(applicationContext)
        engine = DetectionEngine(keywords)
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
                val store = UserHistoryStore(applicationContext)
                store.save(
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
