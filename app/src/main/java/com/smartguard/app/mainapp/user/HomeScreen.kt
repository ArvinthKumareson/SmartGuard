@file:OptIn(ExperimentalMaterial3Api::class)

package com.smartguard.app.mainapp.user

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.smartguard.app.viewmodel.AuthViewModel
import com.smartguard.app.mainapp.common.BackgroundWrapper
import com.smartguard.app.utils.PermissionUtils
import com.smartguard.app.R

data class FeatureItem(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val color: Color,
    val route: String
)

@Composable
fun HomeScreen(nav: NavController, vm: AuthViewModel) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var isNotificationEnabled by remember { mutableStateOf(false) }
    var refreshCounter by remember { mutableStateOf(0) }
    var searchText by remember { mutableStateOf("") }
    
    // Check notification permission status
    LaunchedEffect(refreshCounter) {
        val enabled = PermissionUtils.isNotificationListenerEnabled(context)
        Log.d("HomeScreen", "Permission check result: $enabled (refresh: $refreshCounter)")
        isNotificationEnabled = enabled
    }
    
    // Refresh when screen becomes visible
    LaunchedEffect(Unit) {
        val enabled = PermissionUtils.isNotificationListenerEnabled(context)
        Log.d("HomeScreen", "Initial permission check: $enabled")
        isNotificationEnabled = enabled
    }
    
    // Force refresh every time the screen is focused
    LaunchedEffect(nav) {
        refreshCounter++
    }
    
    // Organized by categories
    val learnFeatures = remember {
        listOf(
            FeatureItem("Advisory Tips", "Learn how to identify and avoid scams", Icons.Default.Lightbulb, Color(0xFF4CAF50), "tips"),
            FeatureItem("Scam Game", "Practice spotting scams through scenarios", Icons.Default.Games, Color(0xFFFF9800), "scam chat"),
            FeatureItem("Quiz", "Test your scam prevention knowledge", Icons.Default.Quiz, Color(0xFF9C27B0), "quiz")
        )
    }
    
    val protectFeatures = remember {
        listOf(
            FeatureItem("Website Scanner", "Check if a website is safe", Icons.Default.Security, Color(0xFF2196F3), "website_checker"),
            FeatureItem("Scam Messages", "Review detected suspicious messages", Icons.Default.Message, Color(0xFFFF5252), "history")
        )
    }
    
    val discussFeatures = remember {
        listOf(
            FeatureItem("Scam Reports", "Share and read scam experiences", Icons.Default.Report, Color(0xFFE91E63), "scam_reports"),
            FeatureItem("Feedback", "Share your thoughts with us", Icons.Default.Feedback, Color(0xFF00BCD4), "user_feedback")
        )
    }
    
    // Filter features based on search text
    fun filterFeatures(features: List<FeatureItem>, query: String): List<FeatureItem> {
        if (query.isBlank()) return features
        return features.filter { 
            it.title.contains(query, ignoreCase = true) || 
            it.description.contains(query, ignoreCase = true)
        }
    }
    
    val filteredLearnFeatures = remember(searchText, learnFeatures) { filterFeatures(learnFeatures, searchText) }
    val filteredProtectFeatures = remember(searchText, protectFeatures) { filterFeatures(protectFeatures, searchText) }
    val filteredDiscussFeatures = remember(searchText, discussFeatures) { filterFeatures(discussFeatures, searchText) }

    BackgroundWrapper(imageResId = R.drawable.bg_profile) {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = { com.smartguard.app.mainapp.resources.SmartGuardBottomBar(nav, "home") }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                // Top Greeting Section
                TopGreeting(
                    userName = vm.currentUser.collectAsState().value?.displayName ?: "User",
                    onProfileClick = {
                        vm.logout()
                        nav.navigate("login") {
                            popUpTo("home") { inclusive = true }
                        }
                    }
                )

                // Search Bar - outside white card
                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                    SearchBar(
                        searchText = searchText,
                        onSearchChange = { searchText = it }
                    )
                }
                
                // Permission banner moved to Profile screen
                
                Spacer(Modifier.height(32.dp))

                // Single White Card - extends to bottom
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 28.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 20.dp)
                    ) {
                        // Show message if no results
                        if (filteredLearnFeatures.isEmpty() && filteredProtectFeatures.isEmpty() && filteredDiscussFeatures.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 40.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = null,
                                            tint = Color(0xFF9E9E9E),
                                            modifier = Modifier.size(48.dp)
                                        )
                                        Spacer(Modifier.height(12.dp))
                                        Text(
                                            text = "No features found",
                                            color = Color(0xFF9E9E9E),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }
                        }
                        
                        // LEARN Category
                        if (filteredLearnFeatures.isNotEmpty()) {
                            item {
                                CategoryHeader(title = "Learn")
                            }
                            items(filteredLearnFeatures) { feature ->
                                FeatureItemRow(
                                    feature = feature,
                                    onClick = { nav.navigate(feature.route) }
                                )
                            }
                            item { Spacer(Modifier.height(24.dp)) }
                        }
                        
                        // PROTECT Category
                        if (filteredProtectFeatures.isNotEmpty()) {
                            item {
                                CategoryHeader(title = "Protect")
                            }
                            items(filteredProtectFeatures) { feature ->
                                FeatureItemRow(
                                    feature = feature,
                                    onClick = { nav.navigate(feature.route) }
                                )
                            }
                            item { Spacer(Modifier.height(24.dp)) }
                        }
                        
                        // DISCUSS Category
                        if (filteredDiscussFeatures.isNotEmpty()) {
                            item {
                                CategoryHeader(title = "Discuss")
                            }
                            items(filteredDiscussFeatures) { feature ->
                                FeatureItemRow(
                                    feature = feature,
                                    onClick = { nav.navigate(feature.route) }
                                )
                            }
                        }
                        
                        item { Spacer(Modifier.height(16.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchBar(searchText: String = "", onSearchChange: (String) -> Unit = {}) {
    TextField(
        value = searchText,
        onValueChange = { onSearchChange(it) },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        placeholder = { 
            Text(
                "Search features...",
                color = Color(0xFF9E9E9E)
            ) 
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = Color(0xFF9E9E9E)
            )
        },
        trailingIcon = if (searchText.isNotEmpty()) {
            {
                IconButton(
                    onClick = { onSearchChange("") },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear",
                        tint = Color(0xFF9E9E9E)
                    )
                }
            }
        } else null,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFF5F5F5),
            unfocusedContainerColor = Color(0xFFF5F5F5),
            disabledContainerColor = Color(0xFFF5F5F5),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        shape = RoundedCornerShape(16.dp),
        singleLine = true
    )
}

