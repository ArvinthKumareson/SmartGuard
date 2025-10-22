package com.smartguard.app.model

data class WebsiteScanResult(
    val url: String,
    val isSafe: Boolean,
    val maliciousCount: Int,
    val suspiciousCount: Int,
    val harmlessCount: Int,
    val undetectedCount: Int,
    val reputation: Int,
    val finalUrl: String?,
    val title: String?
)

sealed class ScanState {
    object Idle : ScanState()
    object Loading : ScanState()
    data class Success(val result: WebsiteScanResult) : ScanState()
    data class Error(val message: String) : ScanState()
}

