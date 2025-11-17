package com.smartguard.app.data

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Helper around Firestore for saving and loading a user's scam message history.
 *
 * This is mainly used by the detection pipeline to push new suspicious
 * messages into the "users/{userId}/scamMessages" subcollection, and by
 * the history UI to read them back.
 */
class UserHistoryStore(private val context: Context) {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    /**
     * Persists a single [ScanRecord] to the current user's Firestore history.
     *
     * Note: the keyword explanations JSON is written by other parts of the
     * pipeline (e.g. NotificationListener) so it is not included here.
     */
    suspend fun save(record: ScanRecord) {
        val userId = auth.currentUser?.uid ?: return
        
        // Note: keyword_explanations will be added by DetectionEngine in NotificationListener
        // This save method is called from NotificationListener which needs to include explanations
        val doc = mapOf(
            "message" to record.message,
            "matched_keywords" to record.matchedKeywords.joinToString(","),
            "source_app" to record.sourceApp,
            "timestamp" to record.timestamp,
            "sender_name" to (record.senderName ?: ""),
            "conversation_title" to (record.conversationTitle ?: "")
        )

        firestore.collection("users")
            .document(userId)
            .collection("scamMessages")
            .add(doc)
    }

    /**
     * Loads all scam messages for the current user from Firestore.
     */
    suspend fun load(): List<ScanRecord> {
        val userId = auth.currentUser?.uid ?: return emptyList()

        val snapshot = firestore.collection("users")
            .document(userId)
            .collection("scamMessages")
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            try {
                ScanRecord(
                    message = doc.getString("message") ?: return@mapNotNull null,
                    matchedKeywords = doc.getString("matched_keywords")
                        ?.split(",")
                        ?.map { it.trim() }
                        ?.filter { it.isNotEmpty() }
                        ?: emptyList(),
                    sourceApp = doc.getString("source_app"),
                    timestamp = doc.getLong("timestamp") ?: 0L,
                    senderName = doc.getString("sender_name"),
                    conversationTitle = doc.getString("conversation_title")
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}
