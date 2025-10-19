@file:OptIn(ExperimentalMaterial3Api::class)

package com.smartguard.app.mainapp.user

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.smartguard.app.mainapp.resources.CourseCard
import com.smartguard.app.mainapp.resources.SmartGuardBottomBar
import com.smartguard.app.model.ScamCourse

@Composable
fun CourseDashboardScreen(nav: NavController) {
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedCourse by remember { mutableStateOf<ScamCourse?>(null) }
    
    val categories = listOf("All", "Beginner", "Intermediate", "Advanced", "Featured")
    
    val allCourses = remember {
        listOf(
            // Beginner Courses
            ScamCourse("Spot Fake Delivery SMS", "Learn to identify spoofed courier messages and avoid delivery scams.", "Beginner", 4.9f, true),
            ScamCourse("Basic Password Security", "Master the fundamentals of creating and managing secure passwords.", "Beginner", 4.8f, false),
            ScamCourse("Recognize Phishing Emails", "Identify common phishing tactics and suspicious email patterns.", "Beginner", 4.7f, false),
            ScamCourse("Safe Online Shopping", "Protect yourself while shopping online and avoid fake e-commerce sites.", "Beginner", 4.6f, false),
            ScamCourse("Social Media Privacy", "Secure your social media accounts and protect your personal information.", "Beginner", 4.5f, false),
            
            // Intermediate Courses
            ScamCourse("OTP Scams Explained", "Understand how scammers trick users into sharing OTPs and verification codes.", "Intermediate", 4.8f, false),
            ScamCourse("Banking Fraud Prevention", "Learn to protect your financial accounts from sophisticated fraud schemes.", "Intermediate", 4.7f, false),
            ScamCourse("Two-Factor Authentication", "Implement and manage 2FA across your digital accounts effectively.", "Intermediate", 4.6f, false),
            ScamCourse("WiFi Security Essentials", "Secure your home and public WiFi connections from cyber threats.", "Intermediate", 4.5f, false),
            ScamCourse("Mobile App Security", "Identify malicious apps and protect your mobile device from threats.", "Intermediate", 4.4f, false),
            
            // Advanced Courses
            ScamCourse("Phishing via Email", "Detect spoofed sender names, fake domains, and advanced phishing techniques.", "Advanced", 4.7f, false),
            ScamCourse("Cryptocurrency Scams", "Navigate the crypto space safely and avoid investment fraud schemes.", "Advanced", 4.6f, false),
            ScamCourse("Social Engineering Defense", "Recognize and counter sophisticated social engineering attacks.", "Advanced", 4.5f, false),
            ScamCourse("Dark Web Awareness", "Understand dark web threats and protect against data breaches.", "Advanced", 4.4f, false),
            ScamCourse("Advanced Threat Detection", "Identify sophisticated cyber threats and implement defense strategies.", "Advanced", 4.3f, false),
            
            // Featured Courses
            ScamCourse("AI-Powered Scam Detection", "Learn to identify AI-generated content and deepfake scams.", "Advanced", 4.9f, true),
            ScamCourse("Elderly Protection Guide", "Comprehensive guide to protect senior citizens from digital scams.", "Beginner", 4.8f, true),
            ScamCourse("Business Email Compromise", "Protect your business from BEC attacks and CEO fraud schemes.", "Advanced", 4.7f, true)
        )
    }
    
    val filteredCourses = remember(selectedCategory) {
        if (selectedCategory == "All") allCourses
        else allCourses.filter { it.level == selectedCategory }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SmartGuard Training", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E1E1E)),
                actions = {
                    IconButton(onClick = { /* Search functionality */ }) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White)
                    }
                }
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
            // Welcome Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D2D))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "Welcome to SmartGuard Training!",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Master cybersecurity skills and protect yourself from digital threats",
                        color = Color.LightGray,
                        fontSize = 14.sp
                    )
                }
            }
            
            Spacer(Modifier.height(24.dp))
            
            // Category Filter
            Text("Categories", style = MaterialTheme.typography.titleMedium, color = Color.White)
            Spacer(Modifier.height(8.dp))
            
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { category ->
                    FilterChip(
                        onClick = { selectedCategory = category },
                        label = { Text(category, color = if (selectedCategory == category) Color.Black else Color.White) },
                        selected = selectedCategory == category,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF4CAF50),
                            containerColor = Color(0xFF2D2D2D)
                        )
                    )
                }
            }
            
            Spacer(Modifier.height(24.dp))
            
            // Courses Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Courses (${filteredCourses.size})", 
                    style = MaterialTheme.typography.titleMedium, 
                    color = Color.White
                )
                TextButton(onClick = { /* Sort functionality */ }) {
                    Text("Sort", color = Color(0xFF4CAF50))
                }
            }
            Spacer(Modifier.height(8.dp))

            // Course Grid
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(filteredCourses) { course ->
                    EnhancedCourseCard(course) { selectedCourse = course }
                }
            }

            Spacer(Modifier.height(24.dp))
            
            // Progress Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D2D))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("Your Progress", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    Spacer(Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ProgressStat("Completed", "3", Icons.Default.CheckCircle, Color(0xFF4CAF50))
                        ProgressStat("In Progress", "2", Icons.Default.Schedule, Color(0xFFFF9800))
                        ProgressStat("Available", "12", Icons.Default.LibraryBooks, Color(0xFF2196F3))
                    }
                }
            }
            
            Spacer(Modifier.height(24.dp))
            
            // Quick Tips Section
            Text("Quick Tips", style = MaterialTheme.typography.titleMedium, color = Color.White)
            Spacer(Modifier.height(8.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D2D))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clickable { nav.navigate("tips") },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Lightbulb, contentDescription = null, tint = Color(0xFFFFD700))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Daily Security Tips", color = Color.White, fontWeight = FontWeight.Medium)
                        Text("Get daily tips to stay secure", color = Color.LightGray, fontSize = 12.sp)
                    }
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.Gray)
                }
            }
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
                    Text("Start Course", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedCourse = null }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            title = { 
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (course.isNew) {
                        Card(
                            modifier = Modifier.padding(end = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50))
                        ) {
                            Text("NEW", color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(4.dp))
                        }
                    }
                    Text(course.title, color = Color.White)
                }
            },
            text = {
                Column {
                    Text(course.description, color = Color.LightGray)
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Difficulty: ${course.level}", color = Color.Gray)
                        Text("Rating: ${course.rating} ⭐", color = Color(0xFFFFD700))
                    }
                }
            },
            containerColor = Color(0xFF1E1E1E)
        )
    }
}

