package com.smartguard.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartguard.app.data.WebsiteScanRepository
import com.smartguard.app.model.WebsiteScanHistory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Sealed UI state used by [ScanHistoryViewModel] to represent website
 * scan history loading status.
 */
sealed class HistoryState {
    object Loading : HistoryState()
    data class Success(val scans: List<WebsiteScanHistory>) : HistoryState()
    data class Error(val message: String) : HistoryState()
}

/**
 * ViewModel that exposes the current user's website scan history.
 *
 * It delegates persistence to [WebsiteScanRepository] and maps repository
 * results into a simple [HistoryState] for the UI.
 */
class ScanHistoryViewModel : ViewModel() {
    
    private val repository = WebsiteScanRepository()
    
    private val _historyState = MutableStateFlow<HistoryState>(HistoryState.Loading)
    val historyState: StateFlow<HistoryState> = _historyState

    init {
        loadHistory()
    }

    /**
     * Triggers a reload of website scan history from Firestore.
     */
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

    /**
     * Deletes a single scan entry and refreshes the list.
     */
    fun deleteScan(scanId: String) {
        viewModelScope.launch {
            repository.deleteScan(scanId)
            loadHistory()
        }
    }

    /**
     * Clears all website scan entries for the current user.
     */
    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAllScans()
            loadHistory()
        }
    }
}

