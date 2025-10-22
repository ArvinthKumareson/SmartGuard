package com.smartguard.app.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.smartguard.app.model.UserFeedback
import kotlinx.coroutines.tasks.await

class FeedbackRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val feedbackCollection = firestore.collection("user_feedback")

    suspend fun submitFeedback(
        category: String,
        subject: String,
        message: String,
        rating: Int
    ): Result<String> {
        return try {
            val currentUser = auth.currentUser ?: return Result.failure(Exception("User not logged in"))
            
            val feedback = UserFeedback(
                userId = currentUser.uid,
                userName = currentUser.displayName ?: "Anonymous",
                userEmail = currentUser.email ?: "",
                category = category,
                subject = subject,
                message = message,
                rating = rating,
                status = "pending"
            )
            
            val docRef = feedbackCollection.add(feedback.toMap()).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserFeedback(): Result<List<UserFeedback>> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))
            
            val snapshot = feedbackCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()
            
            val feedback = snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject(UserFeedback::class.java)?.copy(id = doc.id)
                } catch (e: Exception) {
                    null
                }
            }.sortedByDescending { it.timestamp.seconds }
            
            Result.success(feedback)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllFeedback(): Result<List<UserFeedback>> {
        return try {
            val snapshot = feedbackCollection
                .get()
                .await()
            
            val feedback = snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject(UserFeedback::class.java)?.copy(id = doc.id)
                } catch (e: Exception) {
                    null
                }
            }.sortedByDescending { it.timestamp.seconds }
            
            Result.success(feedback)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateFeedbackStatus(feedbackId: String, status: String, response: String? = null): Result<Unit> {
        return try {
            val updates = mutableMapOf<String, Any>(
                "status" to status
            )
            if (response != null) {
                updates["adminResponse"] = response
            }
            
            feedbackCollection.document(feedbackId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteFeedback(feedbackId: String): Result<Unit> {
        return try {
            feedbackCollection.document(feedbackId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

