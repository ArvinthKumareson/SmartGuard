package com.smartguard.app.db

import androidx.room.Entity
import androidx.room.PrimaryKey

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
