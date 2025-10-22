package com.smartguard.app.model

import com.google.firebase.Timestamp

data class WebsiteScanHistory(
    val id: String = "",
    val userId: String = "",
    val url: String = "",
    val isSafe: Boolean = false,
    val maliciousCount: Int = 0,
    val suspiciousCount: Int = 0,
    val harmlessCount: Int = 0,
    val reputation: Int = 0,
    val title: String? = null,
    val timestamp: Timestamp = Timestamp.now(),
    val scanDate: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "userId" to userId,
        "url" to url,
        "isSafe" to isSafe,
        "maliciousCount" to maliciousCount,
        "suspiciousCount" to suspiciousCount,
        "harmlessCount" to harmlessCount,
        "reputation" to reputation,
        "title" to title,
        "timestamp" to timestamp,
        "scanDate" to scanDate
    )
}

