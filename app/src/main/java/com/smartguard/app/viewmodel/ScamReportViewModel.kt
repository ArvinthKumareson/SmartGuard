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
import kotlinx.coroutines.launch

sealed class ReportSubmitState {
    object Idle : ReportSubmitState()
    object Loading : ReportSubmitState()
    object Success : ReportSubmitState()
    data class Error(val message: String) : ReportSubmitState()
}

sealed class ReportsListState {
    object Loading : ReportsListState()
    data class Success(val reports: List<ScamReport>) : ReportsListState()
    data class Error(val message: String) : ReportsListState()
    object Empty : ReportsListState()
}

sealed class CommentsState {
    object Loading : CommentsState()
    data class Success(val comments: List<ScamComment>) : CommentsState()
    data class Error(val message: String) : CommentsState()
    object Empty : CommentsState()
}

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

    init {
        loadApprovedReports()
    }

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

    fun resetSubmitState() {
        _submitState.value = ReportSubmitState.Idle
    }

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

    fun loadComments(reportId: String) {
        viewModelScope.launch {
            repository.getReportComments(reportId).collectLatest { comments ->
                if (comments.isEmpty()) {
                    _commentsState.value = CommentsState.Empty
                } else {
                    _commentsState.value = CommentsState.Success(comments)
                }
            }
        }
    }

    fun addComment(reportId: String, comment: String) {
        viewModelScope.launch {
            repository.addComment(reportId, comment)
        }
    }

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

    fun updateReportStatus(reportId: String, status: String, moderatorNote: String? = null) {
        viewModelScope.launch {
            repository.updateReportStatus(reportId, status, moderatorNote)
        }
    }

    fun deleteReport(reportId: String) {
        viewModelScope.launch {
            repository.deleteReport(reportId)
        }
    }

    fun deleteComment(commentId: String, reportId: String) {
        viewModelScope.launch {
            repository.deleteComment(commentId, reportId)
        }
    }
}

