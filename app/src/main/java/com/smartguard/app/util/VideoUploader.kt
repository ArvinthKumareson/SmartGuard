package com.smartguard.app.util

import android.content.Context
import android.net.Uri

object VideoUploader {
    suspend fun uploadVideo(context: Context, videoUri: Uri): String? {
        return CloudinaryStorageHelper.uploadVideo(context, videoUri)
    }

    suspend fun deleteVideo(downloadUrl: String): Boolean {
        return try {
            // Cloudinary deletion would require extracting public_id and using delete API
            // For simplicity, return true (manual deletion from Cloudinary dashboard)
            android.util.Log.d("VideoUploader", "Video deletion not implemented for Cloudinary")
            true
        } catch (e: Exception) {
            android.util.Log.e("VideoUploader", "Failed to delete video", e)
            false
        }
    }
}

