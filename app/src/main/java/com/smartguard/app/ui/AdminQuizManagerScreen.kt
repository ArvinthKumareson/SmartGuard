package com.smartguard.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.smartguard.app.viewmodel.QuizAdminViewModel
import com.smartguard.app.model.QuizQ

@Composable
fun AdminQuizManagerScreen(nav: NavController, vm: QuizAdminViewModel = viewModel()) {
    val questions by vm.questions.collectAsState()
    var newQuestion by remember { mutableStateOf("") }
    var newChoices by remember { mutableStateOf("") }
    var newAnswer by remember { mutableStateOf("0") }
    var newVideoId by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Add New Quiz Question", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        TextField(value = newQuestion, onValueChange = { newQuestion = it }, label = { Text("Question") })
        TextField(value = newChoices, onValueChange = { newChoices = it }, label = { Text("Choices (comma-separated)") })
        TextField(value = newAnswer, onValueChange = { newAnswer = it }, label = { Text("Correct Answer Index") })
        TextField(value = newVideoId, onValueChange = { newVideoId = it }, label = { Text("Video ID (optional)") })

        Button(onClick = {
            val choices = newChoices.split(",").map { it.trim() }
            val answerIndex = newAnswer.toIntOrNull() ?: 0
            vm.addQuestion(QuizQ(newQuestion, choices, answerIndex, newVideoId.ifBlank { null }))
            newQuestion = ""; newChoices = ""; newAnswer = ""; newVideoId = ""
        }) {
            Text("Add Question")
        }

        Spacer(Modifier.height(24.dp))
        Text("Existing Questions", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        LazyColumn {
            items(questions, key = { it.first }) { item: Pair<String, QuizQ> ->
                val (id, q) = item
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(q.question, style = MaterialTheme.typography.bodyLarge)
                        Text("Choices: ${q.choices.joinToString()}")
                        Text("Answer: ${q.choices.getOrNull(q.answer) ?: "Invalid"}")
                        q.videoId?.let { Text("Video ID: $it") }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { vm.deleteQuestion(id) }) {
                                Text("Delete")
                            }

                        }
                    }
                }
            }
        }
    }
}