@Composable
fun CategoryHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF1E1E1E),
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

@Composable
fun TopGreeting(userName: String, onProfileClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Hello, $userName",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Stay protected from scams",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
        Spacer(Modifier.weight(1f))
        Card(
            modifier = Modifier
                .size(48.dp)
                .clickable { onProfileClick() },
            shape = CircleShape,
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.2f))
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = "Sign Out",
                    modifier = Modifier.size(24.dp),
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun FeatureItemRow(feature: FeatureItem, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        feature.color.copy(alpha = 0.15f),
                        feature.color.copy(alpha = 0.05f)
                    )
                )
            )
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon with colored background
            Card(
                colors = CardDefaults.cardColors(containerColor = feature.color.copy(alpha = 0.25f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.size(72.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = feature.icon,
                        contentDescription = feature.title,
                        tint = feature.color,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
            
            Spacer(Modifier.width(18.dp))
            
            // Text content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = feature.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF1E1E1E),
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = feature.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF757575),
                    maxLines = 2
                )
            }
            
            Spacer(Modifier.width(8.dp))
            
            // Arrow icon
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Go",
                tint = feature.color,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun NotificationPermissionBanner(onSetupClick: () -> Unit, onRefresh: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFF9800)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Notification",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Enable WhatsApp Scanning",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Get real-time scam detection for WhatsApp messages",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Button(
                onClick = onSetupClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Setup",
                    color = Color(0xFFFF9800),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.width(4.dp))
            
            IconButton(
                onClick = onRefresh,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}



