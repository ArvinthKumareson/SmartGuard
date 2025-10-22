package com.smartguard.app.util

import android.content.Context
import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

object VideoUploader {
    private val storage = FirebaseStorage.getInstance()
    

    suspend fun uploadVideo(context: Context, videoUri: Uri): String? {
        return try {
            android.util.Log.d("VideoUploader", "Starting upload for URI: $videoUri")
            
            // Generate unique filename
            val fileName = "quiz_videos/${UUID.randomUUID()}.mp4"
            val storageRef = storage.reference.child(fileName)
            
            android.util.Log.d("VideoUploader", "Storage path: $fileName")
            
            // Read video data from content URI
            val inputStream = context.contentResolver.openInputStream(videoUri)
            if (inputStream == null) {
                android.util.Log.e("VideoUploader", "Failed to open input stream - URI not accessible")
                throw Exception("Cannot access video file. Please try selecting it again.")
            }
            
            android.util.Log.d("VideoUploader", "Input stream opened successfully")
            
            // Upload to Firebase Storage
            android.util.Log.d("VideoUploader", "Starting Firebase upload...")
            val uploadTask = storageRef.putStream(inputStream)
            uploadTask.await()
            
            android.util.Log.d("VideoUploader", "Upload completed, getting download URL...")
            
            // Get download URL
            val downloadUrl = storageRef.downloadUrl.await()
            
            inputStream.close()
            
            android.util.Log.d("VideoUploader", "Success! Download URL: ${downloadUrl.toString()}")
            downloadUrl.toString()
        } catch (e: com.google.firebase.storage.StorageException) {
            val errorMsg = when (e.errorCode) {
                com.google.firebase.storage.StorageException.ERROR_OBJECT_NOT_FOUND -> "File not found"
                com.google.firebase.storage.StorageException.ERROR_BUCKET_NOT_FOUND -> "Firebase Storage not configured. Please enable it in Firebase Console."
                com.google.firebase.storage.StorageException.ERROR_PROJECT_NOT_FOUND -> "Firebase project not found"
                com.google.firebase.storage.StorageException.ERROR_QUOTA_EXCEEDED -> "Storage quota exceeded"
                com.google.firebase.storage.StorageException.ERROR_NOT_AUTHENTICATED -> "User not authenticated"
                com.google.firebase.storage.StorageException.ERROR_NOT_AUTHORIZED -> "Permission denied. Check Firebase Storage security rules."
                com.google.firebase.storage.StorageException.ERROR_RETRY_LIMIT_EXCEEDED -> "Upload timed out. Check internet connection."
                else -> "Storage error: ${e.message}"
            }
            android.util.Log.e("VideoUploader", "Storage error: $errorMsg", e)
            throw Exception(errorMsg)
        } catch (e: Exception) {
            android.util.Log.e("VideoUploader", "Upload failed: ${e.message}", e)
            throw Exception("Upload failed: ${e.message}")
        }
    }
    

    suspend fun deleteVideo(downloadUrl: String): Boolean {
        return try {
            val storageRef = storage.getReferenceFromUrl(downloadUrl)
            storageRef.delete().await()
            true
        } catch (e: Exception) {
            android.util.Log.e("VideoUploader", "Failed to delete video", e)
            false
        }
    }
}

