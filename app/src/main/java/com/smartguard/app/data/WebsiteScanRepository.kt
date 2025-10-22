package com.smartguard.app.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.smartguard.app.model.WebsiteScanHistory
import com.smartguard.app.model.WebsiteScanResult
import kotlinx.coroutines.tasks.await

class WebsiteScanRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val scansCollection = firestore.collection("website_scans")

    suspend fun saveScan(result: WebsiteScanResult): Result<String> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))
            
            val scanHistory = WebsiteScanHistory(
                userId = userId,
                url = result.url,
                isSafe = result.isSafe,
                maliciousCount = result.maliciousCount,
                suspiciousCount = result.suspiciousCount,
                harmlessCount = result.harmlessCount,
                reputation = result.reputation,
                title = result.title
            )
            
            val docRef = scansCollection.add(scanHistory.toMap()).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserScans(limit: Int = 50): Result<List<WebsiteScanHistory>> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))
            
            val snapshot = scansCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()
            
            val scans = snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject(WebsiteScanHistory::class.java)?.copy(id = doc.id)
                } catch (e: Exception) {
                    null
                }
            }
            .sortedByDescending { it.scanDate }
            .take(limit)
            
            Result.success(scans)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteScan(scanId: String): Result<Unit> {
        return try {
            scansCollection.document(scanId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun clearAllScans(): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))
            
            val snapshot = scansCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()
            
            snapshot.documents.forEach { doc ->
                doc.reference.delete().await()
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

