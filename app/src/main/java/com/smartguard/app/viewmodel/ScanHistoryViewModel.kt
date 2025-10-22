package com.smartguard.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartguard.app.data.WebsiteScanRepository
import com.smartguard.app.model.WebsiteScanHistory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class HistoryState {
    object Loading : HistoryState()
    data class Success(val scans: List<WebsiteScanHistory>) : HistoryState()
    data class Error(val message: String) : HistoryState()
}

class ScanHistoryViewModel : ViewModel() {
    
    private val repository = WebsiteScanRepository()
    
    private val _historyState = MutableStateFlow<HistoryState>(HistoryState.Loading)
    val historyState: StateFlow<HistoryState> = _historyState

    init {
        loadHistory()
    }

    fun loadHistory() {
        viewModelScope.launch {
            _historyState.value = HistoryState.Loading
            
            val result = repository.getUserScans()
            
            result.onSuccess { scans ->
                _historyState.value = HistoryState.Success(scans)
            }.onFailure { error ->
                _historyState.value = HistoryState.Error(error.message ?: "Failed to load history")
            }
        }
    }

    fun deleteScan(scanId: String) {
        viewModelScope.launch {
            repository.deleteScan(scanId)
            loadHistory()
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAllScans()
            loadHistory()
        }
    }
}

