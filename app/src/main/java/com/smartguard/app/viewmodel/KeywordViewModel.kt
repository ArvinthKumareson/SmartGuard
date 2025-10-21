package com.smartguard.app.viewmodel


import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class KeywordViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private val _keywords = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val keywords: StateFlow<List<Pair<String, String>>> = _keywords

    init {
        db.collection("keywords").addSnapshotListener { snapshot, _ ->
            val list = snapshot?.documents?.mapNotNull {
                val value = it.getString("value")
                if (value != null) it.id to value else null
            } ?: emptyList()
            _keywords.value = list
        }
    }

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


    fun addKeyword(value: String) {
        if (value.isBlank()) return
        db.collection("keywords").add(
            mapOf(
                "value" to value,
                "addedBy" to uid,
                "timestamp" to FieldValue.serverTimestamp()
            )
        )
    }

    fun deleteKeyword(id: String) {
        db.collection("keywords").document(id).delete()
    }
}
