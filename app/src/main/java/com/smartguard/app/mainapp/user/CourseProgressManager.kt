package com.smartguard.app.mainapp.user

import android.content.Context
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object CourseProgressManager {
    private const val PREFS_NAME = "course_progress"
    private const val COMPLETED_COURSES_KEY = "completed_courses"
    
    private var context: Context? = null
    private val completedCourses: SnapshotStateMap<String, Boolean> = mutableStateMapOf()
    private var isInitialized = false
    
    fun initialize(appContext: Context) {
        if (isInitialized) return
        context = appContext.applicationContext
        // Load progress immediately but don't block
        val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedCourses = prefs.getStringSet(COMPLETED_COURSES_KEY, emptySet()) ?: emptySet()
        completedCourses.clear()
        savedCourses.forEach { courseTitle ->
            completedCourses[courseTitle] = true
        }
        isInitialized = true
    }
    
    private fun saveProgress() {
        context?.let { ctx ->
            // Use apply() instead of commit() to avoid blocking
            val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val courseTitles = completedCourses.filter { it.value }.keys
            prefs.edit().putStringSet(COMPLETED_COURSES_KEY, courseTitles).apply()
        }
    }
    
    fun markCourseCompleted(courseTitle: String) {
        completedCourses[courseTitle] = true
        saveProgress()
    }
    
    fun isCourseCompleted(courseTitle: String): Boolean {
        return completedCourses[courseTitle] ?: false
    }
    
    fun getCompletedCoursesCount(): Int {
        return completedCourses.count { it.value }
    }
    
    fun getAllCompletedCourses(): List<String> {
        return completedCourses.filter { it.value }.keys.toList()
    }
    
    fun resetProgress() {
        completedCourses.clear()
        saveProgress()
    }
    
    // Calculate progress stats
    fun getProgressStats(allCourses: List<com.smartguard.app.model.ScamCourse>): ProgressStats {
        val totalCourses = allCourses.size
        val completed = completedCourses.count { it.value }
        val inProgress = 0 // We can enhance this later to track partially completed courses
        val available = totalCourses - completed
        
        return ProgressStats(
            completed = completed,
            inProgress = inProgress,
            available = available
        )
    }
}

data class ProgressStats(
    val completed: Int,
    val inProgress: Int,
    val available: Int
)

