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

data class KeywordInfo(
    val keyword: String,
    val explanation: String
)

object EncryptedKeywords {
    private const val PREFS_NAME = "smartguard_encrypted_prefs"
    private const val KEYWORDS_KEY = "keywords"
    private const val KEYWORDS_MAP_KEY = "keywords_map"
    private val gson = Gson()
    private var keywordListener: ListenerRegistration? = null

    fun getKeywords(context: Context): Set<String> {
        val keywordsMap = getKeywordsMap(context)
        return keywordsMap.keys
    }

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

    suspend fun syncFromFirestore(context: Context) {
        val snapshot = FirebaseFirestore.getInstance().collection("keywords").get().await()
        val firestoreKeywords = snapshot.documents.associate {
            val keyword = it.getString("value") ?: return@associate null to null
            val explanation = it.getString("explanation") ?: "This keyword is commonly used in scam messages"
            keyword to explanation
        }.filterKeys { it != null } as Map<String, String>
        
        Log.d("EncryptedKeywords", "Synced ${firestoreKeywords.size} keywords from Firestore")
        saveKeywordsMap(context, firestoreKeywords)
    }

    // Real-time sync - automatically updates when keywords change in Firestore
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
