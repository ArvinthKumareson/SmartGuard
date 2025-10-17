@file:OptIn(ExperimentalMaterial3Api::class)

package com.smartguard.app.mainapp.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun AdminHomeScreen(nav: NavController, onLogout: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Dashboard") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("Welcome, Admin!", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(16.dp))

            AdminFeatureCard("Manage Quiz Questions") {
                nav.navigate("admin_quiz_manager")
            }

            AdminFeatureCard("View Scam Reports") {
                nav.navigate("admin_scam_reports")
            }

            AdminFeatureCard("User Feedback") {
                nav.navigate("admin_feedback")
            }
        }
    }
}

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
