@file:OptIn(ExperimentalMaterial3Api::class)

package com.smartguard.app.mainapp.user

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.smartguard.app.viewmodel.AuthViewModel
import com.smartguard.app.mainapp.common.BackgroundWrapper
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
    var searchQuery by remember { mutableStateOf("") }
    
    val features = remember {
        listOf(
            FeatureItem("Advisory Tips", "Learn how to identify and avoid scams", Icons.Default.Lightbulb, Color(0xFF4CAF50), "tips"),
            FeatureItem("Website Scanner", "Check if a website is safe before visiting", Icons.Default.Security, Color(0xFF2196F3), "website_checker"),
            FeatureItem("Scam Game", "Practice spotting scams through interactive scenarios", Icons.Default.Games, Color(0xFFFF9800), "scam chat"),
            FeatureItem("Quiz", "Test your knowledge about scam prevention", Icons.Default.Quiz, Color(0xFF9C27B0), "quiz"),
            FeatureItem("Scam Messages", "Review detected suspicious messages", Icons.Default.Message, Color(0xFFFF5252), "history"),
            FeatureItem("Feedback", "Share your thoughts and suggestions with us", Icons.Default.Feedback, Color(0xFF00BCD4), "user_feedback")
        )
    }
    
    val filteredFeatures = remember(searchQuery) {
        if (searchQuery.isEmpty()) {
            features
        } else {
            features.filter { it.title.contains(searchQuery, ignoreCase = true) }
        }
    }

    BackgroundWrapper(imageResId = R.drawable.bg_profile) {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = { BottomNavigationBar(nav) }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                TopGreeting(
                    userName = vm.currentUser.collectAsState().value?.displayName ?: "User",
                    onProfileClick = {
                        vm.logout()
                        nav.navigate("login") {
                            popUpTo("home") { inclusive = true }
                        }
                    }
                )

                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredFeatures) { feature ->
                        ModernFeatureCard(
                            feature = feature,
                            onClick = { nav.navigate(feature.route) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TopGreeting(userName: String, onProfileClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Hello, $userName ",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Stay protected from scams",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
        Spacer(Modifier.weight(1f))
        IconButton(
            onClick = onProfileClick,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "Profile",
                modifier = Modifier.size(40.dp),
                tint = Color.White
            )
        }
    }
}

@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text("Search features...", color = Color.Gray) },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray)
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.Gray)
                }
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = Color(0xFF6200EE),
            unfocusedBorderColor = Color.Gray,
            cursorColor = Color.White,
            focusedContainerColor = Color(0xFF1E1E1E),
            unfocusedContainerColor = Color(0xFF1E1E1E)
        ),
        shape = RoundedCornerShape(16.dp),
        singleLine = true
    )
}

@Composable
fun ModernFeatureCard(feature: FeatureItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = feature.color.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(56.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = feature.icon,
                        contentDescription = feature.title,
                        tint = feature.color,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = feature.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = feature.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 2
                )
            }
            
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Go",
                tint = Color.Gray,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}


@Composable
fun BottomNavigationBar(nav: NavController) {
    NavigationBar(containerColor = Color(0xFF1E1E1E)) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text("Home") },
            selected = true,
            onClick = {}
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, contentDescription = null) },
            label = { Text("Profile") },
            selected = false,
            onClick = { nav.navigate("profile") }
        )
    }
}

