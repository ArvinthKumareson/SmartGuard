package com.smartguard.app.mainapp.user

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.smartguard.app.R
import com.smartguard.app.mainapp.common.BackgroundWrapper
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.smartguard.app.model.QuizResult
import com.smartguard.app.mainapp.GradientButton

@Composable
fun QuizOverviewScreen(results: List<QuizResult>, onBack: () -> Unit) {
    BackgroundWrapper(imageResId = R.drawable.bg_profile) {
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {

            Text(
                "Quiz Summary",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(16.dp))

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
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            GradientButton("Back to Quiz", onClick = onBack)
        }
    }
}
