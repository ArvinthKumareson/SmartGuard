package com.smartguard.app.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.smartguard.app.model.ScamCourse
import kotlinx.coroutines.tasks.await

data class FirebaseCourseContent(
    val title: String = "",
    val description: String = "",
    val level: String = "",
    val isNew: Boolean = false,
    val tips: List<FirebaseCourseTip> = emptyList()
)

data class FirebaseCourseTip(
    val title: String = "",
    val description: String = "",
    val iconName: String = "",
    val isImportant: Boolean = false,
    val detailedContent: String = "",
    val actionSteps: List<String> = emptyList(),
    val imageUrl: String? = null,
    val videoUrl: String? = null
)

data class UserCourseProgress(
    val userId: String = "",
    val completedCourses: List<String> = emptyList(),
    val lastUpdated: Long = System.currentTimeMillis()
)

object CourseRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private const val COURSES_COLLECTION = "courses"
    private const val PROGRESS_COLLECTION = "user_course_progress"
    
    // Get all courses from Firebase
    suspend fun getAllCourses(): List<FirebaseCourseContent> {
        return try {
            val snapshot = firestore.collection(COURSES_COLLECTION)
                .get()
                .await()
            
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(FirebaseCourseContent::class.java)
            }
        } catch (e: Exception) {
            Log.e("CourseRepository", "Error fetching courses", e)
            emptyList()
        }
    }
    
    // Get single course by title
    suspend fun getCourseByTitle(title: String): FirebaseCourseContent? {
        return try {
            val snapshot = firestore.collection(COURSES_COLLECTION)
                .whereEqualTo("title", title)
                .limit(1)
                .get()
                .await()
            
            snapshot.documents.firstOrNull()?.toObject(FirebaseCourseContent::class.java)
        } catch (e: Exception) {
            Log.e("CourseRepository", "Error fetching course: $title", e)
            null
        }
    }
    
    // Get user's progress
    suspend fun getUserProgress(): UserCourseProgress? {
        val userId = auth.currentUser?.uid ?: return null
        
        return try {
            val doc = firestore.collection(PROGRESS_COLLECTION)
                .document(userId)
                .get()
                .await()
            
            doc.toObject(UserCourseProgress::class.java) ?: UserCourseProgress(userId = userId)
        } catch (e: Exception) {
            Log.e("CourseRepository", "Error fetching user progress", e)
            UserCourseProgress(userId = userId)
        }
    }
    
    // Mark course as completed
    suspend fun markCourseCompleted(courseTitle: String): Boolean {
        val userId = auth.currentUser?.uid ?: return false
        
        return try {
            val progress = getUserProgress() ?: UserCourseProgress(userId = userId)
            val updatedCourses = (progress.completedCourses + courseTitle).distinct()
            
            val updatedProgress = progress.copy(
                completedCourses = updatedCourses,
                lastUpdated = System.currentTimeMillis()
            )
            
            firestore.collection(PROGRESS_COLLECTION)
                .document(userId)
                .set(updatedProgress)
                .await()
            
            true
        } catch (e: Exception) {
            Log.e("CourseRepository", "Error marking course completed", e)
            false
        }
    }
    
    // Check if course is completed
    suspend fun isCourseCompleted(courseTitle: String): Boolean {
        val progress = getUserProgress() ?: return false
        return courseTitle in progress.completedCourses
    }
    
    // Upload initial course data (only run once)
    suspend fun uploadInitialCourseData(courses: List<FirebaseCourseContent>) {
        try {
            courses.forEach { course ->
                firestore.collection(COURSES_COLLECTION)
                    .document(course.title.replace(" ", "_"))
                    .set(course)
                    .await()
            }
            Log.d("CourseRepository", "Successfully uploaded ${courses.size} courses")
        } catch (e: Exception) {
            Log.e("CourseRepository", "Error uploading courses", e)
        }
    }
    
    // ADMIN FUNCTIONS
    
    // Create a new course
    suspend fun createCourse(course: FirebaseCourseContent): Boolean {
        return try {
            val docId = course.title.replace(" ", "_").lowercase()
            firestore.collection(COURSES_COLLECTION)
                .document(docId)
                .set(course)
                .await()
            Log.d("CourseRepository", "Created course: ${course.title}")
            true
        } catch (e: Exception) {
            Log.e("CourseRepository", "Error creating course", e)
            false
        }
    }
    
    // Update an existing course
    suspend fun updateCourse(oldTitle: String, course: FirebaseCourseContent): Boolean {
        return try {
            // Try to find existing document - old courses may not be lowercase
            val possibleDocIds = listOf(
                oldTitle.replace(" ", "_"),           // Original format (mixed case)
                oldTitle.replace(" ", "_").lowercase() // New format (lowercase)
            )
            
            Log.d("CourseRepository", "Updating course: oldTitle=$oldTitle, newTitle=${course.title}, tips count=${course.tips.size}")
            
            // Find which document actually exists
            var existingDocId: String? = null
            for (docId in possibleDocIds) {
                val doc = firestore.collection(COURSES_COLLECTION)
                    .document(docId)
                    .get()
                    .await()
                if (doc.exists()) {
                    existingDocId = docId
                    Log.d("CourseRepository", "Found existing document: $docId")
                    break
                }
            }
            
            // Use lowercase for new document ID
            val newDocId = course.title.replace(" ", "_").lowercase()
            
            // If title changed or we need to migrate to lowercase, handle accordingly
            if (existingDocId != null && existingDocId != newDocId) {
                // Delete old document (title changed or migrating to lowercase)
                firestore.collection(COURSES_COLLECTION).document(existingDocId).delete().await()
                Log.d("CourseRepository", "Deleted old document: $existingDocId")
            }
            
            // Set the course data (create or update)
            firestore.collection(COURSES_COLLECTION)
                .document(newDocId)
                .set(course)
                .await()
                
            Log.d("CourseRepository", "Successfully updated course: ${course.title} with ${course.tips.size} tips at $newDocId")
            true
        } catch (e: Exception) {
            Log.e("CourseRepository", "Error updating course", e)
            false
        }
    }
    
    // Delete a course
    suspend fun deleteCourse(courseTitle: String): Boolean {
        return try {
            // Try to find existing document - old courses may not be lowercase
            val possibleDocIds = listOf(
                courseTitle.replace(" ", "_"),           // Original format (mixed case)
                courseTitle.replace(" ", "_").lowercase() // New format (lowercase)
            )
            
            // Find and delete whichever document exists
            var deleted = false
            for (docId in possibleDocIds) {
                val doc = firestore.collection(COURSES_COLLECTION)
                    .document(docId)
                    .get()
                    .await()
                if (doc.exists()) {
                    firestore.collection(COURSES_COLLECTION)
                        .document(docId)
                        .delete()
                        .await()
                    Log.d("CourseRepository", "Deleted course: $courseTitle (docId: $docId)")
                    deleted = true
                    break
                }
            }
            
            if (!deleted) {
                Log.w("CourseRepository", "Course not found for deletion: $courseTitle")
            }
            
            true
        } catch (e: Exception) {
            Log.e("CourseRepository", "Error deleting course", e)
            false
        }
    }
    
    // Get course by document ID
    suspend fun getCourseById(docId: String): FirebaseCourseContent? {
        return try {
            val doc = firestore.collection(COURSES_COLLECTION)
                .document(docId)
                .get()
                .await()
            doc.toObject(FirebaseCourseContent::class.java)
        } catch (e: Exception) {
            Log.e("CourseRepository", "Error fetching course by ID: $docId", e)
            null
        }
    }
}


