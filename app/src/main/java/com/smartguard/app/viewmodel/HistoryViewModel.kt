package com.smartguard.app.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.smartguard.app.data.DetectionEngine
import com.smartguard.app.data.KeywordRepository
import com.smartguard.app.data.ScanRecord
import com.smartguard.app.db.AppDatabase
import com.smartguard.app.db.ScanRecordEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class ScanResult(
    val message: String,
    val matchedKeywords: List<String>,
    val sourceApp: String,
    val timestamp: Long
)

class HistoryViewModel(app: Application) : AndroidViewModel(app) {

    private val db = AppDatabase.getInstance(app.applicationContext)
    private val engine = DetectionEngine(KeywordRepository.getKeywords())
    private val firestore = FirebaseFirestore.getInstance()

    // ✅ Local scan history scoped to current user
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

                ScanResult(
                    message = entity.message.ifBlank { "No message" },
                    matchedKeywords = safeKeywords,
                    sourceApp = entity.sourceApp ?: "Unknown",
                    timestamp = entity.timestamp
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val smsOnly: StateFlow<List<ScanResult>> = history
        .map { list -> list.filter { it.sourceApp == "SMS" } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val riskyMessages: StateFlow<List<ScanResult>> = history
        .map { list -> list.filter { it.matchedKeywords.size >= 2 } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ✅ Centralized record insertion for real scans
    fun addRecord(message: String, source: String) {
        viewModelScope.launch {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
            val matches = engine.scan(message)
            val entity = ScanRecordEntity(
                message = message,
                matchedKeywords = matches.joinToString(","),
                sourceApp = source,
                timestamp = System.currentTimeMillis(),
                userId = userId
            )
            db.scanRecordDao().insert(entity)
            syncToCloud(userId, entity)
        }
    }

    // ✅ Firestore sync per user
    private fun syncToCloud(userId: String, entity: ScanRecordEntity) {
        val doc = mapOf(
            "message" to entity.message,
            "matched_keywords" to entity.matchedKeywords,
            "source_app" to entity.sourceApp,
            "timestamp" to entity.timestamp
        )
        firestore.collection("users")
            .document(userId)
            .collection("scamMessages")
            .add(doc)
    }

    // 🧪 Optional: Demo seeding for debug mode
    fun seedDemoData() {
        viewModelScope.launch {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
            val demoMessages = listOf(
                "Your parcel is pending a small fee. Pay now to avoid return: short.link/xyz",
                "Bank alert: unusual login detected. Verify account within 24 hours.",
                "Congrats! You won a prize. Claim now via gift card."
            )

            demoMessages.forEach { msg ->
                addRecord(msg, "Demo") // ✅ Reuse addRecord for consistency
            }
        }
    }

    // 🧪 Optional: Load cloud history if needed later
    fun loadCloudHistory(onResult: (List<ScanResult>) -> Unit) {
        viewModelScope.launch {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
            try {
                val snapshot = firestore.collection("users")
                    .document(userId)
                    .collection("scamMessages")
                    .get()
                    .await()

                val results = snapshot.documents.mapNotNull { doc ->
                    ScanResult(
                        message = doc.getString("message") ?: return@mapNotNull null,
                        matchedKeywords = doc.getString("matched_keywords")?.split(",")?.map { it.trim() } ?: emptyList(),
                        sourceApp = doc.getString("source_app") ?: "Unknown",
                        timestamp = doc.getLong("timestamp") ?: 0L
                    )
                }
                onResult(results)
            } catch (e: Exception) {
                onResult(emptyList())
            }
        }
    }
}
