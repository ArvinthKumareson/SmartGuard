package com.smartguard.app.mainapp.admin

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.smartguard.app.viewmodel.QuizAdminViewModel
import com.smartguard.app.model.QuizQ

@Composable
fun AdminQuizManagerScreen(nav: NavController, vm: QuizAdminViewModel = viewModel()) {
    val context = LocalContext.current
    val questions by vm.questions.collectAsState()
    
    var newQuestion by remember { mutableStateOf("") }
    var newChoices by remember { mutableStateOf("") }
    var newAnswer by remember { mutableStateOf("0") }
    var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }
    var selectedVideoName by remember { mutableStateOf("") }

    // Video picker launcher
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // Take persistent permission to access the file
            context.contentResolver.takePersistableUriPermission(
                it,
                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Add New Quiz Question", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))

        // Question input
        OutlinedTextField(
            value = newQuestion,
            onValueChange = { newQuestion = it },
            label = { Text("Question") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(Modifier.height(8.dp))
        
        // Choices input
        OutlinedTextField(
            value = newChoices,
            onValueChange = { newChoices = it },
            label = { Text("Choices (comma-separated)") },
            placeholder = { Text("e.g., Option A, Option B, Option C") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(Modifier.height(8.dp))
        
        // Answer index input
        OutlinedTextField(
            value = newAnswer,
            onValueChange = { newAnswer = it },
            label = { Text("Correct Answer Index (0-based)") },
            placeholder = { Text("e.g., 0 for first choice, 1 for second, etc.") },
            modifier = Modifier.fillMaxWidth()
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
                    // Show selected video
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
                    // Show picker button
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

        // Add question button
        Button(
            onClick = {
                val choices = newChoices.split(",").map { it.trim() }
                val answerIndex = newAnswer.toIntOrNull() ?: 0
                vm.addQuestion(
                    QuizQ(
                        question = newQuestion,
                        choices = choices,
                        answer = answerIndex,
                        videoId = null,  // No longer using YouTube video IDs
                        videoUri = selectedVideoUri?.toString()
                    )
                )
                // Reset form
                newQuestion = ""
                newChoices = ""
                newAnswer = "0"
                selectedVideoUri = null
                selectedVideoName = ""
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = newQuestion.isNotBlank() && newChoices.isNotBlank()
        ) {
            Text("Add Question")
        }

        Spacer(Modifier.height(24.dp))
        
        // Existing questions list
        Text("Existing Questions", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(questions, key = { it.first }) { item: Pair<String, QuizQ> ->
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
                                onClick = { vm.deleteQuestion(id) },
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
    }
}
