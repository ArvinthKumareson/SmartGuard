@file:OptIn(ExperimentalMaterial3Api::class)

package com.smartguard.app.mainapp.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.smartguard.app.model.WebsiteScanHistory
import com.smartguard.app.viewmodel.HistoryState
import com.smartguard.app.viewmodel.ScanHistoryViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * User screen showing the history of website scans.
 *
 * Reads [HistoryState] from [ScanHistoryViewModel] and shows either a
 * list of past URL scans, an empty view, or an error with retry, plus
 * an option to clear all history.
 */
@Composable
fun ScanHistoryScreen(nav: NavController) {
    val viewModel: ScanHistoryViewModel = viewModel()
    val historyState by viewModel.historyState.collectAsState()
    var showClearDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Website Scan History", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { showClearDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Clear All", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E1E1E))
            )
        },
        bottomBar = { com.smartguard.app.mainapp.resources.SmartGuardBottomBar(nav, "scan_history") }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFF121212))
        ) {
            when (val state = historyState) {
                is HistoryState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color(0xFF6200EE)
                    )
                }

                is HistoryState.Success -> {
                    if (state.scans.isEmpty()) {
                        EmptyHistoryView()
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.scans) { scan ->
                                ScanHistoryCard(scan, onDelete = { viewModel.deleteScan(scan.id) })
                            }
                        }
                    }
                }

                is HistoryState.Error -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFFF5252),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(state.message, color = Color.White, style = MaterialTheme.typography.bodyLarge)
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.loadHistory() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))
                        ) {
                            Text("Retry", color = Color.White)
                        }
                    }
                }
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear All History?", color = Color.White) },
            text = { Text("This will permanently delete all your website scan history.", color = Color.Gray) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllHistory()
                        showClearDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFFF5252))
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showClearDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
                ) {
                    Text("Cancel")
                }
            },
            containerColor = Color(0xFF1E1E1E)
        )
    }
}

/**
 * Card displaying a single website scan result with risk level summary
 * and a delete action.
 */
@Composable
fun ScanHistoryCard(scan: WebsiteScanHistory, onDelete: () -> Unit) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }
    val scanDate = remember(scan.scanDate) { Date(scan.scanDate) }
    
    val actualIsSafe = scan.maliciousCount == 0 && scan.suspiciousCount == 0
    val threatLevel = when {
        scan.maliciousCount >= 5 -> "High Risk"
        scan.maliciousCount > 0 -> "Medium Risk"
        scan.suspiciousCount > 0 -> "Low Risk"
        else -> "Safe"
    }
    val threatColor = when {
        scan.maliciousCount >= 5 -> Color(0xFFD32F2F)
        scan.maliciousCount > 0 -> Color(0xFFFF5252)
        scan.suspiciousCount > 0 -> Color(0xFFFFA726)
        else -> Color(0xFF4CAF50)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (actualIsSafe) Color(0xFF1E3E1E) else Color(0xFF3E1E1E)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (actualIsSafe) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = threatColor,
                    modifier = Modifier.size(40.dp)
                )

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        scan.url,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(Modifier.height(4.dp))
                    
                    Surface(
                        color = threatColor.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            threatLevel,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = threatColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.Gray
                    )
                }
            }
            
            if (!scan.title.isNullOrBlank()) {
                Spacer(Modifier.height(12.dp))
                Text(
                    "\"${scan.title}\"",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.height(12.dp))
            
            Text(
                "Security Vendor Analysis",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DetailChip("🔴 ${scan.maliciousCount}", "Malicious", Color(0xFFFF5252), Modifier.weight(1f))
                DetailChip("🟠 ${scan.suspiciousCount}", "Suspicious", Color(0xFFFFA726), Modifier.weight(1f))
                DetailChip("🟢 ${scan.harmlessCount}", "Safe", Color(0xFF4CAF50), Modifier.weight(1f))
            }
            
            if (scan.reputation != 0) {
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Reputation Score:",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "${scan.reputation}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (scan.reputation < 0) Color(0xFFFF5252) else Color(0xFF4CAF50),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                "Scanned: ${dateFormat.format(scanDate)}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Scan?", color = Color.White) },
            text = { Text("Remove this scan from your history?", color = Color.Gray) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFFF5252))
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
                ) {
                    Text("Cancel")
                }
            },
            containerColor = Color(0xFF1E1E1E)
        )
    }
}

@Composable
fun DetailChip(count: String, label: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.15f)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                count,
                style = MaterialTheme.typography.titleMedium,
                color = color,
                fontWeight = FontWeight.Bold
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = color.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun EmptyHistoryView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.CheckCircle,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(80.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "No Scan History",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Your website scans will appear here",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
    }
}

