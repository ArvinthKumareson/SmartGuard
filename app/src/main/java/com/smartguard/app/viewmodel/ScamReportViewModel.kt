package com.smartguard.app.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartguard.app.data.ScamReportRepository
import com.smartguard.app.model.ScamComment
import com.smartguard.app.model.ScamReport
import com.smartguard.app.util.CloudinaryStorageHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch

/**
 * UI state for submitting a scam report.
 */
sealed class ReportSubmitState {
    object Idle : ReportSubmitState()
    object Loading : ReportSubmitState()
    object Success : ReportSubmitState()
    data class Error(val message: String) : ReportSubmitState()
}

/**
 * UI state for scam report lists (approved list and admin list).
 */
sealed class ReportsListState {
    object Loading : ReportsListState()
    data class Success(val reports: List<ScamReport>) : ReportsListState()
    data class Error(val message: String) : ReportsListState()
    object Empty : ReportsListState()
}

/**
 * UI state for comments attached to a scam report.
 */
sealed class CommentsState {
    object Loading : CommentsState()
    data class Success(val comments: List<ScamComment>) : CommentsState()
    data class Error(val message: String) : CommentsState()
    object Empty : CommentsState()
}

/**
 * ViewModel orchestrating scam report submission and moderation flows.
 *
 * It uses [ScamReportRepository] to submit new reports (with optional
 * media upload), load approved reports for public display, and support
 * admin operations like updating status, deleting reports and handling
 * comments/likes.
 */
class ScamReportViewModel : ViewModel() {
    val repository = ScamReportRepository()

    private val _submitState = MutableStateFlow<ReportSubmitState>(ReportSubmitState.Idle)
    val submitState: StateFlow<ReportSubmitState> = _submitState

    private val _approvedReportsState = MutableStateFlow<ReportsListState>(ReportsListState.Loading)
    val approvedReportsState: StateFlow<ReportsListState> = _approvedReportsState

    private val _allReportsState = MutableStateFlow<ReportsListState>(ReportsListState.Loading)
    val allReportsState: StateFlow<ReportsListState> = _allReportsState

    private val _commentsState = MutableStateFlow<CommentsState>(CommentsState.Loading)
    val commentsState: StateFlow<CommentsState> = _commentsState

    // Tracks the currently active Firestore listener for comments so that we
    // don't have multiple report comment streams writing into the same state.
    private var commentsJob: kotlinx.coroutines.Job? = null
    private var currentCommentReportId: String? = null
    
    // Separate flows per report to keep listeners alive
    private val commentFlows = mutableMapOf<String, StateFlow<List<ScamComment>>>()

    init {
        loadApprovedReports()
    }

    /**
     * Submits a new scam report, optionally uploading an image to Cloudinary.
     *
     * The result is reflected via [_submitState] for the UI to react.
     */
    fun submitReport(
        context: Context,
        title: String,
        description: String,
        scamType: String,
        amount: String,
        platform: String,
        postAsAnonymous: Boolean = false,
        imageUri: Uri? = null
    ) {
        viewModelScope.launch {
            _submitState.value = ReportSubmitState.Loading
            try {
                var imageUrl: String? = null
                if (imageUri != null) {
                    imageUrl = CloudinaryStorageHelper.uploadScamReportImage(context, imageUri)
                    if (imageUrl == null) {
                        _submitState.value = ReportSubmitState.Error("Failed to upload image")
                        return@launch
                    }
                }
                val result = repository.submitReport(title, description, scamType, amount, platform, postAsAnonymous, imageUrl)
                result.fold(
                    onSuccess = { _submitState.value = ReportSubmitState.Success },
                    onFailure = { _submitState.value = ReportSubmitState.Error(it.message ?: "Failed to submit report") }
                )
            } catch (e: Exception) {
                Log.e("ScamReportViewModel", "Error submitting report", e)
                _submitState.value = ReportSubmitState.Error(e.message ?: "Unknown error")
            }
        }
    }

    /**
     * Resets the report submission state to Idle.
     */
    fun resetSubmitState() {
        _submitState.value = ReportSubmitState.Idle
    }

