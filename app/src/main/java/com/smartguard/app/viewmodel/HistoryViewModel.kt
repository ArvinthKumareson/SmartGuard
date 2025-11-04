package com.smartguard.app.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.smartguard.app.data.DetectionEngine
import com.smartguard.app.data.EncryptedKeywords
import com.smartguard.app.data.ScanRecord
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.tasks.await

data class ScanResult(
    val id: Long = 0,
    val message: String,
    val matchedKeywords: List<String>,
    val keywordExplanations: Map<String, String> = emptyMap(),
    val sourceApp: String,
    val timestamp: Long,
    val senderName: String? = null,
    val conversationTitle: String? = null
)

class HistoryViewModel(app: Application) : AndroidViewModel(app) {

    private val appContext = app.applicationContext
    private val engine = DetectionEngine(appContext)
    private val firestore = FirebaseFirestore.getInstance()
    private val db = com.smartguard.app.db.AppDatabase.getInstance(app.applicationContext)
    
    // Clear local database on startup (we use Firestore only now)
    init {
        // Clear database synchronously before listeners start
        try {
            db.clearAllTables()
            Log.d("HistoryViewModel", "Cleared local database on startup")
        } catch (e: Exception) {
            Log.e("HistoryViewModel", "Error clearing local database", e)
        }
    }

    // Cloud scan history from Firestore with real-time listener (single source of truth)
    val fullHistory: StateFlow<List<ScanResult>> = callbackFlow {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            send(emptyList())
            close()
            return@callbackFlow
        }

