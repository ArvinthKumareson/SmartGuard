package com.smartguard.app.mainapp.user

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.smartguard.app.R
import com.smartguard.app.mainapp.common.BackgroundWrapper
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.smartguard.app.model.QuizResult
import com.smartguard.app.mainapp.resources.GradientButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizOverviewScreen(nav: NavController, results: List<QuizResult>, onBack: () -> Unit) {
    BackgroundWrapper(imageResId = R.drawable.bg_profile) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Quiz Summary", color = Color.White) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E1E1E))
                )
            },
            bottomBar = { com.smartguard.app.mainapp.resources.SmartGuardBottomBar(nav, "quiz") }
        ) { padding ->
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp)
            ) {
                results.forEachIndexed { i, result ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Question ${i + 1}",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(result.question, color = Color.White)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Your Answer: ${result.selectedAnswer ?: "No answer"}",
                                color = if (result.isCorrect) Color(0xFF6EDE5B) else Color(0xFFDE5B5B)
                            )
                            Text("Correct Answer: ${result.correctAnswer}", color = Color.LightGray)
                            
                            // Display reason if available
                            if (!result.reason.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    "Why?",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Color(0xFF6EDE5B)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    result.reason,
                                    color = Color(0xFFE0E0E0),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                GradientButton("Back to Quiz", onClick = onBack)
            }
        }
    }
}
