package com.smartguard.app.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import com.smartguard.app.model.ScamComment
import com.smartguard.app.model.ScamReport
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class ScamReportRepository {
    private val firestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    private val reportsCollection = firestore.collection("scam_reports")
    private val commentsCollection = firestore.collection("scam_comments")
    private val usersCollection = firestore.collection("users")

    suspend fun submitReport(
        title: String,
        description: String,
        scamType: String,
        amount: String,
        platform: String,
        postAsAnonymous: Boolean = false,
        imageUrl: String? = null
    ): Result<Unit> {
        return try {
            val currentUser = auth.currentUser ?: return Result.failure(Exception("User not logged in"))
            val userId = currentUser.uid
            
            val userName = if (postAsAnonymous) {
                "Anonymous"
            } else {
                val userDoc = usersCollection.document(userId).get().await()
                userDoc.getString("displayName") 
                    ?: userDoc.getString("name") 
                    ?: currentUser.displayName 
                    ?: currentUser.email?.substringBefore("@") 
                    ?: "User"
            }
            
            val userEmail = if (postAsAnonymous) "hidden" else (currentUser.email ?: "N/A")

            val report = ScamReport(
                userId = userId,
                userName = userName,
                userEmail = userEmail,
                title = title,
                description = description,
                scamType = scamType,
                amount = amount,
                platform = platform,
                imageUrl = imageUrl
            )
            
            reportsCollection.add(report.toMap()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getApprovedReports(): Flow<List<ScamReport>> {
        return reportsCollection
            .whereEqualTo("status", "approved")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    doc.toObject(ScamReport::class.java)?.copy(id = doc.id)
                }
            }
    }

    fun getAllReports(): Flow<List<ScamReport>> {
        return reportsCollection
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    doc.toObject(ScamReport::class.java)?.copy(id = doc.id)
                }
            }
    }

    // Fallback: load without filters or ordering to avoid index/field issues
    fun getReportsAnyStatusUnordered(): Flow<List<ScamReport>> {
        return reportsCollection
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    doc.toObject(ScamReport::class.java)?.copy(id = doc.id)
                }
            }
    }

    fun getReportComments(reportId: String): Flow<List<ScamComment>> {
        return commentsCollection
            .whereEqualTo("reportId", reportId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    doc.toObject(ScamComment::class.java)?.copy(id = doc.id)
                }
            }
    }

    suspend fun addComment(reportId: String, comment: String): Result<Unit> {
        return try {
            val currentUser = auth.currentUser ?: return Result.failure(Exception("User not logged in"))
            val userId = currentUser.uid
            
            val userDoc = usersCollection.document(userId).get().await()
            val userName = userDoc.getString("displayName") 
                ?: userDoc.getString("name") 
                ?: currentUser.displayName 
                ?: currentUser.email?.substringBefore("@") 
                ?: "User"

            val scamComment = ScamComment(
                reportId = reportId,
                userId = userId,
                userName = userName,
                comment = comment
            )
            
            commentsCollection.add(scamComment.toMap()).await()
            
            reportsCollection.document(reportId).update(
                "commentsCount", FieldValue.increment(1)
            ).await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleLike(reportId: String): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))
            val reportDoc = reportsCollection.document(reportId).get().await()
            val report = reportDoc.toObject(ScamReport::class.java) ?: return Result.failure(Exception("Report not found"))
            
            val likedBy = report.likedBy.toMutableList()
            val isLiked = userId in likedBy
            
            if (isLiked) {
                likedBy.remove(userId)
                reportsCollection.document(reportId).update(
                    mapOf(
                        "likedBy" to likedBy,
                        "likesCount" to FieldValue.increment(-1)
                    )
                ).await()
            } else {
                likedBy.add(userId)
                reportsCollection.document(reportId).update(
                    mapOf(
                        "likedBy" to likedBy,
                        "likesCount" to FieldValue.increment(1)
                    )
                ).await()
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateReportStatus(reportId: String, status: String, moderatorNote: String? = null): Result<Unit> {
        return try {
            val updates = mutableMapOf<String, Any>("status" to status)
            if (moderatorNote != null) {
                updates["moderatorNote"] = moderatorNote
            }
            reportsCollection.document(reportId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteReport(reportId: String): Result<Unit> {
        return try {
            val comments = commentsCollection
                .whereEqualTo("reportId", reportId)
                .get()
                .await()
            
            comments.documents.forEach { it.reference.delete().await() }
            
            reportsCollection.document(reportId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteComment(commentId: String, reportId: String): Result<Unit> {
        return try {
            commentsCollection.document(commentId).delete().await()
            
            reportsCollection.document(reportId).update(
                "commentsCount", FieldValue.increment(-1)
            ).await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

