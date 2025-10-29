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
    private val processedNotifications = mutableSetOf<String>()

    override fun onCreate() {
        super.onCreate()
        engine = DetectionEngine(applicationContext)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return

        val extras = sbn.notification.extras
        
        // For WhatsApp, be more selective about what we process
        if (sbn.packageName == "com.whatsapp") {
            // Skip if this is a group notification without actual message content
            val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
            val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
            val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString() ?: ""
            
            // Skip notifications that are just "X messages" or similar
            if (title.contains("messages") && !text.contains(":") && !bigText.contains(":")) {
                Log.d("SmartGuardNotif", "Skipping WhatsApp group summary notification")
                return
            }
            
            // Skip if this looks like a read receipt or status update
            if (text.contains("read") || text.contains("delivered") || text.contains("typing")) {
                Log.d("SmartGuardNotif", "Skipping WhatsApp status notification: $text")
                return
            }
        }

        // Create a unique identifier for this notification to prevent duplicates
        val notificationId = "${sbn.packageName}_${sbn.postTime}_${sbn.id}"
        
        // Skip if we've already processed this notification
        if (processedNotifications.contains(notificationId)) {
            Log.d("SmartGuardNotif", "Skipping duplicate notification: $notificationId")
            return
        }

        // Only process notifications that are actually new messages
        // Skip notifications that are just status updates or read receipts
        val isNewMessage = extras.getBoolean(Notification.EXTRA_SHOW_WHEN, true) && 
                          !extras.getBoolean("android.showChronometer", false)
        
        if (!isNewMessage) {
            Log.d("SmartGuardNotif", "Skipping non-message notification from ${sbn.packageName}")
            return
        }

        // Extract sender information
        val senderName = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: "Unknown Sender"
        val conversationTitle = extras.getCharSequence(Notification.EXTRA_CONVERSATION_TITLE)?.toString()
        
        // Extract message content
        val messageText = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
        val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()

        // For WhatsApp, extract individual messages from MessagingStyle
        val messages = extras.getParcelableArray(Notification.EXTRA_MESSAGES)
        val messageContents = mutableListOf<String>()
        
        if (messages != null && messages.isNotEmpty()) {
            // Process each message individually
            for (message in messages) {
                val bundle = message as? android.os.Bundle
                val messageText = bundle?.getCharSequence("text")?.toString()
                if (!messageText.isNullOrBlank()) {
                    messageContents.add(messageText)
                }
            }
        } else {
            // Fallback to standard notification text
            val content = messageText ?: bigText ?: subText
            if (!content.isNullOrBlank()) {
                messageContents.add(content)
            }
        }

        // Process each message separately
        messageContents.forEach { messageContent ->
            if (messageContent.isBlank()) return@forEach

            // For WhatsApp, create a more specific content-based ID to prevent duplicates
            val contentId = if (sbn.packageName == "com.whatsapp") {
                "${sbn.packageName}_${senderName}_${messageContent.hashCode()}_${System.currentTimeMillis() / 10000}" // 10-second window
            } else {
                notificationId
            }

            // Skip if we've already processed this exact content recently
            if (processedNotifications.contains(contentId)) {
                Log.d("SmartGuardNotif", "Skipping duplicate content: $contentId")
                return@forEach
            }

            Log.d("SmartGuardNotif", "Processing notification from ${sbn.packageName}")
            Log.d("SmartGuardNotif", "Sender: $senderName")
            Log.d("SmartGuardNotif", "Message: $messageContent")

            val matchedKeywords = engine.scanWithExplanations(messageContent)
            if (matchedKeywords.isNotEmpty()) {
                // Mark this content as processed
                processedNotifications.add(contentId)
                
                // Clean up old processed notifications to prevent memory leaks
                if (processedNotifications.size > 100) {
                    processedNotifications.clear()
                }
                
                scope.launch {
                    val keywordsList = matchedKeywords.map { it.keyword }
                    val store = UserHistoryStore(applicationContext)
                    
                    // Create a properly formatted message with sender info
                    val formattedMessage = if (conversationTitle != null && conversationTitle != senderName) {
                        "[$conversationTitle] $senderName: $messageContent"
                    } else {
                        "$senderName: $messageContent"
                    }
                    
                    store.save(
                        ScanRecord(
                            message = formattedMessage,
                            matchedKeywords = keywordsList,
                            sourceApp = sbn.packageName,
                            timestamp = System.currentTimeMillis(),
                            senderName = senderName,
                            conversationTitle = conversationTitle
                        )
                    )
                }
            }
        }
    }
}
