@file:OptIn(ExperimentalMaterial3Api::class)

package com.smartguard.app.mainapp.user

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.ContentScale
import androidx.navigation.NavController
import com.smartguard.app.R
import com.smartguard.app.mainapp.resources.GradientButton
import com.smartguard.app.mainapp.resources.SmartGuardBottomBar
import com.smartguard.app.utils.PermissionUtils

@Composable
fun PermissionsSettingsScreen(nav: NavController) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var permStatus by remember { mutableStateOf(PermissionUtils.getNotificationPermissionStatus(context)) }
    var refreshCounter by remember { mutableStateOf(0) }

    // Refresh permission status when screen becomes visible
    LaunchedEffect(Unit) {
        permStatus = PermissionUtils.getNotificationPermissionStatus(context)
    }

    // Refresh when refreshCounter changes
    LaunchedEffect(refreshCounter) {
        permStatus = PermissionUtils.getNotificationPermissionStatus(context)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.bg_profile),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)))

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Permissions", color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = { nav.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = { refreshCounter++ }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            bottomBar = { SmartGuardBottomBar(nav, currentRoute = "profile") },
            containerColor = Color.Transparent
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(24.dp)
            ) {
                // Notification Access Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0x33000000)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        val isEnabled = permStatus == "Enabled"
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isEnabled) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                                contentDescription = null,
                                tint = if (isEnabled) Color(0xFF4CAF50) else Color(0xFFFF9800),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "WhatsApp Message Scanning",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Status: $permStatus",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isEnabled) Color(0xFF4CAF50) else Color(0xFFFF9800)
                                )
                            }
                        }
                        
                        Spacer(Modifier.height(16.dp))
                        
                        Text(
                            text = if (isEnabled) {
                                "SmartGuard can monitor WhatsApp notifications for potential scam messages."
                            } else {
                                "Enable notification access to allow SmartGuard to scan WhatsApp messages for potential scams in real-time."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                        
                        Spacer(Modifier.height(16.dp))
                        
                        GradientButton(
                            text = "Open Settings",
                            onClick = { PermissionUtils.openNotificationSettings(context) }
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                // How it works section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0x33000000)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "How It Works",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(Modifier.height(12.dp))
                        
                        val steps = listOf(
                            "1. Enable notification access in Android settings",
                            "2. SmartGuard monitors incoming WhatsApp messages",
                            "3. Messages are scanned locally for scam keywords",
                            "4. Alerts are shown if potential scams are detected"
                        )
                        
                        steps.forEach { step ->
                            Row(verticalAlignment = Alignment.Top) {
                                Text(
                                    text = step,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

            }
        }
    }
}