    /**
     * Loads approved reports for the public feed, with multiple fallbacks in
     * case of Firestore index or query issues.
     */
    fun loadApprovedReports() {
        viewModelScope.launch {
            Log.d("ScamReportViewModel", "Starting to load approved reports...")
            try {
                repository.getApprovedReports().collectLatest { reports ->
                    Log.d("ScamReportViewModel", "Received ${reports.size} reports from Firebase")
                    if (reports.isEmpty()) {
                        Log.d("ScamReportViewModel", "No reports found, setting empty state")
                        _approvedReportsState.value = ReportsListState.Empty
                    } else {
                        Log.d("ScamReportViewModel", "Setting success state with ${reports.size} reports")
                        _approvedReportsState.value = ReportsListState.Success(reports)
                    }
                }
            } catch (e: Exception) {
                Log.e("ScamReportViewModel", "Error loading approved reports: ${e.message}", e)
                // Fallback 1: try all reports with timestamp order, but filter for approved only
                try {
                    Log.w("ScamReportViewModel", "Falling back to all reports with ordering, filtering approved...")
                    repository.getAllReports().collectLatest { allReports ->
                        val approvedReports = allReports.filter { it.status == "approved" }
                        if (approvedReports.isEmpty()) {
                            _approvedReportsState.value = ReportsListState.Empty
                        } else {
                            _approvedReportsState.value = ReportsListState.Success(approvedReports)
                        }
                    }
                } catch (e2: Exception) {
                    // Fallback 2: try unordered any-status to avoid index/field issues, but filter for approved only
                    try {
                        Log.w("ScamReportViewModel", "Falling back to unordered any-status reports, filtering approved...")
                        repository.getReportsAnyStatusUnordered().collectLatest { allReports ->
                            val approvedReports = allReports.filter { it.status == "approved" }
                            if (approvedReports.isEmpty()) {
                                _approvedReportsState.value = ReportsListState.Empty
                            } else {
                                _approvedReportsState.value = ReportsListState.Success(approvedReports)
                            }
                        }
                    } catch (e3: Exception) {
                        Log.e("ScamReportViewModel", "All fallbacks failed: ${e3.message}", e3)
                        _approvedReportsState.value = ReportsListState.Error(
                            "Failed to load reports. Firestore may need an index for (status + timestamp). Error: ${e3.message}"
                        )
                    }
                }
            }
        }
    }

    /**
     * Loads all reports for admin viewing and moderation.
     */
    fun loadAllReports() {
        viewModelScope.launch {
            try {
                repository.getAllReports().collectLatest { reports ->
                    if (reports.isEmpty()) {
                        _allReportsState.value = ReportsListState.Empty
                    } else {
                        _allReportsState.value = ReportsListState.Success(reports)
                    }
                }
            } catch (e: Exception) {
                _allReportsState.value = ReportsListState.Error(
                    "Failed to load reports. Please check your internet connection and try again. Error: ${e.message}"
                )
            }
        }
    }

    /**
     * Loads comments for a specific report and updates [_commentsState].
     *
     * Uses stateIn to cache comments in a persistent StateFlow per report,
     * so new subscribers always get the latest snapshot immediately.
     * This ensures comments from other devices are visible even if the dialog
     * was closed when they arrived.
     */
    fun loadComments(reportId: String) {
        // Cancel previous listener if switching reports
        if (currentCommentReportId != reportId) {
            commentsJob?.cancel()
            currentCommentReportId = reportId
        }

        // Get or create a cached StateFlow for this report
        val reportCommentsFlow = commentFlows.getOrPut(reportId) {
            repository.getReportComments(reportId)
                .map { comments ->
                    Log.d("CommentsDebug", "reportId=$reportId, comments.size=${comments.size}")
                    comments.forEach { c ->
                        Log.d("CommentsDebug", "commentId=${c.id}, userId=${c.userId}, text=${c.comment}")
                    }
                    comments
                }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = emptyList()
                )
        }

        // Collect from the cached flow and update commentsState
        commentsJob = viewModelScope.launch {
            reportCommentsFlow.collect { comments ->
                _commentsState.value = if (comments.isEmpty()) {
                    CommentsState.Empty
                } else {
                    CommentsState.Success(comments)
                }
            }
        }
    }

    /**
     * Adds a comment to a specific report and immediately refreshes the comments list.
     */
    fun addComment(reportId: String, comment: String) {
        viewModelScope.launch {
            repository.addComment(reportId, comment)
            // Refresh comments immediately so new comment appears instantly
            loadComments(reportId)
        }
    }

    /**
     * Toggles the like status for a report for the current user.
     */
    fun toggleLike(reportId: String) {
        viewModelScope.launch {
            repository.toggleLike(reportId)
            // Force refresh to ensure UI updates immediately
            refreshApprovedReports()
        }
    }
    
    fun refreshApprovedReports() {
        // Trigger a manual refresh by reloading the data
        loadApprovedReports()
    }

    /**
     * Updates the status and optional moderator note for a report.
     */
    fun updateReportStatus(reportId: String, status: String, moderatorNote: String? = null) {
        viewModelScope.launch {
            repository.updateReportStatus(reportId, status, moderatorNote)
        }
    }

    /**
     * Deletes a report (admin-only operation).
     */
    fun deleteReport(reportId: String) {
        viewModelScope.launch {
            repository.deleteReport(reportId)
        }
    }

    /**
     * Deletes a comment from a report.
     */
    fun deleteComment(commentId: String, reportId: String) {
        viewModelScope.launch {
            repository.deleteComment(commentId, reportId)
        }
    }
}