        val listener = firestore.collection("users")
            .document(userId)
            .collection("scamMessages")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("SmartGuard", "Failed to load history from Firestore", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val results = snapshot.documents.mapNotNull { doc ->
                        try {
                            val explanations = try {
                                doc.getString("keyword_explanations")?.let {
                                    com.google.gson.Gson().fromJson<Map<String, String>>(
                                        it,
                                        object : com.google.gson.reflect.TypeToken<Map<String, String>>() {}.type
                                    )
                                } ?: emptyMap()
                            } catch (e: Exception) {
                                emptyMap()
                            }

                            ScanResult(
                                id = doc.id.hashCode().toLong(),
                                message = doc.getString("message") ?: return@mapNotNull null,
                                matchedKeywords = doc.getString("matched_keywords")
                                    ?.split(",")
                                    ?.map { it.trim() }
                                    ?.filter { it.isNotEmpty() }
                                    ?: emptyList(),
                                keywordExplanations = explanations,
                                sourceApp = doc.getString("source_app") ?: "Unknown",
                                timestamp = doc.getLong("timestamp") ?: 0L,
                                senderName = doc.getString("sender_name"),
                                conversationTitle = doc.getString("conversation_title")
                            )
                        } catch (e: Exception) {
                            Log.e("SmartGuard", "Invalid Firestore doc: ${doc.id}", e)
                            null
                        }
                    }
                    
                    // Deduplicate by message content only (case-insensitive), keep most recent
                    // Group messages by content, then pick the one with the best sender info
                    val deduplicated = results
                        .groupBy { it.message.lowercase() }
                        .map { (_, messages) ->
                            // For each group of identical messages, pick the one with sender info
                            messages.sortedWith(compareByDescending<ScanResult> { it.timestamp }
                                .thenByDescending { !it.senderName.isNullOrBlank() })
                                .first()
                        }
                        .sortedByDescending { it.timestamp }
                    trySend(deduplicated)
                }
            }

        awaitClose { listener.remove() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val smsOnly: StateFlow<List<ScanResult>> = fullHistory
        .map { list -> list.filter { it.sourceApp == "SMS" } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val riskyMessages: StateFlow<List<ScanResult>> = fullHistory
        .map { list -> list.filter { it.matchedKeywords.size >= 2 } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Centralized record insertion for real scans (Firestore only)
    fun addRecord(message: String, source: String) {
        viewModelScope.launch {
            try {
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
                val matchedKeywords = engine.scanWithExplanations(message)
                val explanationsMap = matchedKeywords.associate { it.keyword to it.explanation }
                val explanationsJson = com.google.gson.Gson().toJson(explanationsMap)
                
                val doc = mapOf(
                    "message" to message,
                    "matched_keywords" to matchedKeywords.map { it.keyword }.joinToString(","),
                    "keyword_explanations" to explanationsJson,
                    "source_app" to source,
                    "timestamp" to System.currentTimeMillis()
                )
                firestore.collection("users")
                    .document(userId)
                    .collection("scamMessages")
                    .add(doc)
            } catch (e: Exception) {
                Log.e("HistoryViewModel", "Error adding record", e)
            }
        }
    }

    // Demo seeding for debug mode
    fun seedDemoData() {
        viewModelScope.launch {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
            val demoMessages = listOf(
                "Your parcel is pending a small fee. Pay now to avoid return: short.link/xyz",
                "Bank alert: unusual login detected. Verify account within 24 hours.",
                "Congrats! You won a prize. Claim now via gift card."
            )

            demoMessages.forEach { msg ->
                addRecord(msg, "Demo")
            }
        }
    }
    
    // Delete a history item from Firestore - we need to find by message/timestamp
    fun deleteHistoryItem(id: Long) {
        viewModelScope.launch {
            try {
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
                val items = fullHistory.value
                val itemToDelete = items.find { it.id == id }
                
                if (itemToDelete != null) {
                    // Query for documents matching this message and timestamp
                    val snapshot = firestore.collection("users")
                        .document(userId)
                        .collection("scamMessages")
                        .whereEqualTo("message", itemToDelete.message)
                        .whereEqualTo("timestamp", itemToDelete.timestamp)
                        .get()
                        .await()
                    
                    // Delete the found document(s)
                    for (doc in snapshot.documents) {
                        firestore.collection("users")
                            .document(userId)
                            .collection("scamMessages")
                            .document(doc.id)
                            .delete()
                            .await()
                    }
                    Log.d("HistoryViewModel", "Successfully deleted item from Firestore")
                } else {
                    Log.w("HistoryViewModel", "Item not found for id: $id")
                }
            } catch (e: Exception) {
                Log.e("HistoryViewModel", "Error deleting history item", e)
            }
        }
    }
    
    // Clear all history data for current user (called on logout)
    fun clearUserHistory() {
        viewModelScope.launch {
            try {
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
                val snapshot = firestore.collection("users")
                    .document(userId)
                    .collection("scamMessages")
                    .get()
                    .await()
                
                for (doc in snapshot.documents) {
                    firestore.collection("users")
                        .document(userId)
                        .collection("scamMessages")
                        .document(doc.id)
                        .delete()
                        .await()
                }
                Log.d("HistoryViewModel", "Cleared all history for user: $userId from Firestore")
            } catch (e: Exception) {
                Log.e("HistoryViewModel", "Error clearing user history", e)
            }
        }
    }
    
    // Force refresh the history data
    fun refreshHistory() {
        viewModelScope.launch {
            // Force refresh by re-emitting the current data
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            // Trigger a refresh by calling the flow again
        }
    }
    
    // Clean up old messages without sender_name (one-time cleanup for old data)
    private fun cleanupOldMessagesOnce() {
        viewModelScope.launch {
            try {
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
                val snapshot = firestore.collection("users")
                    .document(userId)
                    .collection("scamMessages")
                    .get()
                    .await()
                
                var deletedCount = 0
                for (doc in snapshot.documents) {
                    val senderName = doc.getString("sender_name")
                    // Delete if sender_name is missing or empty (old incomplete messages)
                    if (senderName.isNullOrBlank()) {
                        firestore.collection("users")
                            .document(userId)
                            .collection("scamMessages")
                            .document(doc.id)
                            .delete()
                            .await()
                        deletedCount++
                        Log.d("HistoryViewModel", "Deleted old message without sender_name: ${doc.id}")
                    }
                }
                if (deletedCount > 0) {
                    Log.d("HistoryViewModel", "Cleanup completed: deleted $deletedCount old messages")
                }
            } catch (e: Exception) {
                Log.e("HistoryViewModel", "Error cleaning up old messages", e)
            }
        }
    }
    
    // Manual function to clear all old messages (for testing/reset)
    fun clearAllMessages() {
        viewModelScope.launch {
            try {
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
                val snapshot = firestore.collection("users")
                    .document(userId)
                    .collection("scamMessages")
                    .get()
                    .await()
                
                for (doc in snapshot.documents) {
                    firestore.collection("users")
                        .document(userId)
                        .collection("scamMessages")
                        .document(doc.id)
                        .delete()
                        .await()
                }
                Log.d("HistoryViewModel", "Cleared all messages for user: $userId")
            } catch (e: Exception) {
                Log.e("HistoryViewModel", "Error clearing all messages", e)
            }
        }
    }
}
