package com.smartguard.app.data

import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.tasks.await

// Model for storing keyword and its explanation
data class KeywordInfo(
    val keyword: String,
    val explanation: String
)

// Manages encrypted storage and syncing of scam detection keywords
// Keywords are stored locally in encrypted SharedPreferences and synced from Firestore
object EncryptedKeywords {
    private const val PREFS_NAME = "smartguard_encrypted_prefs"
    private const val KEYWORDS_KEY = "keywords"
    private const val KEYWORDS_MAP_KEY = "keywords_map"
    private val gson = Gson()
    // Listens for real-time keyword updates from Firestore
    private var keywordListener: ListenerRegistration? = null

    // Returns set of current keywords from encrypted storage
    fun getKeywords(context: Context): Set<String> {
        val keywordsMap = getKeywordsMap(context)
        return keywordsMap.keys
    }

    // Retrieves map of keywords to their explanations from encrypted storage
    fun getKeywordsMap(context: Context): Map<String, String> {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val prefs = EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        val json = prefs.getString(KEYWORDS_MAP_KEY, null)
        val map: Map<String, String> = if (json != null) {
            val type = object : TypeToken<Map<String, String>>() {}.type
            gson.fromJson(json, type) ?: emptyMap()
        } else {
            emptyMap()
        }
        
        Log.d("EncryptedKeywords", "getKeywordsMap returned ${map.size} keywords: ${map.keys.joinToString()}")
        return map
    }

    // Manually syncs keywords from Firestore to local encrypted storage
    // Called on login and when message is received to ensure fresh keywords
    suspend fun syncFromFirestore(context: Context) {
        val snapshot = FirebaseFirestore.getInstance().collection("keywords").get().await()
        // Extract keywords and explanations from Firestore documents
        val firestoreKeywords = snapshot.documents.associate {
            val keyword = it.getString("value") ?: return@associate null to null
            val explanation = it.getString("explanation") ?: "This keyword is commonly used in scam messages"
            keyword to explanation
        }.filterKeys { it != null } as Map<String, String>
        
        Log.d("EncryptedKeywords", "Synced ${firestoreKeywords.size} keywords from Firestore")
        // Save to encrypted storage for offline use
        saveKeywordsMap(context, firestoreKeywords)
    }

    // Sets up real-time listener for keyword updates from Firestore
    // Keywords are automatically updated in local storage whenever they change remotely
    fun startRealtimeSync(context: Context) {
        // Remove existing listener if any
        keywordListener?.remove()
        
        keywordListener = FirebaseFirestore.getInstance()
            .collection("keywords")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("EncryptedKeywords", "Realtime sync error: ${error.message}", error)
                    return@addSnapshotListener
                }
                
                val firestoreKeywords = snapshot?.documents?.associate {
                    val keyword = it.getString("value") ?: return@associate null to null
                    val explanation = it.getString("explanation") ?: "This keyword is commonly used in scam messages"
                    keyword to explanation
                }?.filterKeys { it != null } as? Map<String, String> ?: emptyMap()
                
                Log.d("EncryptedKeywords", "Realtime sync: ${firestoreKeywords.size} keywords updated")
                saveKeywordsMap(context, firestoreKeywords)
            }
    }
    
    // Stop realtime sync
    fun stopRealtimeSync() {
        keywordListener?.remove()
        keywordListener = null
    }

    fun clearKeywords(context: Context) {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val prefs = EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        prefs.edit().remove(KEYWORDS_KEY).remove(KEYWORDS_MAP_KEY).apply()
    }

    private fun saveKeywordsMap(context: Context, keywordsMap: Map<String, String>) {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val prefs = EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        val json = gson.toJson(keywordsMap)
        prefs.edit().putString(KEYWORDS_MAP_KEY, json).apply()
        
        Log.d("EncryptedKeywords", "saveKeywordsMap saved ${keywordsMap.size} keywords: ${keywordsMap.keys.joinToString()}")
    }

    fun saveKeywords(context: Context, keywords: Set<String>) {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val prefs = EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        prefs.edit().putString(KEYWORDS_KEY, keywords.joinToString("||")).apply()
    }
}
