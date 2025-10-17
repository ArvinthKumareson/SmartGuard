package com.smartguard.app.mainapp.resources


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.smartguard.app.model.ScamCourse

@Composable
fun CourseCard(course: ScamCourse, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(220.dp)
            .height(140.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(course.title, style = MaterialTheme.typography.titleMedium, color = Color.White)
            Spacer(Modifier.height(4.dp))
            Text("⭐ ${course.rating}", color = Color.LightGray)
            if (course.isNew) {
                Text("New", color = Color(0xFF6EDE5B), style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
