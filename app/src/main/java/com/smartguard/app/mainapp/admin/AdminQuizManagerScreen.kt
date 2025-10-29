package com.smartguard.app.mainapp.admin

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Clear
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.smartguard.app.viewmodel.QuizAdminViewModel
import com.smartguard.app.model.QuizQ
import com.smartguard.app.util.VideoUploader
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun AdminQuizManagerScreen(nav: NavController, vm: QuizAdminViewModel = viewModel()) {
    val context = LocalContext.current
    val questions by vm.questions.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var newQuestion by remember { mutableStateOf("") }
    val newChoices = remember { mutableStateListOf("", "", "") }
    var newAnswer by remember { mutableStateOf("0") }
    var newReason by remember { mutableStateOf("") }
    var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }
    var selectedVideoName by remember { mutableStateOf("") }
    var isUploading by remember { mutableStateOf(false) }
    var uploadError by remember { mutableStateOf<String?>(null) }
    var editingQuestionId by remember { mutableStateOf<String?>(null) }
    var existingVideoUri by remember { mutableStateOf<String?>(null) } // Store existing video URI when editing
    
    // Delete confirmation dialog state
    var showDeleteDialog by remember { mutableStateOf(false) }
    var questionToDelete by remember { mutableStateOf<Pair<String, QuizQ>?>(null) }
    
    // Search state
    var searchQuery by remember { mutableStateOf("") }
    var filteredQuestions by remember { mutableStateOf(questions) }
    
    // Debounced search with coroutine
    LaunchedEffect(searchQuery, questions) {
        // Add debounce delay for typing
        if (searchQuery.isNotBlank()) {
            delay(300) // Wait 300ms after user stops typing
        }
        
        filteredQuestions = if (searchQuery.isBlank()) {
            questions
        } else {
            withContext(Dispatchers.Default) {
                questions.filter { (_, q) ->
                    q.question.contains(searchQuery, ignoreCase = true) ||
                    q.choices.any { it.contains(searchQuery, ignoreCase = true) } ||
                    q.reason?.contains(searchQuery, ignoreCase = true) == true
                }
            }
        }
    }

    // Video picker launcher
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                // Try to take persistent permission (optional - not all content providers support this)
                context.contentResolver.takePersistableUriPermission(
                    it,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: SecurityException) {
                // Ignore - some content providers don't support persistent permissions
                android.util.Log.d("AdminQuiz", "Persistent permission not available: ${e.message}")
            }
            
            selectedVideoUri = it
            
            // Get the file name from URI
            val cursor = context.contentResolver.query(it, null, null, null, null)
            cursor?.use { c ->
                if (c.moveToFirst()) {
                    val nameIndex = c.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0) {
                        selectedVideoName = c.getString(nameIndex)
                    }
                }
            }
        }
    }

    val scrollState = rememberScrollState()
    
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.inverseSurface,
                    contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                    actionColor = MaterialTheme.colorScheme.inversePrimary
                )
            }
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(paddingValues)
                .padding(16.dp)
        ) {
        Text(
            if (editingQuestionId == null) "Add New Quiz Question" else "Edit Quiz Question",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(Modifier.height(16.dp))

        // Question input
        OutlinedTextField(
            value = newQuestion,
            onValueChange = { newQuestion = it },
            label = { Text("Question") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(Modifier.height(12.dp))
        
        // Choices input
        Text("Answer Choices", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        
        newChoices.forEachIndexed { index, choice ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = choice,
                    onValueChange = { newValue ->
                        newChoices[index] = newValue
                    },
                    label = { Text("Choice ${index + 1}") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                
                if (newChoices.size > 2) {
                    IconButton(
                        onClick = {
                            newChoices.removeAt(index)
                            // Adjust selected answer if needed
                            val currentAnswerIndex = newAnswer.toIntOrNull() ?: 0
                            if (currentAnswerIndex >= newChoices.size) {
                                newAnswer = (newChoices.size - 1).coerceAtLeast(0).toString()
                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.Remove,
                            contentDescription = "Remove choice",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                } else {
                    Spacer(Modifier.width(48.dp))
                }
            }
            Spacer(Modifier.height(8.dp))
        }
        
        // Add choice button
        OutlinedButton(
            onClick = {
                newChoices.add("")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(4.dp))
            Text("Add Another Choice")
        }
        
        Spacer(Modifier.height(8.dp))
        
        // Answer selection
        Text("Correct Answer", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        
        newChoices.forEachIndexed { index, choice ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = newAnswer == index.toString(),
                    onClick = { newAnswer = index.toString() }
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (choice.isBlank()) "Choice ${index + 1} (empty)" else choice,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (choice.isBlank()) 
                        MaterialTheme.colorScheme.onSurfaceVariant 
                    else 
                        MaterialTheme.colorScheme.onSurface
                )
            }
        }
        
        Spacer(Modifier.height(8.dp))
        
        // Reason input
        OutlinedTextField(
            value = newReason,
            onValueChange = { newReason = it },
            label = { Text("Reason for Correct Answer (Optional)") },
            placeholder = { Text("Explain why this answer is correct") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 5
        )
        
        Spacer(Modifier.height(16.dp))
        
        // Video selection section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Video (Optional)",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(8.dp))
                
                if (selectedVideoUri != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Selected video:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                selectedVideoName.ifEmpty { "Video selected" },
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        IconButton(onClick = {
                            selectedVideoUri = null
                            selectedVideoName = ""
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove video")
                        }
                    }
                } else {
                    Button(
                        onClick = { videoPickerLauncher.launch("video/*") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.VideoLibrary, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Select Video from Device")
                    }
                }
            }
        }
        
        Spacer(Modifier.height(16.dp))

        // Upload error message
        if (uploadError != null) {
            Text(
                text = uploadError!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(8.dp))
        }

        // Add question button
        Button(
            onClick = {
                scope.launch {
                    isUploading = true
                    uploadError = null
                    
                    try {
                        // Handle video upload
                        val videoUrl = if (selectedVideoUri != null) {
                            // Upload new video to Firebase Storage
                            VideoUploader.uploadVideo(context, selectedVideoUri!!)
                        } else if (editingQuestionId != null && existingVideoUri != null) {
                            // Keep existing video when editing and no new video selected
                            existingVideoUri
                        } else {
                            null
                        }
                        
                        // Add question with cloud video URL
                        val choices = newChoices.filter { it.isNotBlank() }
                        if (choices.isEmpty()) {
                            uploadError = "Please add at least one choice"
                            return@launch
                        }
                        
                        val answerIndex = newAnswer.toIntOrNull() ?: 0
                        if (answerIndex >= choices.size) {
                            uploadError = "Invalid answer selection"
                            return@launch
                        }
                        
                        val quizQ = QuizQ(
                            question = newQuestion,
                            choices = choices,
                            answer = answerIndex,
                            videoId = null,
                            videoUri = videoUrl,  // Cloud URL instead of local URI
                            reason = newReason.takeIf { it.isNotBlank() }
                        )
                        
                        // Add or update question
                        val isEditing = editingQuestionId != null
                        if (isEditing) {
                            vm.updateQuestion(editingQuestionId!!, quizQ)
                        } else {
                            vm.addQuestion(quizQ)
                        }
                        
                        // Show success message
                        snackbarHostState.showSnackbar(
                            message = if (isEditing) "✓ Question updated successfully!" else "✓ Question added successfully!",
                            duration = SnackbarDuration.Short
                        )
                        
                        // Reset form
                        newQuestion = ""
                        newChoices.clear()
                        newChoices.addAll(listOf("", "", ""))
                        newAnswer = "0"
                        newReason = ""
                        selectedVideoUri = null
                        selectedVideoName = ""
                        editingQuestionId = null
                        existingVideoUri = null
                    } catch (e: Exception) {
                        uploadError = "Error: ${e.message}"
                        snackbarHostState.showSnackbar(
                            message = "✗ Error: ${e.message}",
                            duration = SnackbarDuration.Long
                        )
                    } finally {
                        isUploading = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = newQuestion.isNotBlank() && newChoices.any { it.isNotBlank() } && !isUploading
        ) {
            if (isUploading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(Modifier.width(8.dp))
                Text("Uploading...")
            } else {
                Text(if (editingQuestionId == null) "Add Question" else "Update Question")
            }
        }
        
        // Cancel edit button
        if (editingQuestionId != null) {
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = {
                    // Reset form
                    newQuestion = ""
                    newChoices.clear()
                    newChoices.addAll(listOf("", "", ""))
                    newAnswer = "0"
                    newReason = ""
                    selectedVideoUri = null
                    selectedVideoName = ""
                    editingQuestionId = null
                    existingVideoUri = null
                    uploadError = null
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancel Edit")
            }
        }

        Spacer(Modifier.height(24.dp))
        
        // Existing questions list
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Text("Existing Questions", style = MaterialTheme.typography.titleLarge)
            Text(
                "${questions.size} total",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(12.dp))
        
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search questions, choices, or reasons...") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search")
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear search")
                    }
                }
            },
            singleLine = true
        )
        
        Spacer(Modifier.height(12.dp))
        
        // Show filtered count
        if (searchQuery.isNotEmpty()) {
            Text(
                "Found ${filteredQuestions.size} question(s)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filteredQuestions.forEach { item: Pair<String, QuizQ> ->
                val (id, q) = item
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            q.question,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(8.dp))
                        
                        Text(
                            "Choices:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        q.choices.forEachIndexed { index, choice ->
                            Text(
                                "${index}. $choice",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (index == q.answer) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.onSurface
                            )
                        }
                        
                        Spacer(Modifier.height(8.dp))
                        
                        Text(
                            "Correct Answer: ${q.choices.getOrNull(q.answer) ?: "Invalid"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        // Show reason if available
                        if (!q.reason.isNullOrBlank()) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Reason:",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                q.reason,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        // Show video info if available
                        if (q.videoUri != null) {
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.VideoLibrary,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    "Has video",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                        
                        Spacer(Modifier.height(12.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    // Populate form with question data for editing
                                    newQuestion = q.question
                                    newChoices.clear()
                                    newChoices.addAll(q.choices)
                                    newAnswer = q.answer.toString()
                                    newReason = q.reason ?: ""
                                    
                                    // Handle video
                                    existingVideoUri = q.videoUri
                                    if (q.videoUri != null) {
                                        selectedVideoName = "Existing video"
                                    }
                                    
                                    editingQuestionId = id
                                    
                                    // Scroll to top
                                    scope.launch {
                                        scrollState.animateScrollTo(0)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null)
                                Spacer(Modifier.width(4.dp))
                                Text("Edit")
                            }
                            
                            Button(
                                onClick = {
                                    questionToDelete = item
                                    showDeleteDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                                )
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null)
                                Spacer(Modifier.width(4.dp))
                                Text("Delete")
                            }
                        }
                    }
                }
            }
        }
        
            // Add bottom padding for better scrolling experience
            Spacer(Modifier.height(16.dp))
        }
    }
    
    // Delete confirmation dialog
    if (showDeleteDialog && questionToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    "Delete Question?",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Column {
                    Text(
                        "Are you sure you want to delete this question?",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        questionToDelete!!.second.question,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            vm.deleteQuestion(questionToDelete!!.first)
                            showDeleteDialog = false
                            snackbarHostState.showSnackbar(
                                message = "✓ Question deleted successfully!",
                                duration = SnackbarDuration.Short
                            )
                            questionToDelete = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
