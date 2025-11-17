package com.smartguard.app.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.smartguard.app.model.QuizQ


/**
 * ViewModel for admin management of quiz questions.
 *
 * It loads all questions from the "quizzes" collection and exposes CRUD
 * operations for adding, updating and deleting [QuizQ] entries.
 */
class QuizAdminViewModel : ViewModel() {
    private val db = Firebase.firestore
    val questions = MutableStateFlow<List<Pair<String, QuizQ>>>(emptyList())

    init {
        // Immediately load questions for the admin quiz screen.
        fetchQuestions()
    }
    
    /**
     * Loads all quiz questions from Firestore and stores them as pairs of
     * (document id, question model).
     */
    fun fetchQuestions() {
        db.collection("quizzes").get().addOnSuccessListener { result ->
            val list = result.documents.mapNotNull { doc ->
                val q = QuizQ(
                    question = doc.getString("question") ?: "",
                    choices = doc.get("choices") as? List<String> ?: emptyList(),
                    answer = (doc.getLong("answer") ?: 0).toInt(),
                    videoId = doc.getString("videoId"),
                    videoUri = doc.getString("videoUri"),
                    reason = doc.getString("reason")
                )
                doc.id to q
            }
            questions.value = list
        }
    }

    /**
     * Inserts a new quiz question document.
     */
    fun addQuestion(q: QuizQ) {
        db.collection("quizzes").add(q.toMap()).addOnSuccessListener { fetchQuestions() }
    }

    /**
     * Replaces the question document with the given id.
     */
    fun updateQuestion(id: String, q: QuizQ) {
        db.collection("quizzes").document(id).set(q.toMap()).addOnSuccessListener { fetchQuestions() }
    }

    /**
     * Deletes a quiz question document.
     */
    fun deleteQuestion(id: String) {
        db.collection("quizzes").document(id).delete().addOnSuccessListener { fetchQuestions() }
    }

    private fun QuizQ.toMap() = mapOf(
        "question" to question,
        "choices" to choices,
        "answer" to answer,
        "videoId" to videoId,
        "videoUri" to videoUri,
        "reason" to reason
    )
}
