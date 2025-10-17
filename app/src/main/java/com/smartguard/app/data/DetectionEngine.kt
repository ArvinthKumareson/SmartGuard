package com.smartguard.app.data

class DetectionEngine(private val keywords: Set<String>) {
    fun scan(text: String): List<String> {
        val normalized = text.lowercase()
        return keywords.filter { k -> normalized.contains(k) }
        //Test for commiting
    }
}