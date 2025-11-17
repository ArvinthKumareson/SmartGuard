package com.smartguard.app.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a single suspicious (or scanned) message.
 *
 * This is stored locally in the encrypted Room database for quick access
 * and offline history visualisation.
 */
@Entity(tableName = "scan_records")
data class ScanRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val message: String,
    val matchedKeywords: String, // comma-separated keywords
    val keywordExplanations: String? = null, // JSON map of keyword to explanation
    val sourceApp: String?,       // e.g. "SMS", "Demo"
    val timestamp: Long,
    val userId: String,
    val senderName: String? = null,
    val conversationTitle: String? = null
)
