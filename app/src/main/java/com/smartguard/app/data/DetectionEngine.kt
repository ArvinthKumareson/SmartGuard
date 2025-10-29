package com.smartguard.app.data

import android.content.Context
import android.util.Log

data class MatchedKeyword(
    val keyword: String,
    val explanation: String
)

class DetectionEngine(private val context: Context) {
    
    // Always get fresh keywords from encrypted storage
    private fun getKeywords(): Set<String> {
        val keywords = EncryptedKeywords.getKeywords(context)
        Log.d("DetectionEngine", "Loaded ${keywords.size} keywords for scanning")
        return keywords
    }
    
    private fun getKeywordsMap(): Map<String, String> {
        return EncryptedKeywords.getKeywordsMap(context)
    }

    fun scan(text: String): List<String> {
        val keywords = getKeywords() // Get fresh keywords every time
        val normalized = text.lowercase()
        val matches = keywords.filter { k -> normalized.contains(k) }
        Log.d("DetectionEngine", "Scanned text, found ${matches.size} matches: ${matches.joinToString()}")
        return matches
    }

    fun scanWithExplanations(text: String): List<MatchedKeyword> {
        val keywords = getKeywords() // Get fresh keywords every time
        val keywordsMap = getKeywordsMap() // Get fresh explanations every time
        val normalized = text.lowercase()
        val matches = keywords.filter { k -> normalized.contains(k) }.map { keyword ->
            MatchedKeyword(
                keyword = keyword,
                explanation = keywordsMap[keyword] ?: "This keyword is commonly used in scam messages"
            )
        }
        Log.d("DetectionEngine", "Scanned with explanations, found ${matches.size} matches")
        return matches
    }
}