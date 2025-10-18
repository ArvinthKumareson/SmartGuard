package com.smartguard.app.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await



object EncryptedKeywords {
    private const val PREFS_NAME = "smartguard_encrypted_prefs"
    private const val KEYWORDS_KEY = "keywords"

    fun getKeywords(context: Context): Set<String> {
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

        val joined = prefs.getString(KEYWORDS_KEY, null)
        return joined?.split("||")?.filter { it.isNotBlank() }?.toSet() ?: emptySet()
    }

    suspend fun syncFromFirestore(context: Context) {
        val snapshot = FirebaseFirestore.getInstance().collection("keywords").get().await()
        val firestoreKeywords = snapshot.documents.mapNotNull { it.getString("value") }.toSet()
        val merged = KeywordRepository.getKeywords() + firestoreKeywords
        saveKeywords(context, merged)
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

        prefs.edit().remove(KEYWORDS_KEY).apply()
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
