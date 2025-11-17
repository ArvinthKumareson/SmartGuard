package com.smartguard.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.smartguard.app.model.QuizQ
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * ViewModel for end-users taking quizzes.
 *
 * It loads a random subset of questions from the "quizzes" collection and
 * exposes them as state to the quiz screen.
 */
class QuizUserViewModel : ViewModel() {
    private val _quiz = MutableStateFlow<List<QuizQ>>(emptyList())
    val quiz: StateFlow<List<QuizQ>> = _quiz

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        // On creation, fetch quiz questions asynchronously.
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

    /**
     * Fetches all quiz questions from Firestore and returns a random
     * selection of up to 10 questions.
     */
    private suspend fun fetchQuizFromFirestore(): List<QuizQ> {
        val snapshot = FirebaseFirestore.getInstance()
            .collection("quizzes")
            .get()
            .await()

        val allQuizzes = snapshot.documents.mapNotNull { it.toObject(QuizQ::class.java) }

        return allQuizzes.shuffled().take(10)
    }

}

