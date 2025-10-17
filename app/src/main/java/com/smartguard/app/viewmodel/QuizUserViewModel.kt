package com.smartguard.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.smartguard.app.model.QuizQ
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class QuizUserViewModel : ViewModel() {
    private val _quiz = MutableStateFlow<List<QuizQ>>(emptyList())
    val quiz: StateFlow<List<QuizQ>> = _quiz

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        viewModelScope.launch {
            try {
                val loadedQuiz = fetchQuizFromFirestore()
                _quiz.value = loadedQuiz
            } catch (e: Exception) {
                _quiz.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun fetchQuizFromFirestore(): List<QuizQ> {
        val snapshot = FirebaseFirestore.getInstance()
            .collection("quizzes")
            .get()
            .await()

        val allQuizzes = snapshot.documents.mapNotNull { it.toObject(QuizQ::class.java) }

        return allQuizzes.shuffled().take(10)
    }

}

