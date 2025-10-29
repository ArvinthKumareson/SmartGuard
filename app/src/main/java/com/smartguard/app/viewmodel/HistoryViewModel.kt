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
import com.smartguard.app.db.AppDatabase
import com.smartguard.app.db.ScanRecordEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class ScanResult(
    val message: String,
    val matchedKeywords: List<String>,
    val keywordExplanations: Map<String, String> = emptyMap(),
    val sourceApp: String,
    val timestamp: Long,
    val senderName: String? = null,
    val conversationTitle: String? = null
)

class HistoryViewModel(app: Application) : AndroidViewModel(app) {

    private val db = AppDatabase.getInstance(app.applicationContext)
    private val appContext = app.applicationContext
    private val engine = DetectionEngine(appContext)
    private val firestore = FirebaseFirestore.getInstance()

    // Local scan history scoped to current user
    val history: StateFlow<List<ScanResult>> = flow {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        emitAll(db.scanRecordDao().recentForUser(userId))
    }
        .map { entities ->
            entities.mapNotNull { entity ->
                val safeKeywords = entity.matchedKeywords
                    ?.split(",")
                    ?.map { it.trim() }
                    ?.filter { it.isNotEmpty() }
                    ?: emptyList()

                val explanations = try {
                    entity.keywordExplanations?.let {
                        com.google.gson.Gson().fromJson<Map<String, String>>(
                            it,
                            object : com.google.gson.reflect.TypeToken<Map<String, String>>() {}.type
                        )
                    } ?: emptyMap()
                } catch (e: Exception) {
                    emptyMap()
                }

                ScanResult(
                    message = entity.message.ifBlank { "No message" },
                    matchedKeywords = safeKeywords,
                    keywordExplanations = explanations,
                    sourceApp = entity.sourceApp ?: "Unknown",
                    timestamp = entity.timestamp,
                    senderName = entity.senderName,
                    conversationTitle = entity.conversationTitle
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Cloud scan history from Firestore
    val cloudHistory: StateFlow<List<ScanResult>> = flow {
        try {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@flow
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("scamMessages")
                .get()
                .await()

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

            emit(results)
        } catch (e: Exception) {
            Log.e("SmartGuard", "Failed to load cloud history", e)
            emit(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Combined scan history - ensure newest first
    val fullHistory: StateFlow<List<ScanResult>> = combine(history, cloudHistory) { local, cloud ->
        (local + cloud)
            .distinctBy { it.message + it.timestamp }
            .sortedByDescending { it.timestamp }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val smsOnly: StateFlow<List<ScanResult>> = fullHistory
        .map { list -> list.filter { it.sourceApp == "SMS" } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val riskyMessages: StateFlow<List<ScanResult>> = fullHistory
        .map { list -> list.filter { it.matchedKeywords.size >= 2 } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Centralized record insertion for real scans
    fun addRecord(message: String, source: String) {
        viewModelScope.launch {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
            val matchedKeywords = engine.scanWithExplanations(message)
            val keywordsList = matchedKeywords.map { it.keyword }
            val explanationsMap = matchedKeywords.associate { it.keyword to it.explanation }
            val explanationsJson = com.google.gson.Gson().toJson(explanationsMap)
            
            val entity = ScanRecordEntity(
                message = message,
                matchedKeywords = keywordsList.joinToString(","),
                keywordExplanations = explanationsJson,
                sourceApp = source,
                timestamp = System.currentTimeMillis(),
                userId = userId
            )
            db.scanRecordDao().insert(entity)
            syncToCloud(userId, entity)
        }
    }

    // Firestore sync per user
    private fun syncToCloud(userId: String, entity: ScanRecordEntity) {
        val doc = mapOf(
            "message" to entity.message,
            "matched_keywords" to entity.matchedKeywords,
            "keyword_explanations" to entity.keywordExplanations,
            "source_app" to entity.sourceApp,
            "timestamp" to entity.timestamp
        )
        firestore.collection("users")
            .document(userId)
            .collection("scamMessages")
            .add(doc)
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
    
    // Force refresh the history data
    fun refreshHistory() {
        viewModelScope.launch {
            // Force refresh by re-emitting the current data
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            // Trigger a refresh by calling the flow again
            history.value // This will trigger the flow to re-emit
            cloudHistory.value // This will trigger the cloud flow to re-emit
        }
    }
}
