@file:OptIn(ExperimentalMaterial3Api::class)

package com.smartguard.app.mainapp.admin

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.smartguard.app.data.FirebaseCourseTip
import com.smartguard.app.mainapp.common.BackgroundWrapper
import com.smartguard.app.viewmodel.AdminCourseViewModel
import kotlinx.coroutines.launch

@Composable
fun AdminCourseEditorScreen(
    nav: NavController,
    courseTitle: String?, // null for new course, encoded title for editing
    vm: AdminCourseViewModel = viewModel(
        factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.getInstance(
            LocalContext.current.applicationContext as android.app.Application
        )
    )
) {
    val scope = rememberCoroutineScope()
    val uiState by vm.uiState.collectAsState()
    
    // Course fields
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var level by remember { mutableStateOf("Beginner") }
    var isNew by remember { mutableStateOf(false) }
    var tips by remember { mutableStateOf<List<TipData>>(emptyList()) }
    
    var showTipEditor by remember { mutableStateOf(false) }
    var editingTipIndex by remember { mutableStateOf<Int?>(null) }
    
    val isEditing = courseTitle != null && courseTitle != "new"
    val originalTitle = remember { courseTitle?.let { java.net.URLDecoder.decode(it, "UTF-8") } }
    
    // Load existing course data
    LaunchedEffect(originalTitle, uiState.courses) {
        if (originalTitle != null && uiState.courses.isNotEmpty()) {
            val course = uiState.courses.find { it.title == originalTitle }
            course?.let {
                android.util.Log.d("AdminCourseEditor", "Loading course: ${it.title} with ${it.tips.size} tips")
                title = it.title
                description = it.description
                level = it.level
                isNew = it.isNew
                tips = it.tips.map { tip ->
                    TipData(
                        title = tip.title,
                        description = tip.description,
                        iconName = tip.iconName,
                        isImportant = tip.isImportant,
                        detailedContent = tip.detailedContent,
                        actionSteps = tip.actionSteps.toMutableList(),
                        imageUrl = tip.imageUrl,
                        videoUrl = tip.videoUrl
                    )
                }
                android.util.Log.d("AdminCourseEditor", "Loaded ${tips.size} tips into editor")
            }
        }
    }
    
    // Navigate back when save is successful
    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            nav.popBackStack()
            vm.clearMessages()
        }
    }
    
    // Show error message if any
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // You could show a snackbar here if you have a SnackbarHost
            vm.clearMessages()
        }
    }
    
    BackgroundWrapper(imageResId = R.drawable.bg_profile) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text(if (isEditing) "Edit Course" else "Create Course", color = Color.White) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF1E1E1E)
                    ),
                    navigationIcon = {
                        IconButton(onClick = { nav.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    actions = {
                        TextButton(
                            onClick = {
                                scope.launch {
                                    val tipsList = tips.map { it.toFirebaseCourseTip() }
                                    android.util.Log.d("AdminCourseEditor", "Saving course with ${tipsList.size} tips")
                                    
                                    if (isEditing && originalTitle != null) {
                                        android.util.Log.d("AdminCourseEditor", "Updating existing course: $originalTitle")
                                        vm.updateCourse(originalTitle, title, description, level, isNew, tipsList)
                                    } else {
                                        android.util.Log.d("AdminCourseEditor", "Creating new course: $title")
                                        vm.createCourse(title, description, level, isNew, tipsList)
                                    }
                                }
                            },
                            enabled = title.isNotBlank() && description.isNotBlank() && tips.isNotEmpty() && !uiState.isLoading
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = Color(0xFF4CAF50)
                                )
                            } else {
                                Text(if (isEditing) "Save" else "Create", color = Color(0xFF4CAF50))
                            }
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Error message display
                uiState.error?.let { errorMsg ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF44336))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White)
                            Spacer(Modifier.width(8.dp))
                            Text(errorMsg, color = Color.White, modifier = Modifier.weight(1f))
                            IconButton(onClick = { vm.clearMessages() }) {
                                Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = Color.White)
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }
                
                // Course Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Course Title") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF4CAF50),
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = Color(0xFF4CAF50),
                        unfocusedLabelColor = Color.Gray
                    )
                )
                
                Spacer(Modifier.height(12.dp))
                
                // Course Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF4CAF50),
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = Color(0xFF4CAF50),
                        unfocusedLabelColor = Color.Gray
                    )
                )
                
                Spacer(Modifier.height(12.dp))
                
                // Level Selector
                Text("Difficulty Level", color = Color.White, fontSize = 14.sp)
                Spacer(Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Beginner", "Intermediate", "Advanced").forEach { lvl ->
                        FilterChip(
                            onClick = { level = lvl },
                            label = { Text(lvl) },
                            selected = level == lvl,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = when (lvl) {
                                    "Beginner" -> Color(0xFF4CAF50)
                                    "Intermediate" -> Color(0xFFFF9800)
                                    "Advanced" -> Color(0xFFF44336)
                                    else -> Color(0xFF2196F3)
                                },
                                selectedLabelColor = Color.White,
                                labelColor = Color.Gray
                            )
                        )
                    }
                }
                
                Spacer(Modifier.height(12.dp))
                
                // Mark as New
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Mark as New Course", color = Color.White)
                    Switch(
                        checked = isNew,
                        onCheckedChange = { isNew = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF4CAF50),
                            checkedTrackColor = Color(0xFF66BB6A)
                        )
                    )
                }
                
                Spacer(Modifier.height(24.dp))
                
                // Tips Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Course Tips (${tips.size})", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Button(
                        onClick = {
                            editingTipIndex = null
                            showTipEditor = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Add Tip")
                    }
                }
                
                Spacer(Modifier.height(8.dp))
                
                if (tips.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D2D))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                                Spacer(Modifier.height(8.dp))
                                Text("No tips added yet", color = Color.Gray)
                            }
                        }
                    }
                } else {
                    tips.forEachIndexed { index, tip ->
                        TipListItem(
                            tip = tip,
                            tipNumber = index + 1,
                            onEdit = {
                                editingTipIndex = index
                                showTipEditor = true
                            },
                            onDelete = {
                                tips = tips.toMutableList().apply { removeAt(index) }
                            },
                            onMoveUp = if (index > 0) {
                                {
                                    tips = tips.toMutableList().apply {
                                        val temp = this[index]
                                        this[index] = this[index - 1]
                                        this[index - 1] = temp
                                    }
                                }
                            } else null,
                            onMoveDown = if (index < tips.size - 1) {
                                {
                                    tips = tips.toMutableList().apply {
                                        val temp = this[index]
                                        this[index] = this[index + 1]
                                        this[index + 1] = temp
                                    }
                                }
                            } else null
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    }
    
    // Tip Editor Dialog
    if (showTipEditor) {
        val existingTip = editingTipIndex?.let { tips.getOrNull(it) }
        TipEditorDialog(
            existingTip = existingTip,
            onDismiss = { 
                showTipEditor = false
                editingTipIndex = null
            },
            onSave = { tipData ->
                tips = if (editingTipIndex != null) {
                    tips.toMutableList().apply { 
                        if (editingTipIndex!! < size) {
                            set(editingTipIndex!!, tipData) 
                        }
                    }
                } else {
                    tips + tipData
                }
                showTipEditor = false
                editingTipIndex = null
            },
            vm = vm
        )
    }
}

data class TipData(
    var title: String = "",
    var description: String = "",
    var iconName: String = "Info",
    var isImportant: Boolean = false,
    var detailedContent: String = "",
    var actionSteps: MutableList<String> = mutableListOf(),
    var imageUrl: String? = null,
    var videoUrl: String? = null
) {
    fun toFirebaseCourseTip() = FirebaseCourseTip(
        title = title,
        description = description,
        iconName = iconName,
        isImportant = isImportant,
        detailedContent = detailedContent,
        actionSteps = actionSteps.toList(),
        imageUrl = imageUrl,
        videoUrl = videoUrl
    )
}

@Composable
fun TipListItem(
    tip: TipData,
    tipNumber: Int,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMoveUp: (() -> Unit)?,
    onMoveDown: (() -> Unit)?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (tip.isImportant) Color(0xFF1B3A1B) else Color(0xFF2D2D2D)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tip Number
            Card(
                modifier = Modifier.size(40.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (tip.isImportant) Color(0xFF4CAF50) else Color(0xFF2196F3)
                )
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(tipNumber.toString(), color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(Modifier.width(12.dp))
            
            // Tip Info
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(tip.title, color = Color.White, fontWeight = FontWeight.Medium)
                    if (tip.isImportant) {
                        Spacer(Modifier.width(4.dp))
                        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50))) {
                            Text("IMPORTANT", color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(4.dp))
                        }
                    }
                }
                Spacer(Modifier.height(2.dp))
                Text(tip.description, color = Color.Gray, fontSize = 12.sp, maxLines = 2)
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (tip.imageUrl != null) {
                        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF3F51B5))) {
                            Text("📷", fontSize = 10.sp, modifier = Modifier.padding(2.dp))
                        }
                    }
                    if (tip.videoUrl != null) {
                        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF9C27B0))) {
                            Text("🎥", fontSize = 10.sp, modifier = Modifier.padding(2.dp))
                        }
                    }
                }
            }
            
            // Action Buttons
            Column {
                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF2196F3), modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFF44336), modifier = Modifier.size(18.dp))
                    }
                }
                Row {
                    onMoveUp?.let {
                        IconButton(onClick = it, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.ArrowUpward, contentDescription = "Move Up", tint = Color.Gray, modifier = Modifier.size(18.dp))
                        }
                    }
                    onMoveDown?.let {
                        IconButton(onClick = it, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.ArrowDownward, contentDescription = "Move Down", tint = Color.Gray, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TipEditorDialog(
    existingTip: TipData?,
    onDismiss: () -> Unit,
    onSave: (TipData) -> Unit,
    vm: AdminCourseViewModel
) {
    val scope = rememberCoroutineScope()
    val uiState by vm.uiState.collectAsState()
    
    var title by remember { mutableStateOf(existingTip?.title ?: "") }
    var description by remember { mutableStateOf(existingTip?.description ?: "") }
    var iconName by remember { mutableStateOf(existingTip?.iconName ?: "Info") }
    var isImportant by remember { mutableStateOf(existingTip?.isImportant ?: false) }
    var detailedContent by remember { mutableStateOf(existingTip?.detailedContent ?: "") }
    var actionSteps by remember { mutableStateOf<List<String>>(existingTip?.actionSteps?.toList() ?: emptyList()) }
    var imageUrl by remember { mutableStateOf(existingTip?.imageUrl) }
    var videoUrl by remember { mutableStateOf(existingTip?.videoUrl) }
    var newActionStep by remember { mutableStateOf("") }
    
    // Image picker
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            scope.launch {
                val url = vm.uploadImage(it)
                if (url != null) imageUrl = url
            }
        }
    }
    
    // Video picker
    val videoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            scope.launch {
                val url = vm.uploadVideo(it)
                if (url != null) videoUrl = url
            }
        }
    }
    
    val iconOptions = listOf(
        "Info", "Phone", "Verified", "Link", "Spellcheck", "Warning", "Call", "Lock",
        "PersonOff", "Key", "Security", "Update", "VerifiedUser", "Email", "Schedule",
        "AttachFile", "CreditCard", "WifiOff", "Undo", "AccountBalance", "PhoneDisabled",
        "Visibility", "Search", "Block", "VideoCall", "RecordVoiceOver", "TextFields",
        "Image", "Psychology", "Wifi", "VpnKey", "Computer", "VisibilityOff", "Share",
        "NetworkCheck", "Settings", "Monitor", "Router", "PhoneAndroid", "Backup",
        "Devices", "Sms", "Notifications", "Report", "QuestionMark"
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val newTip = TipData(
                        title = title,
                        description = description,
                        iconName = iconName,
                        isImportant = isImportant,
                        detailedContent = detailedContent,
                        actionSteps = actionSteps.toMutableList(),
                        imageUrl = imageUrl,
                        videoUrl = videoUrl
                    )
                    onSave(newTip)
                },
                enabled = title.isNotBlank() && description.isNotBlank()
            ) {
                Text("Save", color = Color(0xFF4CAF50))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        },
        title = { Text(if (existingTip != null) "Edit Tip" else "Add Tip", color = Color.White) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Tip Title") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF4CAF50),
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = Color(0xFF4CAF50),
                        unfocusedLabelColor = Color.Gray
                    )
                )
                
                Spacer(Modifier.height(8.dp))
                
                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Short Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF4CAF50),
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = Color(0xFF4CAF50),
                        unfocusedLabelColor = Color.Gray
                    )
                )
                
                Spacer(Modifier.height(8.dp))
                
                // Detailed Content
                OutlinedTextField(
                    value = detailedContent,
                    onValueChange = { detailedContent = it },
                    label = { Text("Detailed Content") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF4CAF50),
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = Color(0xFF4CAF50),
                        unfocusedLabelColor = Color.Gray
                    )
                )
                
                Spacer(Modifier.height(8.dp))
                
                // Icon Selector
                Text("Icon", color = Color.White, fontSize = 12.sp)
                Spacer(Modifier.height(4.dp))
                var showIconPicker by remember { mutableStateOf(false) }
                OutlinedButton(
                    onClick = { showIconPicker = !showIconPicker },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(iconName, color = Color.White)
                }
                if (showIconPicker) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 150.dp)
                            .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                    ) {
                        itemsIndexed(iconOptions) { _, icon ->
                            TextButton(
                                onClick = {
                                    iconName = icon
                                    showIconPicker = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(icon, color = if (iconName == icon) Color(0xFF4CAF50) else Color.White)
                            }
                        }
                    }
                }
                
                Spacer(Modifier.height(8.dp))
                
                // Important Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Mark as Important", color = Color.White, fontSize = 14.sp)
                    Switch(
                        checked = isImportant,
                        onCheckedChange = { isImportant = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF4CAF50),
                            checkedTrackColor = Color(0xFF66BB6A)
                        )
                    )
                }
                
                Spacer(Modifier.height(8.dp))
                
                // Action Steps
                Text("Action Steps", color = Color.White, fontSize = 14.sp)
                Spacer(Modifier.height(4.dp))
                
                actionSteps.forEachIndexed { index, step ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${index + 1}.", color = Color(0xFF4CAF50), modifier = Modifier.width(24.dp))
                        Text(step, color = Color.White, modifier = Modifier.weight(1f), fontSize = 12.sp)
                        IconButton(onClick = { actionSteps = actionSteps.filterIndexed { i, _ -> i != index } }) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color.Red, modifier = Modifier.size(16.dp))
                        }
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newActionStep,
                        onValueChange = { newActionStep = it },
                        label = { Text("Add step") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF4CAF50),
                            unfocusedBorderColor = Color.Gray,
                            focusedLabelColor = Color(0xFF4CAF50),
                            unfocusedLabelColor = Color.Gray
                        )
                    )
                    IconButton(
                        onClick = {
                            if (newActionStep.isNotBlank()) {
                                actionSteps = actionSteps + newActionStep
                                newActionStep = ""
                            }
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add", tint = Color(0xFF4CAF50))
                    }
                }
                
                Spacer(Modifier.height(8.dp))
                
                // Image Upload
                Text("Image", color = Color.White, fontSize = 14.sp)
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { imagePicker.launch("image/*") },
                        modifier = Modifier.weight(1f),
                        enabled = !uiState.isUploading
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text(if (imageUrl == null) "Upload Image" else "Change Image")
                    }
                    if (imageUrl != null) {
                        IconButton(onClick = { imageUrl = null }) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color.Red)
                        }
                    }
                }
                if (imageUrl != null) {
                    Text("✓ Image uploaded", color = Color(0xFF4CAF50), fontSize = 12.sp)
                }
                
                Spacer(Modifier.height(8.dp))
                
                // Video Upload
                Text("Video", color = Color.White, fontSize = 14.sp)
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { videoPicker.launch("video/*") },
                        modifier = Modifier.weight(1f),
                        enabled = !uiState.isUploading
                    ) {
                        Icon(Icons.Default.VideoLibrary, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text(if (videoUrl == null) "Upload Video" else "Change Video")
                    }
                    if (videoUrl != null) {
                        IconButton(onClick = { videoUrl = null }) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color.Red)
                        }
                    }
                }
                if (videoUrl != null) {
                    Text("✓ Video uploaded", color = Color(0xFF4CAF50), fontSize = 12.sp)
                }
                
                if (uiState.isUploading) {
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF4CAF50)
                    )
                    Text("Uploading...", color = Color.White, fontSize = 12.sp)
                }
            }
        },
        containerColor = Color(0xFF1E1E1E),
        modifier = Modifier.fillMaxWidth()
    )
}

