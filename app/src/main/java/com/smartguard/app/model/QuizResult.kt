package com.smartguard.app.model

data class QuizResult(
    val question: String,
    val selectedAnswer: String?,
    val correctAnswer: String,
    val isCorrect: Boolean,
    val reason: String? = null
)
