package com.smartguard.app.data

import android.content.Context
import android.util.Log

/**
 * Represents a keyword that was matched in a message along with its explanation.
 *
 * This is the basic unit returned by the detection engine to describe
 * why a particular piece of text was considered suspicious.
 */
data class MatchedKeyword(
    val keyword: String,
    val explanation: String
)

/**
 * Simple keyword-based detection engine used to flag potential scam messages.
 *
 * Responsibilities:
 *  - Load the latest scam keywords and their explanations from encrypted storage.
 *  - Scan an incoming text message for any occurrence of those keywords.
 *  - Return a list of [MatchedKeyword] so the UI can show "why" a message was flagged.
 *
 * This class is intentionally stateless: it pulls fresh data from [EncryptedKeywords]
 * on every scan so newly-added admin keywords are immediately taken into account.
 */
class DetectionEngine(private val context: Context) {
    
    /**
     * Fetches the current set of scam keywords from encrypted SharedPreferences.
     *
     * The data ultimately comes from Firestore but is cached locally by
     * [EncryptedKeywords] for offline use.
     */
    private fun getKeywords(): Set<String> {
        val keywords = EncryptedKeywords.getKeywords(context)
        Log.d("DetectionEngine", "Loaded ${keywords.size} keywords for scanning")
        return keywords
    }
    
    /**
     * Retrieves the map of keyword -> human-readable explanation.
     *
     * Explanations are authored by an admin in Firestore and synced down
     * to the device via [EncryptedKeywords]. These are later surfaced to
     * the user in the UI when explaining why a message was blocked.
     */
    private fun getKeywordsMap(): Map<String, String> {
        return EncryptedKeywords.getKeywordsMap(context)
    }
    
    /**
     * Scans a message and returns all matched keywords with their explanations.
     *
     *  1. Normalizes the message to lowercase for case-insensitive matching.
     *  2. For each keyword currently stored on the device, checks whether the
     *     keyword appears as a substring in the message.
     *  3. Builds a [MatchedKeyword] for each hit, using the stored explanation
     *     or a safe default if none exists.
     */
    fun scanWithExplanations(text: String): List<MatchedKeyword> {
        // Always pull fresh keywords so admin changes are reflected immediately.
        val keywords = getKeywords()
        val keywordsMap = getKeywordsMap()
        val normalized = text.lowercase()

        // Filter matching keywords and pair each with its explanation
        val matches = keywords
            .filter { k -> normalized.contains(k.lowercase()) }
            .map { keyword ->
                MatchedKeyword(
                    keyword = keyword,
                    // Use explanation from Firestore; fallback to a generic message
                    explanation = keywordsMap[keyword]
                        ?: "This keyword is commonly used in scam messages"
                )
            }

        Log.d("DetectionEngine", "Scanned with explanations, found ${matches.size} matches")
        return matches
    }
}
