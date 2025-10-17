package com.smartguard.app.model

data class QuizQ(
    val question: String = "",
    val choices: List<String> = emptyList(),
    val answer: Int = 0,
    val videoId: String? = null
)
