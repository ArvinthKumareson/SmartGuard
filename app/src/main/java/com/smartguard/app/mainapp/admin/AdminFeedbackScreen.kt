@file:OptIn(ExperimentalMaterial3Api::class)

package com.smartguard.app.mainapp.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.smartguard.app.model.UserFeedback
import com.smartguard.app.viewmodel.FeedbackListState
import com.smartguard.app.viewmodel.FeedbackViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AdminFeedbackScreen(nav: NavController) {
    val viewModel: FeedbackViewModel = viewModel()
    val feedbackState by viewModel.allFeedbackState.collectAsState()
    
    var selectedFilter by remember { mutableStateOf("All") }

    LaunchedEffect(Unit) {
        viewModel.loadAllFeedback()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("User Feedback", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadAllFeedback() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E1E1E))
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFF121212))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == "All",
                    onClick = { selectedFilter = "All" },
                    label = { Text("All") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF6200EE),
                        selectedLabelColor = Color.White
                    )
                )
                FilterChip(
                    selected = selectedFilter == "Pending",
                    onClick = { selectedFilter = "Pending" },
                    label = { Text("Pending") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF6200EE),
                        selectedLabelColor = Color.White
                    )
                )
                FilterChip(
                    selected = selectedFilter == "Reviewed",
                    onClick = { selectedFilter = "Reviewed" },
                    label = { Text("Reviewed") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF6200EE),
                        selectedLabelColor = Color.White
                    )
                )
            }

            when (val state = feedbackState) {
                is FeedbackListState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF6200EE))
                    }
                }

                is FeedbackListState.Success -> {
                    val filteredFeedback = when (selectedFilter) {
                        "Pending" -> state.feedback.filter { it.status == "pending" }
                        "Reviewed" -> state.feedback.filter { it.status == "reviewed" }
                        else -> state.feedback
                    }

                    if (filteredFeedback.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.Feedback,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(80.dp)
                                )
                                Spacer(Modifier.height(16.dp))
                                Text("No feedback found", color = Color.White, style = MaterialTheme.typography.titleLarge)
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredFeedback) { feedback ->
                                AdminFeedbackCard(feedback, viewModel)
                            }
                        }
                    }
                }

                is FeedbackListState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFFF5252), modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(16.dp))
                            Text(state.message, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminFeedbackCard(feedback: UserFeedback, viewModel: FeedbackViewModel) {
    var showResponseDialog by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }
    val feedbackDate = remember(feedback.timestamp) { Date(feedback.timestamp.seconds * 1000) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (feedback.status == "pending") Color(0xFF3E2E1E) else Color(0xFF1E1E1E)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF6200EE), modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            feedback.userName,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        feedback.userEmail,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                Surface(
                    color = if (feedback.status == "pending") Color(0xFFFFA726) else Color(0xFF4CAF50),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        feedback.status.uppercase(),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color(0xFF2E2E3E),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        feedback.category,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF6200EE)
                    )
                }

                Row {
                    repeat(5) { index ->
                        Icon(
                            if (index < feedback.rating) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = null,
                            tint = if (index < feedback.rating) Color(0xFFFFB300) else Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                feedback.subject,
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(8.dp))

            Text(
                feedback.message,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                maxLines = if (expanded) Int.MAX_VALUE else 3
            )

            if (feedback.message.length > 100) {
                TextButton(onClick = { expanded = !expanded }) {
                    Text(
                        if (expanded) "Show less" else "Show more",
                        color = Color(0xFF6200EE)
                    )
                }
            }

            if (!feedback.adminResponse.isNullOrBlank()) {
                Spacer(Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2E2E3E)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = Color(0xFF6200EE), modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Admin Response", style = MaterialTheme.typography.labelMedium, color = Color(0xFF6200EE), fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(feedback.adminResponse, style = MaterialTheme.typography.bodySmall, color = Color.White)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                "Submitted: ${dateFormat.format(feedbackDate)}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { showResponseDialog = true },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF6200EE))
                ) {
                    Icon(Icons.Default.Reply, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Respond")
                }

                if (feedback.status == "pending") {
                    Button(
                        onClick = { viewModel.updateStatus(feedback.id, "reviewed") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Mark Reviewed")
                    }
                }
            }
        }
    }

    if (showResponseDialog) {
        var responseText by remember { mutableStateOf(feedback.adminResponse ?: "") }
        
        AlertDialog(
            onDismissRequest = { showResponseDialog = false },
            title = { Text("Admin Response", color = Color.White) },
            text = {
                OutlinedTextField(
                    value = responseText,
                    onValueChange = { responseText = it },
                    placeholder = { Text("Write your response...", color = Color.Gray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF6200EE),
                        unfocusedBorderColor = Color.Gray
                    ),
                    maxLines = 5
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateStatus(feedback.id, "reviewed", responseText)
                        showResponseDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))
                ) {
                    Text("Send Response")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResponseDialog = false }) {
                    Text("Cancel", color = Color.White)
                }
            },
            containerColor = Color(0xFF1E1E1E)
        )
    }
}

