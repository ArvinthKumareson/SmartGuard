package com.smartguard.app.notifications

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
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
        for (key in extras.keySet()) {
            Log.d("SmartGuardNotif", "Extra[$key] = ${extras.get(key)}")
        }



        // WhatsApp
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
        val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()

        val fullText = listOfNotNull(title, text, bigText, subText)
            .joinToString(" ")
            .trim()

        Log.d("SmartGuardNotif", "Notification from ${sbn.packageName}")
        Log.d("SmartGuardNotif", "Extracted text: $fullText")
// location change


        if (fullText.isBlank()) return

        val matches = engine.scan(fullText)
        if (matches.isNotEmpty()) {
            scope.launch {
                val store = UserHistoryStore(applicationContext)
                store.save(
                    ScanRecord(
                        message = fullText,
                        matchedKeywords = matches,
                        sourceApp = sbn.packageName,
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
        }
    }
}
