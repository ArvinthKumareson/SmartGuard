@file:OptIn(ExperimentalMaterial3Api::class)

package com.smartguard.app.mainapp.user

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.lazy.LazyColumn
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.smartguard.app.model.FeedbackCategory
import com.smartguard.app.model.UserFeedback
import com.smartguard.app.viewmodel.FeedbackSubmitState
import com.smartguard.app.viewmodel.FeedbackViewModel
import com.smartguard.app.viewmodel.FeedbackListState

@Composable
fun UserFeedbackScreen(nav: NavController) {
    val viewModel: FeedbackViewModel = viewModel()
    val submitState by viewModel.submitState.collectAsState()
    val feedbackListState by viewModel.userFeedbackState.collectAsState()
    
    var selectedTab by remember { mutableStateOf(0) }
    var selectedCategory by remember { mutableStateOf(FeedbackCategory.OTHER) }
    var subject by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf(0) }
    var showCategoryMenu by remember { mutableStateOf(false) }

    val hasNewResponses = remember(feedbackListState) {
        if (feedbackListState is FeedbackListState.Success) {
            (feedbackListState as FeedbackListState.Success).feedback.any { 
                !it.adminResponse.isNullOrBlank() && it.status == "reviewed"
            }
        } else false
    }

    LaunchedEffect(Unit) {
        viewModel.loadUserFeedback()
    }

    LaunchedEffect(submitState) {
        if (submitState is FeedbackSubmitState.Success) {
            kotlinx.coroutines.delay(2000)
            subject = ""
            message = ""
            rating = 0
            selectedCategory = FeedbackCategory.OTHER
            viewModel.resetSubmitState()
            viewModel.loadUserFeedback()
            selectedTab = 1
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Feedback", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    BadgedBox(
                        badge = {
                            if (hasNewResponses && selectedTab == 0) {
                                Badge(containerColor = Color(0xFFFF5252)) {
                                    Text("!", color = Color.White)
                                }
                            }
                        }
                    ) {
                        IconButton(onClick = { 
                            selectedTab = 1
                            viewModel.loadUserFeedback()
                        }) {
                            Icon(Icons.Default.History, contentDescription = "My Feedback", tint = Color.White)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E1E1E))
            )
        },
        bottomBar = { com.smartguard.app.mainapp.resources.SmartGuardBottomBar(nav, "user_feedback") }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFF121212))
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF1E1E1E),
                contentColor = Color.White
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Send Feedback") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { 
                        selectedTab = 1
                        viewModel.loadUserFeedback()
                    },
                    text = { 
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("My Feedback")
                            if (hasNewResponses) {
                                Spacer(Modifier.width(8.dp))
                                Badge(containerColor = Color(0xFFFF5252)) {
                                    Text("New", color = Color.White, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                )
            }

            if (selectedTab == 0) {
                SendFeedbackTab(
                    selectedCategory = selectedCategory,
                    onCategoryChange = { selectedCategory = it },
                    subject = subject,
                    onSubjectChange = { subject = it },
                    message = message,
                    onMessageChange = { message = it },
                    rating = rating,
                    onRatingChange = { rating = it },
                    showCategoryMenu = showCategoryMenu,
                    onCategoryMenuChange = { showCategoryMenu = it },
                    submitState = submitState,
                    onSubmit = { viewModel.submitFeedback(selectedCategory.name, subject, message, rating) }
                )
            } else {
                MyFeedbackTab(feedbackListState, viewModel)
            }
        }
    }
}

