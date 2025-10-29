@file:OptIn(ExperimentalMaterial3Api::class)

package com.smartguard.app.mainapp.user

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
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
    var searchQuery by remember { mutableStateOf("") }
    
    val categories = listOf("All", "Beginner", "Intermediate", "Advanced", "Featured")
    
    val allCourses = viewModel.toScamCourses()
    
    // Create a list that includes completion status, this will update when progress changes
    data class CourseWithStatus(val course: ScamCourse, val isCompleted: Boolean)
    
    val coursesWithStatus = remember(allCourses, uiState.progress) {
        allCourses.map { course ->
            CourseWithStatus(
                course = course,
                isCompleted = viewModel.isCourseCompleted(course.title)
            )
        }
    }
    
    val filteredCourses = remember(selectedCategory, coursesWithStatus, searchQuery) {
        var courses = if (selectedCategory == "All") coursesWithStatus
            else if (selectedCategory == "Featured") coursesWithStatus.filter { it.course.isNew }
            else coursesWithStatus.filter { it.course.level == selectedCategory }
        
        // Apply search filter
        if (searchQuery.isNotBlank()) {
            courses = courses.filter { 
                it.course.title.contains(searchQuery, ignoreCase = true) ||
                it.course.description.contains(searchQuery, ignoreCase = true)
            }
        }
        
        courses
    }
    
    // Get progress stats, recalculate when uiState changes
    val (completed, inProgress, available) = remember(uiState.progress, uiState.courses) {
        viewModel.getProgressStats()
    }
    
    // Refresh data when screen becomes visible
    LaunchedEffect(Unit) {
        viewModel.refreshProgress()
    }

    BackgroundWrapper(imageResId = R.drawable.bg_profile) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("SmartGuard Training", color = Color.White) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E1E1E))
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
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Welcome Section with Gradient
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF1B5E20),
                                        Color(0xFF4CAF50)
                                    )
                                )
                            )
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.School,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    "SmartGuard Training",
                                    fontSize = 20.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "Master cybersecurity skills and protect yourself from digital threats",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            
            // Compact Progress Section
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF1E1E1E))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CompactProgressStat("Completed", completed.toString(), Icons.Default.CheckCircle, Color(0xFF4CAF50))
                        CompactProgressStat("In Progress", inProgress.toString(), Icons.Default.Schedule, Color(0xFFFF9800))
                        CompactProgressStat("Available", available.toString(), Icons.Default.LibraryBooks, Color(0xFF2196F3))
                    }
                }
            }
            
            // Modern Search Bar
            item {
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(16.dp)),
                    placeholder = { Text("Search for courses...", color = Color.Gray) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color(0xFF4CAF50)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = Color.Gray
                                )
                            }
                        }
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF1E1E1E),
                        unfocusedContainerColor = Color(0xFF1E1E1E),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color(0xFF4CAF50),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )
            }
            
            // Category Filter
            item {
                Column {
                    Text(
                        "Categories",
                        fontSize = 18.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(10.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(categories) { category ->
                            ModernCategoryChip(
                                category = category,
                                isSelected = selectedCategory == category,
                                onClick = { selectedCategory = category }
                            )
                        }
                    }
                }
            }
            
            // Courses Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "All Courses",
                        fontSize = 18.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        color = Color(0xFF2D2D2D),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            "${filteredCourses.size} Courses",
                            color = Color(0xFF4CAF50),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            // Course Grid
            items(filteredCourses, key = { it.course.title }) { courseWithStatus ->
                EnhancedCourseCard(
                    course = courseWithStatus.course,
                    isCompleted = courseWithStatus.isCompleted,
                    onClick = { 
                        if (!uiState.isLoading) {
                            selectedCourse = courseWithStatus.course 
                        }
                    }
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
                            Text("Get dailyS tips to stay secure", color = Color.LightGray, fontSize = 12.sp)
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
                TextButton(
                    onClick = {
                        if (!uiState.isLoading) {
                            selectedCourse = null
                            nav.navigate("courseDetail/${course.title}")
                        }
                    },
                    enabled = !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.Gray,
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Loading...", color = Color.Gray)
                        }
                    } else {
                        Text("Start Course", color = Color.White)
                    }
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
                    Text("Difficulty: ${course.level}", color = Color.Gray)
                    
                    if (uiState.isLoading) {
                        Spacer(Modifier.height(12.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color(0xFF4CAF50),
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "Loading course data...",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
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
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0x33000000)),
        shape = RoundedCornerShape(20.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = when (course.level) {
                            "Beginner" -> Color(0xFF4CAF50)
                            "Intermediate" -> Color(0xFFFF9800)
                            "Advanced" -> Color(0xFFF44336)
                            else -> Color(0xFF2196F3)
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            course.level,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    if (course.isNew) {
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            color = Color(0xFF4CAF50),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                "NEW",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                if (isCompleted) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4CAF50)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Completed",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                course.title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                lineHeight = 22.sp
            )

            Spacer(Modifier.height(6.dp))

            Text(
                course.description,
                color = Color.LightGray,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                maxLines = 2
            )

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (isCompleted) "Review Course" else "Start Learning",
                    color = Color(0xFF4CAF50),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.width(8.dp))
                Icon(
                    Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun CompactProgressStat(label: String, value: String, icon: ImageVector, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Column {
            Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(label, color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun ModernCategoryChip(category: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (isSelected) {
                    Brush.horizontalGradient(
                        colors = listOf(Color(0xFF1B5E20), Color(0xFF4CAF50))
                    )
                } else {
                    Brush.horizontalGradient(
                        colors = listOf(Color(0xFF2D2D2D), Color(0xFF2D2D2D))
                    )
                }
            )
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Text(
            text = category,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}
