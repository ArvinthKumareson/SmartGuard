package com.smartguard.app.viewmodel


import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Simple UI model representing a keyword row from Firestore.
 */
data class KeywordData(
    val id: String,
    val value: String,
    val explanation: String
)

/**
 * ViewModel for managing scam detection keywords in the admin panel.
 *
 * It listens to the Firestore "keywords" collection and exposes a live
 * list of [KeywordData], and provides operations to add and delete keywords.
 */
class KeywordViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private val _keywords = MutableStateFlow<List<KeywordData>>(emptyList())
    val keywords: StateFlow<List<KeywordData>> = _keywords

    init {
        // Subscribe to changes in the keyword collection so the UI stays fresh.
        db.collection("keywords").addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("KeywordViewModel", "Error listening to keywords: ${error.message}", error)
                return@addSnapshotListener
            }
            
            val list = snapshot?.documents?.mapNotNull {
                val value = it.getString("value")
                val explanation = it.getString("explanation") ?: "This keyword is commonly used in scam messages"
                if (value != null) KeywordData(it.id, value, explanation) else null
            } ?: emptyList()
            
            Log.d("KeywordViewModel", "Loaded ${list.size} keywords from Firestore")
            _keywords.value = list
        }
    }
    // Deprecated helper for bulk keyword upload (kept for migration scripts/testing).
    suspend fun addKeywordsBulk(keywords: List<String>) {
        val db = FirebaseFirestore.getInstance()
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val batch = db.batch()
        val keywordsRef = db.collection("keywords")

        keywords.forEach { keyword ->
            val docRef = keywordsRef.document() // auto-generated ID
            batch.set(docRef, mapOf(
                "value" to keyword,
                "addedBy" to uid,
                "timestamp" to FieldValue.serverTimestamp()
            ))
        }

        batch.commit().addOnSuccessListener {
            Log.d("KeywordUpload", "All keywords uploaded successfully.")
        }.addOnFailureListener {
            Log.e("KeywordUpload", "Failed to upload keywords: ${it.message}")
        }
    }


    /**
     * Adds a single keyword + explanation document to Firestore.
     */
    fun addKeyword(value: String, explanation: String = "") {
        if (value.isBlank()) return
        val finalExplanation = explanation.ifBlank { "This keyword is commonly used in scam messages" }
        
        Log.d("KeywordViewModel", "Adding keyword: $value with explanation: $finalExplanation")
        Log.d("KeywordViewModel", "Current user UID: $uid")
        
        db.collection("keywords").add(
            mapOf(
                "value" to value,
                "explanation" to finalExplanation,
                "addedBy" to uid,
                "timestamp" to FieldValue.serverTimestamp()
            )
        ).addOnSuccessListener { docRef ->
            Log.d("KeywordViewModel", "Keyword added successfully with ID: ${docRef.id}")
        }.addOnFailureListener { e ->
            Log.e("KeywordViewModel", "Failed to add keyword: ${e.message}", e)
        }
    }

    /**
     * Deletes the keyword document with the given id.
     */
    fun deleteKeyword(id: String) {
        db.collection("keywords").document(id).delete()
    }
}