@Composable
fun EnhancedCourseCard(course: ScamCourse, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D2D))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (course.isNew) {
                            Card(
                                modifier = Modifier.padding(end = 8.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50))
                            ) {
                                Text("NEW", color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(4.dp))
                            }
                        }
                        Text(
                            course.title,
                            color = Color.White,
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        course.description,
                        color = Color.LightGray,
                        fontSize = 14.sp
                    )
                }
                
                IconButton(onClick = { /* Favorite functionality */ }) {
                    Icon(
                        Icons.Default.FavoriteBorder,
                        contentDescription = "Add to favorites",
                        tint = Color.Gray
                    )
                }
            }
            
            Spacer(Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = when (course.level) {
                                "Beginner" -> Color(0xFF4CAF50)
                                "Intermediate" -> Color(0xFFFF9800)
                                "Advanced" -> Color(0xFFF44336)
                                else -> Color(0xFF2196F3)
                            }
                        )
                    ) {
                        Text(
                            course.level,
                            color = Color.White,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text("${course.rating} ⭐", color = Color(0xFFFFD700), fontSize = 12.sp)
                }
                
                TextButton(onClick = onClick) {
                    Text("Start", color = Color(0xFF4CAF50))
                }
            }
        }
    }
}

@Composable
fun ProgressStat(label: String, value: String, icon: ImageVector, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
        Spacer(Modifier.height(4.dp))
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(label, color = Color.Gray, fontSize = 12.sp)
    }
}
