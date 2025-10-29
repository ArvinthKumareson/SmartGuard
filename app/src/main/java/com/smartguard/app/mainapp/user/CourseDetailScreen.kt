@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package com.smartguard.app.mainapp.user

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.smartguard.app.R
import com.smartguard.app.mainapp.common.BackgroundWrapper
import com.smartguard.app.mainapp.resources.SmartGuardBottomBar
import kotlinx.coroutines.launch
import android.widget.VideoView
import android.widget.MediaController
import android.net.Uri
import coil.compose.AsyncImage

@Composable
fun CourseDetailScreen(nav: NavController, courseTitle: String) {
    val viewModel: com.smartguard.app.viewmodel.CourseViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val uiState by viewModel.uiState.collectAsState()
    val courseContent = viewModel.getCourseContent(courseTitle)
    val isCourseCompleted = viewModel.isCourseCompleted(courseTitle)
    var showCompletionDialog by remember { mutableStateOf(false) }
    
    // Track if waited for initial load
    var hasWaitedForLoad by remember { mutableStateOf(false) }
    
    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading && uiState.courses.isNotEmpty()) {
            hasWaitedForLoad = true
        }
    }
    
    BackgroundWrapper(imageResId = R.drawable.bg_profile) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text(courseTitle, color = Color.White) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E1E1E)),
                    navigationIcon = {
                        IconButton(onClick = { nav.popBackStack() }) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    }
                )
            },
            bottomBar = { SmartGuardBottomBar(nav, currentRoute = "tips") }
        ) { padding ->
            // Show loading state (or if course not found coz haven't finished loading yet)
            if (uiState.isLoading || (courseContent == null && !hasWaitedForLoad)) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Color(0xFF4CAF50))
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Loading course...",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                return@Scaffold
            }
            
            // Show error state
            if (uiState.error != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = "Error",
                            tint = Color.Red,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Error loading course",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            uiState.error ?: "Unknown error",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadCourses() }) {
                            Text("Retry")
                        }
                    }
                }
                return@Scaffold
            }
            
            // Show not found state (only if waited and it's still null)
            if (courseContent == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.SearchOff,
                            contentDescription = "Not Found",
                            tint = Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Course not found",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            "This course may not be available yet",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { nav.popBackStack() }) {
                            Text("Go Back")
                        }
                    }
                }
                return@Scaffold
            }
            
            // Show course content (courseContent is guaranteed non-null here)
            val pagerState = rememberPagerState(pageCount = { courseContent.tips.size + 1 })
            val scope = rememberCoroutineScope()
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Progress Bar
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E1E1E))
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Course Progress",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.Gray
                        )
                        Text(
                            "${pagerState.currentPage + 1} / ${courseContent.tips.size + 1}",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { (pagerState.currentPage + 1).toFloat() / (courseContent.tips.size + 1) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = Color(0xFF4CAF50),
                        trackColor = Color(0xFF424242),
                    )
                }
                
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f)
                ) { page ->
                    if (page < courseContent.tips.size) {
                        // Show tip page
                        TipDetailPage(
                            tip = courseContent.tips[page],
                            tipNumber = page + 1,
                            totalTips = courseContent.tips.size,
                            onNext = {
                                scope.launch {
                                    pagerState.animateScrollToPage(page + 1)
                                }
                            },
                            onPrevious = {
                                if (page > 0) {
                                    scope.launch {
                                        pagerState.animateScrollToPage(page - 1)
                                    }
                                }
                            },
                            isLastTip = page == courseContent.tips.size - 1
                        )
                    } else {
                        // Show completion page
                        CompletionPage(
                            isCourseCompleted = isCourseCompleted,
                            onMarkComplete = {
                                if (!isCourseCompleted) {
                                    viewModel.markCourseCompleted(courseTitle)
                                    showCompletionDialog = true
                                }
                            },
                            onPrevious = {
                                scope.launch {
                                    pagerState.animateScrollToPage(page - 1)
                                }
                            }
                        )
                    }
                }

                // Page Indicators
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(courseContent.tips.size + 1) { index ->
                        val isSelected = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(if (isSelected) 12.dp else 8.dp)
                                .background(
                                    color = if (isSelected) Color(0xFF4CAF50) else Color.Gray,
                                    shape = RoundedCornerShape(50)
                                )
                        )
                    }
                }
            }
        }
    }
    
    // Completion Dialog
    if (showCompletionDialog) {
        AlertDialog(
            onDismissRequest = { showCompletionDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    showCompletionDialog = false
                    nav.popBackStack()
                }) {
                    Text("Back to Courses", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCompletionDialog = false }) {
                    Text("Continue Learning", color = Color.Gray)
                }
            },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Congratulations!", color = Color.White)
                }
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "You've completed this course!",
                        color = Color.LightGray,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Keep learning to strengthen your cybersecurity knowledge and protect yourself from digital threats.",
                        color = Color.LightGray,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            },
            containerColor = Color(0xFF1E1E1E)
        )
    }
}

