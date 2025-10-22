@file:OptIn(ExperimentalMaterial3Api::class)

package com.smartguard.app.mainapp.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.smartguard.app.model.ScanState
import com.smartguard.app.model.WebsiteScanResult
import com.smartguard.app.viewmodel.WebsiteScanViewModel
import com.smartguard.app.mainapp.resources.SmartGuardBottomBar

@Composable
fun WebsiteCheckerScreen(nav: NavController) {
    val viewModel: WebsiteScanViewModel = viewModel()
    val scanState by viewModel.scanState.collectAsState()
    var urlInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Website Safety Checker", color = Color.White) },
                actions = {
                    IconButton(onClick = { nav.navigate("scan_history") }) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = "View History",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E1E1E))
            )
        },
        bottomBar = { SmartGuardBottomBar(nav, currentRoute = "website_checker") }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFF121212))
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                "Check if a website is safe before visiting",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = urlInput,
                onValueChange = { urlInput = it },
                label = { Text("Enter website URL", color = Color.Gray) },
                placeholder = { Text("google.com or https://twitter.com", color = Color.DarkGray) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF6200EE),
                    unfocusedBorderColor = Color.Gray,
                    cursorColor = Color.White
                ),
                singleLine = true,
                supportingText = {
                    Text(
                        "Just enter the domain name (no @ or special characters)",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    if (urlInput.isNotBlank()) {
                        viewModel.scanWebsite(urlInput)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE)),
                enabled = urlInput.isNotBlank() && scanState !is ScanState.Loading
            ) {
                Text(
                    if (scanState is ScanState.Loading) "Scanning..." else "Check Website",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
            }

            Spacer(Modifier.height(16.dp))

            OutlinedButton(
                onClick = { nav.navigate("scan_history") },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF6200EE)
                )
            ) {
                Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("View Scan History")
            }

            Spacer(Modifier.height(24.dp))

            when (val state = scanState) {
                is ScanState.Loading -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(24.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = Color(0xFF6200EE))
                            Spacer(Modifier.height(16.dp))
                            Text("Analyzing website security...", color = Color.White, style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(8.dp))
                            Text("This may take up to 60 seconds", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                            Spacer(Modifier.height(4.dp))
                            Text("Checking against 90+ security vendors", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                is ScanState.Success -> {
                    ScanResultCard(state.result, onReset = { viewModel.resetState() })
                }

                is ScanState.Error -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF3E1E1E)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color(0xFFFF5252),
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    "Error",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(state.message, color = Color.White)
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.resetState() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))
                            ) {
                                Text("Try Again", color = Color.White)
                            }
                        }
                    }
                }

                is ScanState.Idle -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color(0xFF2196F3),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "How it works",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "This tool uses VirusTotal to scan URLs against 90+ security vendors worldwide to detect malicious websites.",
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "✓ Checks against 90+ security vendors\n✓ Detects phishing sites\n✓ Identifies malware distribution\n✓ Real-time threat intelligence",
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScanResultCard(result: WebsiteScanResult, onReset: () -> Unit) {
    val safetyColor = if (result.isSafe) Color(0xFF4CAF50) else Color(0xFFFF5252)
    val safetyIcon = if (result.isSafe) Icons.Default.CheckCircle else Icons.Default.Warning

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (result.isSafe) Color(0xFF1E3E1E) else Color(0xFF3E1E1E)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    safetyIcon,
                    contentDescription = null,
                    tint = safetyColor,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        if (result.isSafe) "Website appears safe" else "Warning: Potential threat detected",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        result.url,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            Divider(color = Color.Gray.copy(alpha = 0.3f))
            Spacer(Modifier.height(16.dp))

            Text(
                "Security Analysis",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))

            SecurityStatRow("Malicious", result.maliciousCount, Color(0xFFFF5252))
            SecurityStatRow("Suspicious", result.suspiciousCount, Color(0xFFFFA726))
            SecurityStatRow("Safe", result.harmlessCount, Color(0xFF4CAF50))
            SecurityStatRow("Undetected", result.undetectedCount, Color.Gray)

            if (result.reputation != 0) {
                Spacer(Modifier.height(12.dp))
                Text(
                    "Reputation Score: ${result.reputation}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
            }

            if (!result.title.isNullOrBlank()) {
                Spacer(Modifier.height(12.dp))
                Text(
                    "Site Title: ${result.title}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
            }

            Spacer(Modifier.height(20.dp))

            if (!result.isSafe) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2E1E1E)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "⚠️ Recommendation",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color(0xFFFF5252),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Do not visit this website or enter any personal information. It has been flagged by ${result.maliciousCount + result.suspiciousCount} security vendors.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            Button(
                onClick = onReset,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))
            ) {
                Text("Check Another Website", color = Color.White)
            }
        }
    }
}

@Composable
fun SecurityStatRow(label: String, count: Int, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(color, RoundedCornerShape(6.dp))
            )
            Spacer(Modifier.width(8.dp))
            Text(
                count.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

