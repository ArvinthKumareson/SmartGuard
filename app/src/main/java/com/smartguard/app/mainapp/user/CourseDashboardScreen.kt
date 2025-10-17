@file:OptIn(ExperimentalMaterial3Api::class)

package com.smartguard.app.mainapp.user

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.smartguard.app.mainapp.resources.CourseCard
import com.smartguard.app.mainapp.resources.SmartGuardBottomBar
import com.smartguard.app.model.ScamCourse

@Composable
fun CourseDashboardScreen(nav: NavController) {
    val courses = remember {
        listOf(
            ScamCourse("Spot Fake Delivery SMS", "Learn to identify spoofed courier messages.", "Beginner", 4.9f, true),
            ScamCourse("OTP Scams Explained", "Understand how scammers trick users into sharing OTPs.", "Intermediate", 4.8f, false),
            ScamCourse("Phishing via Email", "Detect spoofed sender names and fake domains.", "Advanced", 4.7f, false)
        )
    }

    var selectedCourse by remember { mutableStateOf<ScamCourse?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SmartGuard Training", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E1E1E))
            )
        },
        bottomBar = { SmartGuardBottomBar(nav, currentRoute = "home") }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Courses", style = MaterialTheme.typography.titleLarge, color = Color.White)
            Spacer(Modifier.height(8.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(courses) { course ->
                    CourseCard(course) { selectedCourse = course }
                }
            }

            Spacer(Modifier.height(24.dp))
            Text("Leaderboard", style = MaterialTheme.typography.titleMedium, color = Color.White)
            // Add leaderboard stats here...

            Spacer(Modifier.height(24.dp))
            Text("Tips", style = MaterialTheme.typography.titleMedium, color = Color.White)
            // Add tips preview or link to TipsScreen
        }
    }

    selectedCourse?.let { course ->
        AlertDialog(
            onDismissRequest = { selectedCourse = null },
            confirmButton = {
                TextButton(onClick = {
                    selectedCourse = null
                    nav.navigate("courseDetail/${course.title}")
                }) {
                    Text("Start", color = Color.White)
                }
            },
            title = { Text(course.title, color = Color.White) },
            text = {
                Column {
                    Text(course.description, color = Color.LightGray)
                    Spacer(Modifier.height(8.dp))
                    Text("Difficulty: ${course.level}", color = Color.Gray)
                    Text("Rating: ${course.rating}", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF1E1E1E)
        )
    }
}
