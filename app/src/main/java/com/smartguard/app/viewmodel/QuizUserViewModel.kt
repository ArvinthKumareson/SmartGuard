// File: viewmodel/QuizUserViewModel.kt
package com.smartguard.app.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.smartguard.app.model.QuizQ
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class QuizUserViewModel : ViewModel() {
    private val db = Firebase.firestore
    private val _quiz = MutableStateFlow<List<QuizQ>>(emptyList())
    val quiz: StateFlow<List<QuizQ>> = _quiz

    init {
        fetchRandomQuiz()
    }

    fun fetchRandomQuiz() {
        db.collection("quizzes").get().addOnSuccessListener { result ->
            val all = result.mapNotNull { doc ->
                val question = doc.getString("question") ?: return@mapNotNull null
                val choices = doc.get("choices") as? List<String> ?: return@mapNotNull null
                val answer = (doc.getLong("answer") ?: 0).toInt()
                val videoId = doc.getString("videoId")
                QuizQ(question, choices, answer, videoId)
            }
            _quiz.value = all.shuffled().take(10)
        }
    }
}
