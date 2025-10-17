@file:OptIn(ExperimentalMaterial3Api::class)

package com.smartguard.app.mainapp.quiz

import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.gson.Gson
import com.smartguard.app.R
import com.smartguard.app.model.QuizResult
import com.smartguard.app.mainapp.user.BottomNavigationBar
import com.smartguard.app.mainapp.GradientButton
import com.smartguard.app.mainapp.common.BackgroundWrapper
import com.smartguard.app.viewmodel.QuizUserViewModel

@Composable
fun YouTubePlayer(videoId: String) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                webViewClient = WebViewClient()
                webChromeClient = WebChromeClient()
                loadUrl("https://www.youtube.com/$videoId")
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    )
}

@Composable
fun QuizScreen(nav: NavController, vm: QuizUserViewModel = viewModel()) {
    val quiz by vm.quiz.collectAsState()
    val isLoading by vm.isLoading.collectAsState()
    var index by remember { mutableStateOf(0) }
    val selected = remember(quiz) {
        mutableStateListOf<Int?>().apply { repeat(quiz.size) { add(null) } }
    }

    BackgroundWrapper(imageResId = R.drawable.bg_profile) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Scam Quiz", color = Color.White) },
                    navigationIcon = {
                        Icon(
                            Icons.Default.Assignment,
                            contentDescription = "Quiz Icon",
                            tint = Color.White,
                            modifier = Modifier.padding(8.dp)
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E1E1E))
                )
            },
            bottomBar = { BottomNavigationBar(nav) }
        ) { padding ->
            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }
                quiz.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No quiz available", color = Color.White)
                    }
                }
                else -> {
                    val q = quiz[index]

                    Column(
                        modifier = Modifier
                            .padding(padding)
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Text("Question ${index + 1} of ${quiz.size}", style = MaterialTheme.typography.titleMedium, color = Color.White)
                        Spacer(Modifier.height(8.dp))
                        Text(q.question, style = MaterialTheme.typography.bodyLarge, color = Color.White)
                        Spacer(Modifier.height(16.dp))

                        q.videoId?.let {
                            YouTubePlayer(it)
                            Spacer(Modifier.height(16.dp))
                        }

                        q.choices.forEachIndexed { i, c ->
                            val checked = selected[index] == i
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                                onClick = { selected[index] = i }
                            ) {
                                Row(
                                    Modifier
                                        .padding(16.dp)
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = checked,
                                        onClick = { selected[index] = i },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = Color.White,
                                            unselectedColor = Color.Gray
                                        )
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(c, color = Color.White)
                                }
                            }
                        }

                        Spacer(Modifier.height(24.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            if (index > 0) {
                                GradientButton("Back", onClick = { index-- }, modifier = Modifier.weight(1f))
                            }
                            if (index < quiz.lastIndex) {
                                GradientButton("Next", onClick = { index++ }, modifier = Modifier.weight(1f))
                            }
                            if (index == quiz.lastIndex) {
                                GradientButton("Submit", onClick = {
                                    val results = quiz.mapIndexed { i, q ->
                                        QuizResult(
                                            question = q.question,
                                            selectedAnswer = selected[i]?.let { q.choices[it] },
                                            correctAnswer = q.choices[q.answer],
                                            isCorrect = selected[i] == q.answer
                                        )
                                    }
                                    val json = Gson().toJson(results)
                                    nav.navigate("quizOverview?resultsJson=${java.net.URLEncoder.encode(json, "UTF-8")}")
                                }, modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}
