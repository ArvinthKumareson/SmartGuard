package com.smartguard.app.model

data class QuizQ(
    val question: String = "",
    val choices: List<String> = emptyList(),
    val answer: Int = 0,
    val videoId: String? = null,  // Deprecated: kept for backward compatibility
    val videoUri: String? = null,   // New: local video URI (Cloudinary)
    val reason: String? = null     // Explanation for the correct answer
)
