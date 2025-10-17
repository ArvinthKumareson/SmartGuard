package com.smartguard.app.data

import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import android.content.Context


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
        return joined?.split("||")?.filter { it.isNotBlank() }?.toSet() ?: KeywordRepository.getKeywords()
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
