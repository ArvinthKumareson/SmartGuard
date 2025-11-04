package com.smartguard.app.util

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.util.UUID

object CloudinaryStorageHelper {

    private const val CLOUD_NAME = "divkeedz7"
    private const val UPLOAD_PRESET = "unsigned_preset" // Create in Cloudinary settings
    
    private val client = OkHttpClient()
    
    suspend fun uploadImage(context: Context, imageUri: Uri): String? {
        return uploadFile(context, imageUri, "course_images", "https://api.cloudinary.com/v1_1/$CLOUD_NAME/image/upload")
    }
    
    suspend fun uploadVideo(context: Context, videoUri: Uri): String? {
        return uploadFile(context, videoUri, "course_videos", "https://api.cloudinary.com/v1_1/$CLOUD_NAME/video/upload")
    }
    
    suspend fun uploadScamReportImage(context: Context, imageUri: Uri): String? {
        return uploadFile(context, imageUri, "scam_reports", "https://api.cloudinary.com/v1_1/$CLOUD_NAME/image/upload")
    }
    
    private suspend fun uploadFile(
        context: Context,
        fileUri: Uri,
        folder: String,
        uploadUrl: String
    ): String? = withContext(Dispatchers.IO) {
        try {
            Log.d("CloudinaryStorage", "Starting upload for URI: $fileUri")
            
            val inputStream = context.contentResolver.openInputStream(fileUri)
                ?: throw Exception("Cannot access file")
            
            val tempFile = File(context.cacheDir, "${UUID.randomUUID()}")
            tempFile.outputStream().use { output ->
                inputStream.copyTo(output)
            }
            inputStream.close()
            
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "file",
                    tempFile.name,
                    tempFile.asRequestBody("application/octet-stream".toMediaType())
                )
                .addFormDataPart("upload_preset", UPLOAD_PRESET)
                .addFormDataPart("folder", folder)
                .addFormDataPart("public_id", UUID.randomUUID().toString())
                .build()
            
            val request = Request.Builder()
                .url(uploadUrl)
                .post(requestBody)
                .build()
            
            Log.d("CloudinaryStorage", "Uploading to Cloudinary...")
            val response = client.newCall(request).execute()
            
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                val jsonResponse = JSONObject(responseBody ?: "{}")
                val secureUrl = jsonResponse.optString("secure_url")
                
                Log.d("CloudinaryStorage", "Upload successful: $secureUrl")
                tempFile.delete()
                secureUrl
            } else {
                Log.e("CloudinaryStorage", "Upload failed: ${response.code} - ${response.message}")
                tempFile.delete()
                null
            }
        } catch (e: Exception) {
            Log.e("CloudinaryStorage", "Error uploading file", e)
            null
        }
    }
}

