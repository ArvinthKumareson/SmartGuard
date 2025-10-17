package com.smartguard.app.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.smartguard.app.model.QuizQ


class QuizAdminViewModel : ViewModel() {
    private val db = Firebase.firestore
    val questions = MutableStateFlow<List<Pair<String, QuizQ>>>(emptyList())

    init {
        fetchQuestions()
    }

    fun fetchQuestions() {
        db.collection("quizzes").get().addOnSuccessListener { result ->
            val list = result.documents.mapNotNull { doc ->
                val q = QuizQ(
                    question = doc.getString("question") ?: "",
                    choices = doc.get("choices") as? List<String> ?: emptyList(),
                    answer = (doc.getLong("answer") ?: 0).toInt(),
                    videoId = doc.getString("videoId")
                )
                doc.id to q
            }
            questions.value = list
        }
    }

    fun addQuestion(q: QuizQ) {
        db.collection("quizzes").add(q.toMap()).addOnSuccessListener { fetchQuestions() }
    }

    fun updateQuestion(id: String, q: QuizQ) {
        db.collection("quizzes").document(id).set(q.toMap()).addOnSuccessListener { fetchQuestions() }
    }

    fun deleteQuestion(id: String) {
        db.collection("quizzes").document(id).delete().addOnSuccessListener { fetchQuestions() }
    }

    private fun QuizQ.toMap() = mapOf(
        "question" to question,
        "choices" to choices,
        "answer" to answer,
        "videoId" to videoId
    )
}
