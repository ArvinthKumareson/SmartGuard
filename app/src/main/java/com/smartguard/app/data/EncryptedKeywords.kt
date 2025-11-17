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

/**
 * Simple model for storing a scam keyword together with its explanation.
 */
data class KeywordInfo(
    val keyword: String,
    val explanation: String
)

/**
 * Central place for managing scam detection keywords on the device.
 *
 * Responsibilities:
 *  - Store keywords and their explanations in [EncryptedSharedPreferences]
 *    so that they are protected at rest.
 *  - Sync the latest keyword set from Firestore on demand (manual sync).
 *  - Maintain an optional real-time listener so that admin changes in
 *    Firestore are reflected on the device without requiring a restart.
 */
object EncryptedKeywords {
    private const val PREFS_NAME = "smartguard_encrypted_prefs"
    private const val KEYWORDS_KEY = "keywords"
    private const val KEYWORDS_MAP_KEY = "keywords_map"
    private val gson = Gson()
    // Listens for real-time keyword updates from Firestore
    private var keywordListener: ListenerRegistration? = null

    /**
     * Returns the set of currently stored keywords.
     *
     * This is derived from the keyword -> explanation map; the explanations
     * themselves are only needed later in the UI.
     */
    fun getKeywords(context: Context): Set<String> {
        val keywordsMap = getKeywordsMap(context)
        return keywordsMap.keys
    }

    /**
     * Retrieves the keyword -> explanation map from encrypted storage.
     *
     * Internally this uses [EncryptedSharedPreferences] and a per-app
     * [MasterKey] so the data cannot be read from the raw file on disk.
     */
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

    /**
     * Manually syncs keywords from Firestore to local encrypted storage.
     *
     * Typical call sites:
     *  - On app/login startup to prime the cache.
     *  - Before scanning a message, to ensure we are using a fresh keyword list.
     */
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

    /**
     * Starts a real-time listener for Firestore keyword updates.
     *
     * Whenever the "keywords" collection changes, this listener rewrites
     * the encrypted local cache. This allows the detection engine to
     * immediately see new keywords without user interaction.
     */
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
    
    /**
     * Stops the Firestore snapshot listener, freeing network and memory resources.
     *
     * Typically called when the app/activity is destroyed.
     */
    fun stopRealtimeSync() {
        keywordListener?.remove()
        keywordListener = null
    }

    /**
     * Clears all locally stored keyword information.
     *
     * This is useful when logging out, or when wanting to force a clean
     * re-sync from Firestore.
     */
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

    /**
     * Persists a new keyword -> explanation map into encrypted preferences.
     */
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

    /**
     * Legacy helper to persist just the keyword set (without explanations).
     *
     * Newer code prefers [saveKeywordsMap] so that explanations are available
     * for user-facing explanations.
     */
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
