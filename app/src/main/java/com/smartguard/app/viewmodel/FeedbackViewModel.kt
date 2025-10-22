package com.smartguard.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartguard.app.data.FeedbackRepository
import com.smartguard.app.model.UserFeedback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class FeedbackSubmitState {
    object Idle : FeedbackSubmitState()
    object Loading : FeedbackSubmitState()
    object Success : FeedbackSubmitState()
    data class Error(val message: String) : FeedbackSubmitState()
}

sealed class FeedbackListState {
    object Loading : FeedbackListState()
    data class Success(val feedback: List<UserFeedback>) : FeedbackListState()
    data class Error(val message: String) : FeedbackListState()
}

class FeedbackViewModel : ViewModel() {
    
    private val repository = FeedbackRepository()
    
    private val _submitState = MutableStateFlow<FeedbackSubmitState>(FeedbackSubmitState.Idle)
    val submitState: StateFlow<FeedbackSubmitState> = _submitState
    
    private val _userFeedbackState = MutableStateFlow<FeedbackListState>(FeedbackListState.Loading)
    val userFeedbackState: StateFlow<FeedbackListState> = _userFeedbackState
    
    private val _allFeedbackState = MutableStateFlow<FeedbackListState>(FeedbackListState.Loading)
    val allFeedbackState: StateFlow<FeedbackListState> = _allFeedbackState

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

    fun updateStatus(feedbackId: String, status: String, response: String? = null) {
        viewModelScope.launch {
            repository.updateFeedbackStatus(feedbackId, status, response)
            loadAllFeedback()
        }
    }

    fun deleteFeedback(feedbackId: String) {
        viewModelScope.launch {
            repository.deleteFeedback(feedbackId)
            loadUserFeedback()
        }
    }

    fun resetSubmitState() {
        _submitState.value = FeedbackSubmitState.Idle
    }
}

