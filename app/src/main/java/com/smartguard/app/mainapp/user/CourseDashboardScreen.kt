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
import com.smartguard.app.R
import com.smartguard.app.mainapp.common.BackgroundWrapper
import com.smartguard.app.mainapp.resources.SmartGuardBottomBar
import com.smartguard.app.model.ScamCourse

@Composable
fun CourseDashboardScreen(nav: NavController) {
    val viewModel: com.smartguard.app.viewmodel.CourseViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val uiState by viewModel.uiState.collectAsState()
    
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedCourse by remember { mutableStateOf<ScamCourse?>(null) }
    
    val categories = listOf("All", "Beginner", "Intermediate", "Advanced", "Featured")
    
    val allCourses = viewModel.toScamCourses()
    
    // Create a list that includes completion status - this will update when progress changes
    data class CourseWithStatus(val course: ScamCourse, val isCompleted: Boolean)
    
    val coursesWithStatus = remember(allCourses, uiState.progress) {
        allCourses.map { course ->
            CourseWithStatus(
                course = course,
                isCompleted = viewModel.isCourseCompleted(course.title)
            )
        }
    }
    
    val filteredCourses = remember(selectedCategory, coursesWithStatus) {
        if (selectedCategory == "All") coursesWithStatus
        else if (selectedCategory == "Featured") coursesWithStatus.filter { it.course.isNew }
        else coursesWithStatus.filter { it.course.level == selectedCategory }
    }
    
    // Get progress stats - recalculate when uiState changes
    val (completed, inProgress, available) = remember(uiState.progress, uiState.courses) {
        viewModel.getProgressStats()
    }

    BackgroundWrapper(imageResId = R.drawable.bg_profile) {
        Scaffold(
            containerColor = Color.Transparent,
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
            bottomBar = { SmartGuardBottomBar(nav, currentRoute = "tips") }
        ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF4CAF50))
            }
        } else if (uiState.error != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Error loading courses",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        uiState.error ?: "",
                        color = Color.LightGray,
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { viewModel.loadCourses() }) {
                        Text("Retry")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Welcome Section
                item {
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
                }
            
            // Progress Section - MOVED TO TOP
            item {
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
                            ProgressStat("Completed", completed.toString(), Icons.Default.CheckCircle, Color(0xFF4CAF50))
                            ProgressStat("In Progress", inProgress.toString(), Icons.Default.Schedule, Color(0xFFFF9800))
                            ProgressStat("Available", available.toString(), Icons.Default.LibraryBooks, Color(0xFF2196F3))
                        }
                    }
                }
            }
            
            // Category Filter
            item {
                Spacer(Modifier.height(8.dp))
                Text("Categories", style = MaterialTheme.typography.titleMedium, color = Color.White)
            }
            
            item {
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
            }
            
            // Courses Section
            item {
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
            }

            // Course Grid
            items(filteredCourses, key = { it.course.title }) { courseWithStatus ->
                EnhancedCourseCard(
                    course = courseWithStatus.course,
                    isCompleted = courseWithStatus.isCompleted,
                    onClick = { selectedCourse = courseWithStatus.course }
                )
            }
            
            // Quick Tips Section
            item {
                Text("Quick Tips", style = MaterialTheme.typography.titleMedium, color = Color.White)
            }
            
            item {
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
fun EnhancedCourseCard(course: ScamCourse, isCompleted: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) Color(0xFF1B3A1B) else Color(0xFF2D2D2D)
        )
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
                        if (isCompleted) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Completed",
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier
                                    .size(20.dp)
                                    .padding(end = 4.dp)
                            )
                        }
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
                    Text(
                        if (isCompleted) "Review" else "Start",
                        color = Color(0xFF4CAF50)
                    )
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
