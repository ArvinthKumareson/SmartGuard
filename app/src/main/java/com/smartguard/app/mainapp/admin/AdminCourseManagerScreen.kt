@file:OptIn(ExperimentalMaterial3Api::class)

package com.smartguard.app.mainapp.admin

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.smartguard.app.R
import com.smartguard.app.data.FirebaseCourseContent
import com.smartguard.app.mainapp.common.BackgroundWrapper
import com.smartguard.app.viewmodel.AdminCourseViewModel

@Composable
fun AdminCourseManagerScreen(
    nav: NavController,
    vm: AdminCourseViewModel = viewModel(
        factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.getInstance(
            LocalContext.current.applicationContext as android.app.Application
        )
    )
) {
    Log.d("AdminCourseManagerScreen", "Screen composing...")
    
    val uiState by vm.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf<FirebaseCourseContent?>(null) }
    
    // Show success/error messages
    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            // Auto-clear after showing
            kotlinx.coroutines.delay(3000)
            vm.clearMessages()
        }
    }
    
    // Ensure courses list is safe and non-null
    val safeCourses = remember(uiState.courses) {
        uiState.courses.filterNotNull().distinctBy { it.title }
    }
    
    BackgroundWrapper(imageResId = R.drawable.bg_profile) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Manage Courses", color = Color.White) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF1E1E1E)
                    ),
                    navigationIcon = {
                        IconButton(onClick = { nav.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = { nav.navigate("admin_course_editor/new") }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Course", tint = Color(0xFF4CAF50))
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { nav.navigate("admin_course_editor/new") },
                    containerColor = Color(0xFF4CAF50),
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Course")
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                // Success/Error Messages
                uiState.successMessage?.let { message ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B5E20))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50))
                            Spacer(Modifier.width(8.dp))
                            Text(message, color = Color.White)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
                
                uiState.error?.let { message ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF5D1F1F))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Error, contentDescription = null, tint = Color.Red)
                            Spacer(Modifier.width(8.dp))
                            Text(message, color = Color.White)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
                
                // Course Stats
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D2D))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.LibraryBooks, contentDescription = null, tint = Color(0xFF4CAF50))
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "${safeCourses.size}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                            Text("Total Courses", color = Color.Gray, fontSize = 12.sp)
                        }
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.NewReleases, contentDescription = null, tint = Color(0xFFFF9800))
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "${safeCourses.count { it.isNew }}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                            Text("New Courses", color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                
                // Loading State
                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF4CAF50))
                    }
                } else if (safeCourses.isEmpty()) {
                    // Empty State
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.LibraryBooks,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(Modifier.height(16.dp))
                            Text("No courses yet", color = Color.Gray, fontSize = 18.sp)
                            Spacer(Modifier.height(8.dp))
                            Button(onClick = { nav.navigate("admin_course_editor/new") }) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(Modifier.width(4.dp))
                                Text("Create First Course")
                            }
                        }
                    }
                } else {
                    // Course List
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 100.dp, top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(
                            items = safeCourses,
                            key = { index, course -> "${course.title}_$index" }
                        ) { index, course ->
                            AdminCourseCard(
                                course = course,
                                onEdit = {
                                    val encodedTitle = runCatching {
                                        java.net.URLEncoder.encode(course.title, "UTF-8")
                                    }.getOrElse {
                                        Log.e("AdminCourseManager", "Error encoding title", it)
                                        course.title
                                    }
                                    nav.navigate("admin_course_editor/$encodedTitle")
                                },
                                onDelete = { showDeleteDialog = course }
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Delete Confirmation Dialog
    showDeleteDialog?.let { course ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.deleteCourse(course.title)
                        showDeleteDialog = null
                    }
                ) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            title = { Text("Delete Course?", color = Color.White) },
            text = {
                Text(
                    "Are you sure you want to delete \"${course.title}\"? This action cannot be undone.",
                    color = Color.LightGray
                )
            },
            containerColor = Color(0xFF1E1E1E)
        )
    }
}

@Composable
fun AdminCourseCard(
    course: FirebaseCourseContent,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D2D))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (course.isNew) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50)),
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Text(
                                    "NEW",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = course.title.takeIf { it.isNotBlank() } ?: "Untitled Course",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            maxLines = 2,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = course.description.takeIf { it.isNotBlank() } ?: "No description",
                        color = Color.LightGray,
                        fontSize = 14.sp,
                        maxLines = 3,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    
                    Spacer(Modifier.height(8.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = when (course.level.lowercase().trim()) {
                                    "beginner" -> Color(0xFF4CAF50)
                                    "intermediate" -> Color(0xFFFF9800)
                                    "advanced" -> Color(0xFFF44336)
                                    else -> Color(0xFF2196F3)
                                }
                            )
                        ) {
                            Text(
                                text = course.level.trim().takeIf { it.isNotBlank() } ?: "Unknown",
                                color = Color.White,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LibraryBooks, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(2.dp))
                            Text(
                                text = "${course.tips.size} tip${if (course.tips.size != 1) "s" else ""}",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
                
                Column {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF2196F3))
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFF44336))
                    }
                }
            }
        }
    }
}