@Composable
fun SendFeedbackTab(
    selectedCategory: FeedbackCategory,
    onCategoryChange: (FeedbackCategory) -> Unit,
    subject: String,
    onSubjectChange: (String) -> Unit,
    message: String,
    onMessageChange: (String) -> Unit,
    rating: Int,
    onRatingChange: (Int) -> Unit,
    showCategoryMenu: Boolean,
    onCategoryMenuChange: (Boolean) -> Unit,
    submitState: FeedbackSubmitState,
    onSubmit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "We'd love to hear from you!",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )

        Text(
            "Your feedback helps us improve SmartGuard and better protect you from scams.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Text(
            "Rate your experience",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            modifier = Modifier.padding(top = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(5) { index ->
                Icon(
                    if (index < rating) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = "Star ${index + 1}",
                    tint = if (index < rating) Color(0xFFFFB300) else Color.Gray,
                    modifier = Modifier
                        .size(48.dp)
                        .clickable { onRatingChange(index + 1) }
                        .padding(4.dp)
                )
            }
        }

        Text(
            "Feedback Category",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White
        )

        ExposedDropdownMenuBox(
            expanded = showCategoryMenu,
            onExpandedChange = onCategoryMenuChange
        ) {
            OutlinedTextField(
                value = selectedCategory.displayName,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                trailingIcon = {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.White)
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF6200EE),
                    unfocusedBorderColor = Color.Gray
                )
            )

            ExposedDropdownMenu(
                expanded = showCategoryMenu,
                onDismissRequest = { onCategoryMenuChange(false) },
                modifier = Modifier.background(Color(0xFF1E1E1E))
            ) {
                FeedbackCategory.values().forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category.displayName, color = Color.White) },
                        onClick = {
                            onCategoryChange(category)
                            onCategoryMenuChange(false)
                        }
                    )
                }
            }
        }

        Text(
            "Subject",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White
        )

        OutlinedTextField(
            value = subject,
            onValueChange = onSubjectChange,
            placeholder = { Text("Brief summary of your feedback", color = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFF6200EE),
                unfocusedBorderColor = Color.Gray,
                cursorColor = Color.White
            ),
            singleLine = true
        )

        Text(
            "Message",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White
        )

        OutlinedTextField(
            value = message,
            onValueChange = onMessageChange,
            placeholder = { Text("Tell us more details...", color = Color.Gray) },
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFF6200EE),
                unfocusedBorderColor = Color.Gray,
                cursorColor = Color.White
            ),
            maxLines = 10
        )

        Button(
            onClick = {
                if (subject.isNotBlank() && message.isNotBlank() && rating > 0) {
                    onSubmit()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE)),
            enabled = subject.isNotBlank() && message.isNotBlank() && rating > 0 && submitState !is FeedbackSubmitState.Loading
        ) {
            if (submitState is FeedbackSubmitState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White
                )
            } else {
                Icon(Icons.Default.Send, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Submit Feedback", style = MaterialTheme.typography.titleMedium)
            }
        }

        when (submitState) {
            is FeedbackSubmitState.Success -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E3E1E)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50))
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Thank you! Your feedback has been submitted.", color = Color.White)
                            Text("Check 'My Feedback' tab for admin responses", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
            is FeedbackSubmitState.Error -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF3E1E1E)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFFF5252))
                        Spacer(Modifier.width(12.dp))
                        Text(submitState.message, color = Color.White)
                    }
                }
            }
            else -> {}
        }
    }
}

@Composable
fun MyFeedbackTab(feedbackListState: FeedbackListState, viewModel: FeedbackViewModel) {
    Box(modifier = Modifier.fillMaxSize()) {
        when (feedbackListState) {
            is FeedbackListState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFF6200EE)
                )
            }

            is FeedbackListState.Success -> {
                if (feedbackListState.feedback.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Feedback,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "No Feedback Yet",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Submit your first feedback using the form",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(feedbackListState.feedback.size) { index ->
                            val feedback = feedbackListState.feedback[index]
                            UserFeedbackCard(feedback, viewModel)
                        }
                    }
                }
            }

            is FeedbackListState.Error -> {
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
                    Text(feedbackListState.message, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun UserFeedbackCard(feedback: com.smartguard.app.model.UserFeedback, viewModel: FeedbackViewModel) {
    var expanded by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }
    val feedbackDate = remember(feedback.timestamp) { Date(feedback.timestamp.seconds * 1000) }
    
    val hasAdminResponse = !feedback.adminResponse.isNullOrBlank()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (hasAdminResponse) Color(0xFF1E3E2E) else Color(0xFF1E1E1E)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = when (feedback.status) {
                        "pending" -> Color(0xFFFFA726)
                        "reviewed" -> Color(0xFF4CAF50)
                        else -> Color.Gray
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            when (feedback.status) {
                                "pending" -> Icons.Default.Schedule
                                "reviewed" -> Icons.Default.CheckCircle
                                else -> Icons.Default.Info
                            },
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            feedback.status.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray)
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
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
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

            if (hasAdminResponse) {
                Spacer(Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2E3E2E)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Admin Response",
                                style = MaterialTheme.typography.titleSmall,
                                color = Color(0xFF4CAF50),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            feedback.adminResponse ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                    }
                }
            } else if (feedback.status == "pending") {
                Spacer(Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        tint = Color(0xFFFFA726),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Waiting for admin response...",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                "Submitted: ${dateFormat.format(feedbackDate)}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Feedback?", color = Color.White) },
            text = { Text("This will permanently delete your feedback submission.", color = Color.Gray) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteFeedback(feedback.id)
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


