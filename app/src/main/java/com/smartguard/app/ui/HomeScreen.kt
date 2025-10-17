@file:OptIn(ExperimentalMaterial3Api::class)

package com.smartguard.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.smartguard.app.viewmodel.AuthViewModel
import com.smartguard.app.ui.common.BackgroundWrapper
import com.smartguard.app.R

@Composable
fun HomeScreen(nav: NavController, vm: AuthViewModel) {
    var selectedTab by remember { mutableStateOf("Today") }

    BackgroundWrapper(imageResId = R.drawable.bg_profile) {
    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {BottomNavigationBar(nav) }
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
                    vm.logout()
                    nav.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }

                }
            )

            TabSelector(selectedTab) { selectedTab = it }

            Spacer(Modifier.height(16.dp))

            FeatureCard("Anti‑Scam Advisory Tips", "Learn how to spot scams") { nav.navigate("tips") }
            FeatureCard("Play a Scenario Based Game", "Experience real scam simulations") { nav.navigate("scam chat") }
            FeatureCard("Take a Scam Quiz", "Test your scam awareness") { nav.navigate("quiz") }
            FeatureCard("Test Yourself in Scenarios", "Challenge your instincts") { nav.navigate("scenarios") }
            FeatureCard("View Potential Scam Messages", "See what SmartGuard has detected") { nav.navigate("history") }
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
        Text(
            text = "Hello $userName",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White
        )
        Spacer(Modifier.weight(1f))
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "Profile",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .clickable { onProfileClick() },
            tint = Color.White
        )
    }
}

@Composable
fun TabSelector(selected: String, onTabSelected: (String) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        listOf("Today", "Learning Plan").forEach { tab ->
            val isSelected = tab == selected
            Text(
                text = tab,
                modifier = Modifier
                    .padding(end = 16.dp)
                    .clickable { onTabSelected(tab) },
                color = if (isSelected) Color.White else Color.Gray,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun FeatureCard(title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = Color.White)
            Spacer(Modifier.height(4.dp))
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}


@Composable
fun BottomNavigationBar(nav: NavController) {
    NavigationBar(containerColor = Color(0xFF1E1E1E)) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            selected = true,
            onClick = {}
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Search, contentDescription = null) },
            selected = false,
            onClick = {}
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, contentDescription = null) },
            selected = false,
            onClick = { nav.navigate("profile") }
        )
    }
}

