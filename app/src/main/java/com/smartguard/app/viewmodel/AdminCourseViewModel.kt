package com.smartguard.app.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.smartguard.app.data.CourseRepository
import com.smartguard.app.data.FirebaseCourseContent
import com.smartguard.app.data.FirebaseCourseTip
import com.smartguard.app.util.CloudinaryStorageHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI state for the admin course management screen.
 *
 * Holds loading flags, current list of courses, and any transient error or
 * success messages to show in the UI.
 */
data class AdminCourseUiState(
    val isLoading: Boolean = false,
    val courses: List<FirebaseCourseContent> = emptyList(),
    val error: String? = null,
    val successMessage: String? = null,
    val isUploading: Boolean = false
)

/**
 * ViewModel for admin features related to scam awareness courses.
 *
 * Responsibilities:
 *  - Load all courses from [CourseRepository].
 *  - Create, update and delete course documents in Firestore.
 *  - Upload course-related images/videos via [CloudinaryStorageHelper].
 */
class AdminCourseViewModel(application: Application) : AndroidViewModel(application) {
    
    private val _uiState = MutableStateFlow(AdminCourseUiState())
    val uiState: StateFlow<AdminCourseUiState> = _uiState.asStateFlow()
    
    init {
        Log.d("AdminCourseViewModel", "ViewModel initialized")
        // Eagerly load courses for the admin dashboard.
        loadCourses()
    }
    
    /**
     * Fetches all course definitions from Firestore via [CourseRepository].
     */
    fun loadCourses() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                Log.d("AdminCourseViewModel", "Loading courses...")
                val courses = CourseRepository.getAllCourses()
                Log.d("AdminCourseViewModel", "Loaded ${courses.size} courses")
                _uiState.value = _uiState.value.copy(
                    courses = courses,
                    isLoading = false
                )
            } catch (e: Exception) {
                Log.e("AdminCourseViewModel", "Error loading courses", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load courses: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Creates a new course from the provided fields and persists it using
     * [CourseRepository]. On success, reloads the course list.
     */
    fun createCourse(
        title: String,
        description: String,
        level: String,
        isNew: Boolean,
        tips: List<FirebaseCourseTip>
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, successMessage = null)
            
            try {
                val course = FirebaseCourseContent(
                    title = title,
                    description = description,
                    level = level,
                    isNew = isNew,
                    tips = tips
                )
                
                val success = CourseRepository.createCourse(course)
                
                if (success) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Course created successfully!"
                    )
                    loadCourses() // Reload courses
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to create course"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error creating course: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Updates an existing course identified by [oldTitle] with new content.
     */
    fun updateCourse(
        oldTitle: String,
        title: String,
        description: String,
        level: String,
        isNew: Boolean,
        tips: List<FirebaseCourseTip>
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, successMessage = null)
            
            try {
                val course = FirebaseCourseContent(
                    title = title,
                    description = description,
                    level = level,
                    isNew = isNew,
                    tips = tips
                )
                
                val success = CourseRepository.updateCourse(oldTitle, course)
                
                if (success) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Course updated successfully!"
                    )
                    loadCourses() // Reload courses
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to update course"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error updating course: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Deletes the course with the given [courseTitle] from Firestore.
     */
    fun deleteCourse(courseTitle: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, successMessage = null)
            
            try {
                val success = CourseRepository.deleteCourse(courseTitle)
                
                if (success) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Course deleted successfully!"
                    )
                    loadCourses() // Reload courses
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to delete course"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error deleting course: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Upload an image and return its URL
     */
    suspend fun uploadImage(imageUri: Uri): String? {
        _uiState.value = _uiState.value.copy(isUploading = true)
        
        return try {
            val url = CloudinaryStorageHelper.uploadImage(getApplication(), imageUri)
            _uiState.value = _uiState.value.copy(isUploading = false)
            url
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isUploading = false,
                error = "Failed to upload image: ${e.message}"
            )
            null
        }
    }
    
    /**
     * Upload a video and return its URL
     */
    suspend fun uploadVideo(videoUri: Uri): String? {
        _uiState.value = _uiState.value.copy(isUploading = true)
        
        return try {
            val url = CloudinaryStorageHelper.uploadVideo(getApplication(), videoUri)
            _uiState.value = _uiState.value.copy(isUploading = false)
            url
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isUploading = false,
                error = "Failed to upload video: ${e.message}"
            )
            null
        }
    }
    
    /**
     * Clears transient error/success messages after they have been shown.
     */
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }
}

