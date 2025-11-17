package com.smartguard.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartguard.app.data.FeedbackRepository
import com.smartguard.app.model.UserFeedback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * UI state for the feedback submission process.
 */
sealed class FeedbackSubmitState {
    object Idle : FeedbackSubmitState()
    object Loading : FeedbackSubmitState()
    object Success : FeedbackSubmitState()
    data class Error(val message: String) : FeedbackSubmitState()
}

/**
 * UI state for lists of feedback (either per-user or all feedback).
 */
sealed class FeedbackListState {
    object Loading : FeedbackListState()
    data class Success(val feedback: List<UserFeedback>) : FeedbackListState()
    data class Error(val message: String) : FeedbackListState()
}

/**
 * ViewModel that coordinates user feedback and admin feedback views.
 *
 * Uses [FeedbackRepository] to submit feedback and to load both the
 * current user's feedback and the complete feedback list for admins.
 */
class FeedbackViewModel : ViewModel() {
    
    private val repository = FeedbackRepository()
    
    private val _submitState = MutableStateFlow<FeedbackSubmitState>(FeedbackSubmitState.Idle)
    val submitState: StateFlow<FeedbackSubmitState> = _submitState
    
    private val _userFeedbackState = MutableStateFlow<FeedbackListState>(FeedbackListState.Loading)
    val userFeedbackState: StateFlow<FeedbackListState> = _userFeedbackState
    
    private val _allFeedbackState = MutableStateFlow<FeedbackListState>(FeedbackListState.Loading)
    val allFeedbackState: StateFlow<FeedbackListState> = _allFeedbackState

    /**
     * Submits a new feedback entry and updates [_submitState] based on the result.
     */
    fun submitFeedback(category: String, subject: String, message: String, rating: Int) {
        viewModelScope.launch {
            _submitState.value = FeedbackSubmitState.Loading
            
            val result = repository.submitFeedback(category, subject, message, rating)
            
            result.onSuccess {
                _submitState.value = FeedbackSubmitState.Success
            }.onFailure { error ->
                _submitState.value = FeedbackSubmitState.Error(error.message ?: "Failed to submit feedback")
            }
        }
    }

    /**
     * Loads feedback entries submitted by the current user.
     */
    fun loadUserFeedback() {
        viewModelScope.launch {
            _userFeedbackState.value = FeedbackListState.Loading
            
            val result = repository.getUserFeedback()
            
            result.onSuccess { feedback ->
                _userFeedbackState.value = FeedbackListState.Success(feedback)
            }.onFailure { error ->
                _userFeedbackState.value = FeedbackListState.Error(error.message ?: "Failed to load feedback")
            }
        }
    }

    /**
     * Loads all feedback entries for admin review.
     */
    fun loadAllFeedback() {
        viewModelScope.launch {
            _allFeedbackState.value = FeedbackListState.Loading
            
            val result = repository.getAllFeedback()
            
            result.onSuccess { feedback ->
                _allFeedbackState.value = FeedbackListState.Success(feedback)
            }.onFailure { error ->
                _allFeedbackState.value = FeedbackListState.Error(error.message ?: "Failed to load feedback")
            }
        }
    }

    /**
     * Updates the status/response of a feedback entry (admin only operation).
     */
    fun updateStatus(feedbackId: String, status: String, response: String? = null) {
        viewModelScope.launch {
            repository.updateFeedbackStatus(feedbackId, status, response)
            loadAllFeedback()
        }
    }

    /**
     * Deletes a feedback entry created by the current user.
     */
    fun deleteFeedback(feedbackId: String) {
        viewModelScope.launch {
            repository.deleteFeedback(feedbackId)
            loadUserFeedback()
        }
    }

    /**
     * Resets the submit state to Idle after showing success/error.
     */
    fun resetSubmitState() {
        _submitState.value = FeedbackSubmitState.Idle
    }
}

