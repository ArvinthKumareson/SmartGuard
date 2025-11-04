package com.smartguard.app.notifications

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.smartguard.app.data.DetectionEngine
import com.smartguard.app.data.EncryptedKeywords
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SmartGuardNotificationListener : NotificationListenerService() {

    private val scope = CoroutineScope(Dispatchers.IO)
    private lateinit var engine: DetectionEngine
    private val processedNotifications = mutableSetOf<String>()
    
    private fun getAppDisplayName(packageName: String): String {
        return when (packageName) {
            "com.whatsapp" -> "WhatsApp"
            "org.telegram.messenger" -> "Telegram"
            "com.android.mms" -> "Messages"
            "android.provider.Telephony.SMS_RECEIVED" -> "SMS"
            else -> packageName.substringAfterLast('.').replaceFirstChar { it.uppercase() }
        }
    }

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
        var senderName = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: "Unknown Sender"
        val conversationTitle = extras.getCharSequence(Notification.EXTRA_CONVERSATION_TITLE)?.toString()
        Log.d("SmartGuardNotif", "Initial title/sender: $senderName")
        
        // Check if this is SMS (any SMS/messaging app)
        val isSMS = sbn.packageName == "android.provider.Telephony.SMS_RECEIVED" || 
                    sbn.packageName == "com.android.mms" ||
                    sbn.packageName == "com.google.android.apps.messaging" ||
                    sbn.packageName == "com.samsung.android.messaging" ||
                    sbn.packageName.contains("messaging") ||
                    sbn.packageName.contains("sms") ||
                    senderName.matches(Regex(".*\\+?[0-9\\s\\-\\(\\)]{7,}.*")) // Phone number in title
        
        if (isSMS) {
            val phoneRegex = Regex("\\+?[0-9\\s\\-\\(\\)]{7,}")
            var phoneMatch = phoneRegex.find(senderName)
            
            // If no phone in title, try extracting from message text
            if (phoneMatch == null) {
                val messageText = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
                phoneMatch = phoneRegex.find(messageText)
                Log.d("SmartGuardNotif", "Searching in message text: $messageText, found: ${phoneMatch?.value}")
            }
            
            if (phoneMatch != null) {
                senderName = phoneMatch.value.trim()
                Log.d("SmartGuardNotif", "Extracted phone: $senderName")
            } else {
                senderName = "SMS"
                Log.d("SmartGuardNotif", "No phone found, using SMS fallback")
            }
        } else {
            // For other apps, use app display name
            senderName = getAppDisplayName(sbn.packageName)
        }
        
        // Extract message content
        val messageText = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
        val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()

        // Only process the latest message from the notification thread (not all historical messages)
        val messages = extras.getParcelableArray(Notification.EXTRA_MESSAGES)
        val messageContents = mutableListOf<String>()
        
        if (messages != null && messages.isNotEmpty()) {
            // Only process the LATEST message (last one in array)
            val lastMessage = messages.last()
            val bundle = lastMessage as? android.os.Bundle
            val messageText = bundle?.getCharSequence("text")?.toString()
            if (!messageText.isNullOrBlank()) {
                messageContents.add(messageText)
            }
        }
        
        // If no message from array, fallback to standard notification text
        if (messageContents.isEmpty()) {
            val content = messageText ?: bigText ?: subText
            if (!content.isNullOrBlank()) {
                messageContents.add(content)
            }
        }

        // Process each message separately
        messageContents.forEach { messageContent ->
            if (messageContent.isBlank()) return@forEach
            
            // Skip system messages
            if (messageContent.contains("doing work") || messageContent.contains("background") ||
                messageContent.contains("notification") || messageContent.lowercase().contains("system")) {
                Log.d("SmartGuardNotif", "Skipping system message: $messageContent")
                return@forEach
            }

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

            // Refresh keywords before scanning (in case admin added new ones) - do in background
            scope.launch {
                try {
                    Log.d("SmartGuardNotif", "Starting keyword sync...")
                    val keywordsBefore = EncryptedKeywords.getKeywords(applicationContext)
                    Log.d("SmartGuardNotif", "Keywords BEFORE sync: ${keywordsBefore.size} - ${keywordsBefore.joinToString()}")
                    
                    EncryptedKeywords.syncFromFirestore(applicationContext)
                    Log.d("SmartGuardNotif", "Keywords refreshed from Firestore")
                    
                    val keywordsAfter = EncryptedKeywords.getKeywords(applicationContext)
                    Log.d("SmartGuardNotif", "Keywords AFTER sync: ${keywordsAfter.size} - ${keywordsAfter.joinToString()}")
                    
                    // Now scan with fresh keywords
                    Log.d("SmartGuardNotif", "About to scan message: '$messageContent'")
                    val currentKeywords = EncryptedKeywords.getKeywords(applicationContext)
                    Log.d("SmartGuardNotif", "Current keywords available for scan: ${currentKeywords.size} - ${currentKeywords.joinToString()}")
                    
                    val matchedKeywords = engine.scanWithExplanations(messageContent)
                    Log.d("SmartGuardNotif", "Scan result: ${matchedKeywords.size} matches found - ${matchedKeywords.map { it.keyword }}")
                    
                    if (matchedKeywords.isNotEmpty()) {
                        Log.d("SmartGuardNotif", "FINAL sender_name before save: '$senderName'")
                        Log.d("SmartGuardNotif", "Matched ${matchedKeywords.size} keywords: ${matchedKeywords.map { it.keyword }}")
                        // Mark this content as processed
                        processedNotifications.add(contentId)
                        
                        // Clean up old processed notifications to prevent memory leaks
                        if (processedNotifications.size > 100) {
                            processedNotifications.clear()
                        }
                        
                        try {
                            val keywordsList = matchedKeywords.map { it.keyword }
                            val explanationsMap = matchedKeywords.associate { it.keyword to it.explanation }
                            val explanationsJson = com.google.gson.Gson().toJson(explanationsMap)
                            val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
                            val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                            
                            // Check if similar message already exists (prevent SMS/Messages duplicates)
                            val existingQuery = firestore.collection("users")
                                .document(userId)
                                .collection("scamMessages")
                                .whereEqualTo("message", messageContent)
                                .limit(1)
                                .get()
                                .await()
                            
                            // Save or update the message
                            val doc = mapOf(
                                "message" to messageContent,
                                "matched_keywords" to keywordsList.joinToString(","),
                                "keyword_explanations" to explanationsJson,
                                "source_app" to sbn.packageName,
                                "timestamp" to System.currentTimeMillis(),
                                "sender_name" to senderName,
                                "conversation_title" to (conversationTitle ?: "")
                            )
                            
                            Log.d("SmartGuardNotif", "Doc to save: sender_name='$senderName'")
                            
                            if (existingQuery.isEmpty) {
                                // New message - add it
                                Log.d("SmartGuardNotif", "Saving new message - sender_name: '$senderName', message: '$messageContent'")
                                firestore.collection("users")
                                    .document(userId)
                                    .collection("scamMessages")
                                    .add(doc)
                                    .await()
                                Log.d("SmartGuardNotif", "Successfully saved new message with sender: '$senderName'")
                            } else {
                                // Existing message - update it with new sender_name if available
                                val existingDoc = existingQuery.documents.first()
                                Log.d("SmartGuardNotif", "Updating existing message - sender_name: '$senderName'")
                                firestore.collection("users")
                                    .document(userId)
                                    .collection("scamMessages")
                                    .document(existingDoc.id)
                                    .set(doc)
                                    .await()
                                Log.d("SmartGuardNotif", "Successfully updated message with sender: '$senderName'")
                            }
                        } catch (e: Exception) {
                            Log.e("SmartGuardNotif", "Error saving to Firestore", e)
                        }
                    }
                } catch (e: Exception) {
                    Log.d("SmartGuardNotif", "Error syncing keywords or processing message: ${e.message}")
                }
            }
        }
    }
}
