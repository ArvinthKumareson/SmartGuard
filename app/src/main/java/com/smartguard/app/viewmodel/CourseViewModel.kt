package com.smartguard.app.viewmodel

import android.app.Application
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.smartguard.app.data.CourseRepository
import com.smartguard.app.data.FirebaseCourseContent
import com.smartguard.app.data.UserCourseProgress
import com.smartguard.app.mainapp.user.CourseContent
import com.smartguard.app.mainapp.user.CourseTip
import com.smartguard.app.model.ScamCourse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI state for the user course dashboard.
 */
data class CourseUiState(
    val isLoading: Boolean = true,
    val courses: List<FirebaseCourseContent> = emptyList(),
    val progress: UserCourseProgress? = null,
    val error: String? = null
)

/**
 * ViewModel that provides scam awareness course content to the user side.
 *
 * It loads all courses and the current user's progress from [CourseRepository]
 * and exposes convenience helpers for marking courses as completed and
 * mapping Firebase models into UI models.
 */
class CourseViewModel(application: Application) : AndroidViewModel(application) {
    
    private val context = application.applicationContext
    private val _uiState = MutableStateFlow(CourseUiState())
    val uiState: StateFlow<CourseUiState> = _uiState.asStateFlow()
    
    init {
        // Load both course list and user progress when the ViewModel is created.
        loadCourses()
        loadProgress()
    }
    
    /**
     * Loads all available courses from Firestore.
     */
    fun loadCourses() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val courses = CourseRepository.getAllCourses()
                _uiState.value = _uiState.value.copy(
                    courses = courses,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load courses: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Loads the current user's progress (completed courses) from Firestore.
     */
    fun loadProgress() {
        viewModelScope.launch {
            try {
                val progress = CourseRepository.getUserProgress()
                _uiState.value = _uiState.value.copy(progress = progress)
            } catch (e: Exception) {
                // Silent fail - progress is optional
            }
        }
    }
    
    /**
     * Marks a course as completed, updating local state optimistically
     * and then persisting the change via [CourseRepository].
     */
    fun markCourseCompleted(courseTitle: String) {
        viewModelScope.launch {
            // Optimistically update local state first for immediate UI feedback
            val currentProgress = _uiState.value.progress
            if (currentProgress != null) {
                val updatedCourses = (currentProgress.completedCourses + courseTitle).distinct()
                val updatedProgress = currentProgress.copy(
                    completedCourses = updatedCourses,
                    lastUpdated = System.currentTimeMillis()
                )
                _uiState.value = _uiState.value.copy(progress = updatedProgress)
            }
            
            // Then update Firebase in the background
            val success = CourseRepository.markCourseCompleted(courseTitle)
            if (!success) {
                // If failed, reload to get correct state
                loadProgress()
            } else {
                // Force refresh to ensure UI updates everywhere
                refreshProgress()
            }
        }
    }
    
    fun refreshProgress() {
        // Trigger a manual refresh of progress
        loadProgress()
    }
    
    /**
     * Refreshes both the course list and the user's progress.
     */
    fun refresh() {
        // Refresh both courses and progress
        loadCourses()
        loadProgress()
    }
    
    /**
     * Returns whether the given course has been marked as completed by the user.
     */
    fun isCourseCompleted(courseTitle: String): Boolean {
        return _uiState.value.progress?.completedCourses?.contains(courseTitle) ?: false
    }
    
    /**
     * Returns a triple of (completed, in-progress, available) course counts
     * for simple progress summaries in the UI.
     */
    fun getProgressStats(): Triple<Int, Int, Int> {
        val progress = _uiState.value.progress
        val totalCourses = _uiState.value.courses.size
        val completed = progress?.completedCourses?.size ?: 0
        val inProgress = 0 // Can be enhanced later
        val available = totalCourses - completed
        
        return Triple(completed, inProgress, available)
    }
    
    // Convert Firebase course to ScamCourse for compatibility with existing components
    fun toScamCourses(): List<ScamCourse> {
        return _uiState.value.courses.map { fbCourse ->
            ScamCourse(
                title = fbCourse.title,
                description = fbCourse.description,
                level = fbCourse.level,
                isNew = fbCourse.isNew
            )
        }
    }
    
    // Get course content for CourseDetailScreen by mapping Firebase model into a UI-friendly structure
    fun getCourseContent(courseTitle: String): CourseContent? {
        val fbCourse = _uiState.value.courses.find { it.title == courseTitle } ?: return null
        
        return CourseContent(
            title = fbCourse.title,
            description = fbCourse.description,
            level = fbCourse.level,
            tips = fbCourse.tips.map { fbTip ->
                CourseTip(
                    title = fbTip.title,
                    description = fbTip.description,
                    icon = getIconFromName(fbTip.iconName),
                    isImportant = fbTip.isImportant,
                    detailedContent = fbTip.detailedContent,
                    actionSteps = fbTip.actionSteps,
                    imageUrl = fbTip.imageUrl, // Pass URL directly for cloud storage
                    videoUri = fbTip.videoUrl // Pass URL directly for cloud storage
                )
            },
            isNew = fbCourse.isNew
        )
    }
    
    // Map icon name string to ImageVector
    private fun getIconFromName(iconName: String): ImageVector {
        return when (iconName) {
            "Phone" -> Icons.Default.Phone
            "Verified" -> Icons.Default.Verified
            "Link" -> Icons.Default.Link
            "Spellcheck" -> Icons.Default.Spellcheck
            "Warning" -> Icons.Default.Warning
            "Call" -> Icons.Default.Call
            "Lock" -> Icons.Default.Lock
            "PersonOff" -> Icons.Default.PersonOff
            "Key" -> Icons.Default.Key
            "Security" -> Icons.Default.Security
            "Update" -> Icons.Default.Update
            "VerifiedUser" -> Icons.Default.VerifiedUser
            "Email" -> Icons.Default.Email
            "Schedule" -> Icons.Default.Schedule
            "AttachFile" -> Icons.Default.AttachFile
            "CreditCard" -> Icons.Default.CreditCard
            "WifiOff" -> Icons.Default.WifiOff
            "Undo" -> Icons.Default.Undo
            "AccountBalance" -> Icons.Default.AccountBalance
            "PhoneDisabled" -> Icons.Default.PhoneDisabled
            "Visibility" -> Icons.Default.Visibility
            "Search" -> Icons.Default.Search
            "Block" -> Icons.Default.Block
            "VideoCall" -> Icons.Default.VideoCall
            "RecordVoiceOver" -> Icons.Default.RecordVoiceOver
            "TextFields" -> Icons.Default.TextFields
            "Image" -> Icons.Default.Image
            "Psychology" -> Icons.Default.Psychology
            "Wifi" -> Icons.Default.Wifi
            "VpnKey" -> Icons.Default.VpnKey
            "Computer" -> Icons.Default.Computer
            "VisibilityOff" -> Icons.Default.VisibilityOff
            "Share" -> Icons.Default.Share
            "NetworkCheck" -> Icons.Default.NetworkCheck
            "Settings" -> Icons.Default.Settings
            "Monitor" -> Icons.Default.Monitor
            "Router" -> Icons.Default.Router
            "PhoneAndroid" -> Icons.Default.PhoneAndroid
            "Backup" -> Icons.Default.Backup
            "Devices" -> Icons.Default.Devices
            "Sms" -> Icons.Default.Sms
            "Notifications" -> Icons.Default.Notifications
            "Report" -> Icons.Default.Report
            "Info" -> Icons.Default.Info
            "QuestionMark" -> Icons.Default.QuestionMark
            else -> Icons.Default.Info // Default fallback icon
        }
    }
}


