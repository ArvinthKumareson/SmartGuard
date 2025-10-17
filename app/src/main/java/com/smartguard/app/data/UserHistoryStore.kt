package com.smartguard.app.data

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserHistoryStore(private val context: Context) {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun save(record: ScanRecord) {
        val userId = auth.currentUser?.uid ?: return

        val doc = mapOf(
            "message" to record.message,
            "matched_keywords" to record.matchedKeywords,
            "source_app" to record.sourceApp,
            "timestamp" to record.timestamp
        )

        firestore.collection("users")
            .document(userId)
            .collection("scamMessages")
            .add(doc)
    }

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
                    matchedKeywords = doc.getString("matched_keywords")?.split(",")?.map { it.trim() } ?: emptyList(),
                    sourceApp = doc.getString("source_app"),
                    timestamp = doc.getLong("timestamp") ?: 0L
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}
