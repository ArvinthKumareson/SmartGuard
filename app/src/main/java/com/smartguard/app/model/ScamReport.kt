package com.smartguard.app.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class ScamReport(
    @DocumentId val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userEmail: String = "",
    val title: String = "",
    val description: String = "",
    val scamType: String = "",
    val amount: String = "",
    val platform: String = "",
    val imageUrl: String? = null,
    val timestamp: Timestamp = Timestamp.now(),
    val status: String = "pending",
    val moderatorNote: String? = null,
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val likedBy: List<String> = emptyList()
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "userId" to userId,
            "userName" to userName,
            "userEmail" to userEmail,
            "title" to title,
            "description" to description,
            "scamType" to scamType,
            "amount" to amount,
            "platform" to platform,
            "imageUrl" to imageUrl,
            "timestamp" to timestamp,
            "status" to status,
            "moderatorNote" to moderatorNote,
            "likesCount" to likesCount,
            "commentsCount" to commentsCount,
            "likedBy" to likedBy
        )
    }
}

data class ScamComment(
    @DocumentId val id: String = "",
    val reportId: String = "",
    val userId: String = "",
    val userName: String = "",
    val comment: String = "",
    val timestamp: Timestamp = Timestamp.now()
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "reportId" to reportId,
            "userId" to userId,
            "userName" to userName,
            "comment" to comment,
            "timestamp" to timestamp
        )
    }
}

enum class ScamType(val displayName: String) {
    PHISHING("Phishing/Fake Website"),
    INVESTMENT("Investment Scam"),
    ROMANCE("Romance Scam"),
    SHOPPING("Online Shopping Scam"),
    LOTTERY("Lottery/Prize Scam"),
    EMPLOYMENT("Employment Scam"),
    IMPERSONATION("Impersonation Scam"),
    OTHER("Other")
}

enum class ReportStatus(val displayName: String) {
    PENDING("Pending Review"),
    APPROVED("Approved"),
    REJECTED("Rejected")
}

