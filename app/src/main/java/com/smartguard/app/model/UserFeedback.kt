package com.smartguard.app.model

import com.google.firebase.Timestamp

data class UserFeedback(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userEmail: String = "",
    val category: String = "",
    val subject: String = "",
    val message: String = "",
    val rating: Int = 0,
    val timestamp: Timestamp = Timestamp.now(),
    val status: String = "pending",
    val adminResponse: String? = null
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "userId" to userId,
        "userName" to userName,
        "userEmail" to userEmail,
        "category" to category,
        "subject" to subject,
        "message" to message,
        "rating" to rating,
        "timestamp" to timestamp,
        "status" to status,
        "adminResponse" to adminResponse
    )
}

enum class FeedbackCategory(val displayName: String) {
    BUG("Bug Report"),
    FEATURE("Feature Request"),
    IMPROVEMENT("Improvement Suggestion"),
    COMPLAINT("Complaint"),
    OTHER("Other")
}

