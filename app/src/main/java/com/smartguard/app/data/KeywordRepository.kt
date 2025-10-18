package com.smartguard.app.data

object KeywordRepository {
    // Basic keyword list. Extend via DataStore updates in future. Only for backend use not for general use.
    private val defaultKeywords = setOf(
        "invoice",
    )

    fun getKeywords(): Set<String> = defaultKeywords
}