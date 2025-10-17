@file:OptIn(ExperimentalMaterial3Api::class)

package com.smartguard.app.mainapp.user

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.smartguard.app.R
import com.smartguard.app.mainapp.common.BackgroundWrapper
import com.smartguard.app.viewmodel.HistoryViewModel
import java.time.Instant
import java.time.format.DateTimeFormatter
import androidx.compose.ui.graphics.Color


@Composable
fun HistoryScreen(nav: NavController, vm: HistoryViewModel = viewModel()) {
    val items by vm.history.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Potential Scam Messages" , color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E1E1E))
            )
        }
    ) { padding ->
        BackgroundWrapper(imageResId = R.drawable.bg_profile) {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
            )  {
                Text(
                    "This list shows alerts captured by SmartGuard (from notification listener or manual checks).",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFFFFFFF)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Button(onClick = { vm.seedDemoData() }) {
                    Text("Seed Demo Alerts")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text("Loaded ${items.size} items", style = MaterialTheme.typography.labelSmall)

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(items) { item ->
                        val source = item.sourceApp.ifBlank { "Unknown App" }
                        val message = item.message.ifBlank { "No message content" }
                        val keywords =
                            item.matchedKeywords.takeIf { it.isNotEmpty() }?.joinToString()
                                ?: "None"
                        val formattedTime = runCatching {
                            DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochMilli(item.timestamp))
                        }.getOrElse {
                            Log.e("HistoryScreen", "Invalid timestamp: ${item.timestamp}")
                            "Invalid time"
                        }

                        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(source, style = MaterialTheme.typography.titleSmall)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(message)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Matched: $keywords")
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Time: $formattedTime",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
