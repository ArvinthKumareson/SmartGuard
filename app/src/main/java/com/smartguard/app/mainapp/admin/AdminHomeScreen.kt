@file:OptIn(ExperimentalMaterial3Api::class)

package com.smartguard.app.mainapp.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.smartguard.app.R
import com.smartguard.app.mainapp.common.BackgroundWrapper

/**
 * Main landing screen for admin users.
 *
 * Displays a simple dashboard of admin features such as quiz management,
 * scam reports, keyword management, user feedback and course management.
 */
@Composable
fun AdminHomeScreen(nav: NavController, onLogout: () -> Unit) {
    BackgroundWrapper(imageResId = R.drawable.bg_profile) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Admin Dashboard", color = Color.White) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E1E1E)),
                    actions = {
                        IconButton(onClick = onLogout) {
                            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout", tint = Color.White)
                        }
                    }
                )
            },
            containerColor = Color(0x00000000), // Transparent
            contentColor = Color.White
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
            ) {
                Text("Welcome, Admin!", style = MaterialTheme.typography.headlineSmall, color = Color.White)
                Spacer(Modifier.height(16.dp))

                AdminFeatureCard("Manage Quiz Questions") {
                    nav.navigate("admin_quiz_manager")
                }

                AdminFeatureCard("View Scam Reports") {
                    nav.navigate("admin_scam_reports")
                }

                AdminFeatureCard("Manage Scam Keywords") {
                    nav.navigate("admin_keywords")
                }

                AdminFeatureCard("User Feedback") {
                    nav.navigate("admin_feedback")
                }

                AdminFeatureCard("Manage Courses") {
                    nav.navigate("admin_courses")
                }

            }
        }
    }
}

/**
 * Simple card component used by [AdminHomeScreen] to represent a single
 * admin feature entry point.
 */
@Composable
fun AdminFeatureCard(title: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
        }
    }
}

