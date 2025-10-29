@file:OptIn(ExperimentalMaterial3Api::class)

package com.smartguard.app.mainapp.user

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.smartguard.app.R
import com.smartguard.app.mainapp.common.BackgroundWrapper
import com.smartguard.app.viewmodel.HistoryViewModel
import java.time.Instant
import java.time.format.DateTimeFormatter

fun getAppDisplayName(packageName: String): String {
    return when (packageName) {
        "com.whatsapp" -> "WhatsApp"
        "org.telegram.messenger" -> "Telegram"
        "com.android.mms" -> "Messages"
        "android.provider.Telephony.SMS_RECEIVED" -> "SMS"
        else -> packageName.substringAfterLast('.').replaceFirstChar { it.uppercase() }
    }
}

@Composable
fun HistoryScreen(nav: NavController, vm: HistoryViewModel = viewModel()) {
    val items by vm.fullHistory.collectAsState(initial = emptyList()) // Use fullHistory to include cloud records
    
    // Auto-refresh when screen becomes visible
    LaunchedEffect(Unit) {
        vm.refreshHistory()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Potential Scam Messages", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E1E1E)),
                actions = {
                    IconButton(onClick = { vm.refreshHistory() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color.White)
                    }
                }
            )
        },
        bottomBar = { com.smartguard.app.mainapp.resources.SmartGuardBottomBar(nav, "history") }
    ) { padding ->
        BackgroundWrapper(imageResId = R.drawable.bg_profile) {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
            ) {
                Text(
                    "This list shows alerts captured by SmartGuard (from notification listener or manual checks).",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))

                Button(onClick = { vm.refreshHistory() }) {
                    Text("Refresh")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(onClick = { vm.seedDemoData() }) {
                    Text("Seed Demo Alerts")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text("Loaded ${items.size} items", style = MaterialTheme.typography.labelSmall)

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(items) { item ->
                        val source = getAppDisplayName(item.sourceApp.ifBlank { "Unknown" })
                        val message = item.message.ifBlank { "No message content" }
                        val keywords = item.matchedKeywords.takeIf { it.isNotEmpty() }?.joinToString() ?: "None"
                        val formattedTime = runCatching {
                            DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochMilli(item.timestamp))
                        }.getOrElse {
                            Log.e("HistoryScreen", "Invalid timestamp: ${item.timestamp}")
                            "Invalid time"
                        }

                        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                // App source
                                Text(source, style = MaterialTheme.typography.titleSmall, color = Color.Black, fontWeight = FontWeight.Bold)
                                
                                // Sender information - more prominent display
                                if (!item.senderName.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    androidx.compose.material.icons.Icons.Default.Person,
                                                    contentDescription = null,
                                                    tint = Color(0xFF1976D2),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "Sender: ${item.senderName}",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = Color(0xFF1976D2),
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            
                                            if (!item.conversationTitle.isNullOrBlank() && item.conversationTitle != item.senderName) {
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        androidx.compose.material.icons.Icons.Default.Group,
                                                        contentDescription = null,
                                                        tint = Color(0xFF388E3C),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = "Group: ${item.conversationTitle}",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = Color(0xFF388E3C),
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                }
                                            }
                                            
                                            // Extract phone number if present in sender name
                                            val phoneRegex = Regex("\\+?[0-9\\s\\-\\(\\)]{7,}")
                                            val phoneMatch = phoneRegex.find(item.senderName)
                                            if (phoneMatch != null) {
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        androidx.compose.material.icons.Icons.Default.Phone,
                                                        contentDescription = null,
                                                        tint = Color(0xFFFF9800),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = "Phone: ${phoneMatch.value}",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = Color(0xFFFF9800),
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(message, color = Color.Black)
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                // Display matched keywords with explanations
                                if (item.matchedKeywords.isNotEmpty()) {
                                    Text("Scam Indicators:", style = MaterialTheme.typography.titleSmall, color = Color(0xFFFF5252), fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    item.matchedKeywords.forEach { keyword ->
                                        val explanation = item.keywordExplanations[keyword] ?: "This keyword is commonly used in scam messages"
                                        Card(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFF3E1E1E))
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        Icons.Default.Warning,
                                                        contentDescription = null,
                                                        tint = Color(0xFFFF5252),
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        keyword,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    explanation,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color(0xFFDDDDDD)
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    Text("No scam indicators detected", color = Color(0xFF4CAF50))
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Time: $formattedTime", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}
