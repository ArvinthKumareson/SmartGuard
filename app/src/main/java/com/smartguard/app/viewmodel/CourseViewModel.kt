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
import com.smartguard.app.util.MediaResourceHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CourseUiState(
    val isLoading: Boolean = true,
    val courses: List<FirebaseCourseContent> = emptyList(),
    val progress: UserCourseProgress? = null,
    val error: String? = null
)

class CourseViewModel(application: Application) : AndroidViewModel(application) {
    
    private val context = application.applicationContext
    private val _uiState = MutableStateFlow(CourseUiState())
    val uiState: StateFlow<CourseUiState> = _uiState.asStateFlow()
    
    init {
        loadCourses()
        loadProgress()
    }
    
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
            }
        }
    }
    
    fun isCourseCompleted(courseTitle: String): Boolean {
        return _uiState.value.progress?.completedCourses?.contains(courseTitle) ?: false
    }
    
    fun getProgressStats(): Triple<Int, Int, Int> {
        val progress = _uiState.value.progress
        val totalCourses = _uiState.value.courses.size
        val completed = progress?.completedCourses?.size ?: 0
        val inProgress = 0 // Can be enhanced later
        val available = totalCourses - completed
        
        return Triple(completed, inProgress, available)
    }
    
    // Convert Firebase course to ScamCourse for compatibility
    fun toScamCourses(): List<ScamCourse> {
        return _uiState.value.courses.map { fbCourse ->
            ScamCourse(
                title = fbCourse.title,
                description = fbCourse.description,
                level = fbCourse.level,
                rating = fbCourse.rating,
                isNew = fbCourse.isNew
            )
        }
    }
    
    // Get course content for CourseDetailScreen
    fun getCourseContent(courseTitle: String): CourseContent? {
        val fbCourse = _uiState.value.courses.find { it.title == courseTitle } ?: return null
        
        return CourseContent(
            title = fbCourse.title,
            description = fbCourse.description,
            level = fbCourse.level,
            rating = fbCourse.rating,
            tips = fbCourse.tips.map { fbTip ->
                CourseTip(
                    title = fbTip.title,
                    description = fbTip.description,
                    icon = getIconFromName(fbTip.iconName),
                    isImportant = fbTip.isImportant,
                    detailedContent = fbTip.detailedContent,
                    actionSteps = fbTip.actionSteps,
                    imageResId = fbTip.imageUrl?.let { 
                        // Try to parse as int first (backward compatibility)
                        it.toIntOrNull() ?: 
                        // Otherwise treat as resource name and look it up
                        MediaResourceHelper.getImageResourceId(context, it)?.toIntOrNull()
                    },
                    videoUri = fbTip.videoUrl?.let {
                        // If it's already a full URI, use it
                        if (it.startsWith("android.resource://")) {
                            it
                        } else {
                            // Otherwise treat as resource name and look it up
                            MediaResourceHelper.getVideoUriByName(context, it)
                        }
                    }
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


