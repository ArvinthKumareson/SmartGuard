package com.smartguard.app.data

import android.content.Context
import android.util.Log

// Represents a keyword that was matched in a message along with its explanation
data class MatchedKeyword(
    val keyword: String,
    val explanation: String
)

// Engine for scanning messages against scam keywords
class DetectionEngine(private val context: Context) {
    
    // Fetches current keywords from encrypted storage
    // Pulls fresh keywords every time to ensure new admin-added keywords are picked up
    private fun getKeywords(): Set<String> {
        val keywords = EncryptedKeywords.getKeywords(context)
        Log.d("DetectionEngine", "Loaded ${keywords.size} keywords for scanning")
        return keywords
    }
    
    // Retrieves the keyword-to-explanation mapping from encrypted storage
    private fun getKeywordsMap(): Map<String, String> {
        return EncryptedKeywords.getKeywordsMap(context)
    }

    // Scans message and returns matched keywords with their explanations
    // Used to show users why a message was flagged as potential scam
    fun scanWithExplanations(text: String): List<MatchedKeyword> {
        val keywords = getKeywords() // Fresh keywords every scan
        val keywordsMap = getKeywordsMap() // Fresh explanations every scan
        val normalized = text.lowercase()
        // Filter matching keywords and pair each with its explanation
        val matches = keywords.filter { k -> normalized.contains(k.lowercase()) }.map { keyword ->
            MatchedKeyword(
                keyword = keyword,
                // Use explanation from Firestore, fallback to default message
                explanation = keywordsMap[keyword] ?: "This keyword is commonly used in scam messages"
            )
        }
        Log.d("DetectionEngine", "Scanned with explanations, found ${matches.size} matches")
        return matches
    }
}
