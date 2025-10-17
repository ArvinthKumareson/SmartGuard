@file:OptIn(ExperimentalMaterial3Api::class)

package com.smartguard.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController




private val tips = listOf(
    "Never tap links from unknown senders. Go directly to the official app/site.",
    "Urgency is a red flag: 'your account will be closed in 24h' → pause & verify.",
    "Never share OTPs or 2FA codes with anyone, even 'support'.",
    "Check sender details carefully. Spoofed names are common.",
    "For deliveries, use the official courier app to track—not SMS links."
)

@Composable
fun TipsScreen(nav: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Anti‑Scam Advisory Tips")}

            )
        },
        bottomBar = { SmartGuardBottomBar(nav, currentRoute = "tips") }

    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                "Watch this short guidance and review the tips below.",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(Modifier.height(16.dp))

            tips.forEachIndexed { i, tip ->
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Tip ${i + 1}", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        Text(tip, style = MaterialTheme.typography.bodyMedium)
                    }

                }
            }
        }
    }
}
