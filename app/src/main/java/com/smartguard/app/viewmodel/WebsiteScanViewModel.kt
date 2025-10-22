package com.smartguard.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartguard.app.api.VirusTotalService
import com.smartguard.app.data.WebsiteScanRepository
import com.smartguard.app.model.ScanState
import com.smartguard.app.model.WebsiteScanResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.net.URL
import java.util.Base64

class WebsiteScanViewModel : ViewModel() {
    
    private val _scanState = MutableStateFlow<ScanState>(ScanState.Idle)
    val scanState: StateFlow<ScanState> = _scanState

    private val repository = WebsiteScanRepository()
    private val apiKey = "d3f767faa1898e94b56bc3761828e10a49ac5d70b9c689dedafc0ed1a19b76e1"

    fun scanWebsite(urlString: String) {
        viewModelScope.launch {
            try {
                _scanState.value = ScanState.Loading

                val normalizedUrl = normalizeUrl(urlString)
                
                if (!isValidUrl(normalizedUrl)) {
                    _scanState.value = ScanState.Error("Invalid URL format. Try: example.com or https://example.com")
                    return@launch
                }

                val urlId = encodeUrlForVirusTotal(normalizedUrl)
                
                val existingReport = VirusTotalService.api.checkUrlReport(apiKey, urlId)
                
                if (existingReport.isSuccessful && existingReport.body()?.data?.attributes != null) {
                    val report = existingReport.body()?.data?.attributes!!
                    processReport(normalizedUrl, report)
                    return@launch
                }

                val scanResponse = VirusTotalService.api.scanUrl(apiKey, normalizedUrl)

                if (!scanResponse.isSuccessful) {
                    val errorMsg = when (scanResponse.code()) {
                        401 -> "API key invalid. Get a free key from virustotal.com"
                        429 -> "Rate limit: 4 requests/min. Wait a moment"
                        403 -> "API access forbidden. Check your key"
                        400 -> "Invalid URL format"
                        else -> "Scan failed (${scanResponse.code()})"
                    }
                    _scanState.value = ScanState.Error(errorMsg)
                    return@launch
                }
                
                if (scanResponse.body() == null) {
                    _scanState.value = ScanState.Error("No response data received")
                    return@launch
                }

                val analysisId = scanResponse.body()?.data?.id
                if (analysisId == null) {
                    _scanState.value = ScanState.Error("No analysis ID received")
                    return@launch
                }

                delay(10000)

                var reportResponse = VirusTotalService.api.getUrlReport(apiKey, analysisId)
                var attempts = 0
                
                while ((!reportResponse.isSuccessful || reportResponse.body()?.data?.attributes == null) && attempts < 12) {
                    delay(5000)
                    attempts++
                    reportResponse = VirusTotalService.api.getUrlReport(apiKey, analysisId)
                }

                if (!reportResponse.isSuccessful || reportResponse.body() == null) {
                    _scanState.value = ScanState.Error("Scan queued. This URL will be ready in 1-2 minutes. Please try again shortly")
                    return@launch
                }

                val report = reportResponse.body()?.data?.attributes
                if (report == null) {
                    _scanState.value = ScanState.Error("No scan data available yet. Please retry")
                    return@launch
                }

                processReport(normalizedUrl, report)

            } catch (e: Exception) {
                _scanState.value = ScanState.Error(e.message ?: "Network error occurred")
            }
        }
    }
    
    private suspend fun processReport(url: String, report: com.smartguard.app.api.ReportAttributes) {
        val stats = report.last_analysis_stats
        val malicious = stats?.malicious ?: 0
        val suspicious = stats?.suspicious ?: 0
        val harmless = stats?.harmless ?: 0
        val undetected = stats?.undetected ?: 0
        val reputation = report.reputation ?: 0

        val isSafe = malicious == 0 && suspicious == 0

        val result = WebsiteScanResult(
            url = url,
            isSafe = isSafe,
            maliciousCount = malicious,
            suspiciousCount = suspicious,
            harmlessCount = harmless,
            undetectedCount = undetected,
            reputation = reputation,
            finalUrl = report.last_final_url,
            title = report.title
        )

        repository.saveScan(result)
        _scanState.value = ScanState.Success(result)
    }
    
    private fun encodeUrlForVirusTotal(url: String): String {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(url.toByteArray())
    }

    private fun normalizeUrl(url: String): String {
        var normalized = url.trim()
        
        normalized = normalized.removePrefix("@")
        normalized = normalized.removePrefix("www.")
        
        if (normalized.startsWith("http://") || normalized.startsWith("https://")) {
            return normalized
        }
        
        return "https://$normalized"
    }

    private fun isValidUrl(url: String): Boolean {
        return try {
            val urlObj = URL(url)
            val host = urlObj.host
            host.isNotEmpty() && host.contains(".")
        } catch (e: Exception) {
            false
        }
    }

    fun resetState() {
        _scanState.value = ScanState.Idle
    }
}