@Composable
fun TipDetailPage(
    tip: CourseTip,
    tipNumber: Int,
    totalTips: Int,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    isLastTip: Boolean = false
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = if (tip.isImportant) {
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF1B5E20), Color(0xFF2E7D32))
                    )
                } else {
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF2D2D2D), Color(0xFF1E1E1E))
                    )
                }
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            // Header Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Card(
                        modifier = Modifier.size(48.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (tip.isImportant) Color(0xFF4CAF50) else Color(0xFF2196F3)
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            if (tip.isImportant) {
                                Icon(Icons.Default.PriorityHigh, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                            } else {
                                Text(
                                    tipNumber.toString(),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                )
                            }
                        }
                    }
                    
                    Spacer(Modifier.width(16.dp))
                    
                    Column {
                        Text(
                            "Tip $tipNumber of $totalTips",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                        if (tip.isImportant) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50))
                            ) {
                                Text(
                                    "IMPORTANT",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
                
                Icon(
                    tip.icon,
                    contentDescription = null,
                    tint = if (tip.isImportant) Color(0xFF4CAF50) else Color(0xFF2196F3),
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Spacer(Modifier.height(20.dp))
            
            // Tip Title
            Text(
                tip.title,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(Modifier.height(16.dp))
            
            // Display Image if available
            tip.imageUrl?.let { imageUrl ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    var isLoading by remember { mutableStateOf(true) }
                    var hasError by remember { mutableStateOf(false) }
                    
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = tip.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            onLoading = { isLoading = true },
                            onSuccess = { 
                                isLoading = false
                                hasError = false
                            },
                            onError = { 
                                isLoading = false
                                hasError = true
                            }
                        )
                        
                        if (isLoading) {
                            CircularProgressIndicator(color = Color(0xFF4CAF50))
                        }
                        
                        if (hasError) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.BrokenImage,
                                    contentDescription = "Failed to load",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(36.dp)
                                )
                                Text(
                                    "Failed to load image",
                                    color = Color.Gray,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
            
            // Display Video if available
            tip.videoUri?.let { videoUri ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.Black)
                ) {
                    AndroidView(
                        factory = { context ->
                            VideoView(context).apply {
                                setVideoURI(Uri.parse(videoUri))
                                val mediaController = MediaController(context)
                                mediaController.setAnchorView(this)
                                setMediaController(mediaController)
                                
                                setOnPreparedListener { mp ->
                                    mp.isLooping = false
                                    start()
                                }
                                
                                setOnErrorListener { _, what, extra ->
                                    android.util.Log.e("VideoPlayer", "Error: what=$what, extra=$extra")
                                    true
                                }
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(Modifier.height(16.dp))
            }
            
            // Detailed Content
            Text(
                tip.detailedContent,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.LightGray,
                textAlign = TextAlign.Justify
            )
            
            Spacer(Modifier.height(20.dp))
            
            // Action Steps
            if (tip.actionSteps.isNotEmpty()) {
                Text(
                    "Action Steps:",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(12.dp))
                
                tip.actionSteps.forEachIndexed { index, step ->
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            "${index + 1}.",
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            step,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.LightGray,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(Modifier.height(20.dp))
            }
            
            // Navigation Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Previous Button
                Button(
                    onClick = onPrevious,
                    enabled = tipNumber > 1,
                    modifier = Modifier
                        .weight(0.48f)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50),
                        disabledContainerColor = Color(0xFF424242)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Previous",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Previous")
                }
                
                Spacer(Modifier.width(12.dp))
                
                // Next Button
                Button(
                    onClick = onNext,
                    modifier = Modifier
                        .weight(0.48f)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (isLastTip) "Complete" else "Next")
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        if (isLastTip) Icons.Default.CheckCircle else Icons.Default.ArrowForward,
                        contentDescription = if (isLastTip) "Complete" else "Next",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun CompletionPage(
    isCourseCompleted: Boolean,
    onMarkComplete: () -> Unit,
    onPrevious: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF1B5E20), Color(0xFF2E7D32))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(40.dp))
            
            // Completion Content
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    if (isCourseCompleted) Icons.Default.CheckCircle else Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(120.dp)
                )
                
                Spacer(Modifier.height(24.dp))
                
                Text(
                    if (isCourseCompleted) "Course Completed!" else "Ready to Complete?",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Spacer(Modifier.height(16.dp))
                
                Text(
                    if (isCourseCompleted) 
                        "Great job! You've mastered this course and strengthened your cybersecurity knowledge." 
                    else 
                        "You've reviewed all the tips. Mark this course as complete to track your progress.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.LightGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                
                Spacer(Modifier.height(32.dp))
                
                // Additional Resources Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D2D)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "Keep Learning",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "• Practice these tips regularly to build good security habits",
                            color = Color.LightGray,
                            fontSize = 14.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "• Share this knowledge with family and friends",
                            color = Color.LightGray,
                            fontSize = 14.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "• Stay updated with the latest security threats",
                            color = Color.LightGray,
                            fontSize = 14.sp
                        )
                    }
                }
            }
            
            // Bottom Buttons
            Column {
                if (!isCourseCompleted) {
                    Button(
                        onClick = onMarkComplete,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Mark as Complete",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                }
                
                Button(
                    onClick = onPrevious,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF424242)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Previous",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Review Tips")
                }
            }
        }
    }
}

@Composable
fun TipCard(
    tipNumber: Int,
    title: String,
    description: String,
    icon: ImageVector,
    isImportant: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isImportant) Color(0xFF1B5E20) else Color(0xFF2D2D2D)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Card(
                modifier = Modifier.size(40.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isImportant) Color(0xFF4CAF50) else Color(0xFF2196F3)
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (isImportant) {
                        Icon(Icons.Default.PriorityHigh, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    } else {
                        Text(
                            tipNumber.toString(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
            
            Spacer(Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        title,
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                    if (isImportant) {
                        Spacer(Modifier.width(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50))
                        ) {
                            Text(
                                "IMPORTANT",
                                color = Color.White,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    description,
                    color = Color.LightGray,
                    fontSize = 14.sp
                )
            }
        }
    }
}

data class CourseTip(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val isImportant: Boolean = false,
    val detailedContent: String = "",
    val actionSteps: List<String> = emptyList(),
    val imageUrl: String? = null, // Optional image URL from cloud storage
    val videoUri: String? = null // Optional video URI (can be cloud URL or raw resource)
)

data class CourseContent(
    val title: String,
    val description: String,
    val level: String,
    val tips: List<CourseTip>,
    val isNew: Boolean = false
)
