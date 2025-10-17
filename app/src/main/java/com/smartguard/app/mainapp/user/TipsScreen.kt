@file:OptIn(ExperimentalMaterial3Api::class)

package com.smartguard.app.mainapp.user

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.smartguard.app.mainapp.SmartGuardBottomBar


private val tips = listOf(
    "Never tap links from unknown senders. Go directly to the official app/site.",
    "Urgency is a red flag: 'your account will be closed in 24h' → pause & verify.",
    "Never share OTPs or 2FA codes with anyone, even 'support'.",
    "Check sender details carefully. Spoofed names are common.",
    "For deliveries, use the official courier app to track—not SMS links."
)

@Composable
fun TipsScreen(nav: NavController) {
    val expandedTipIndex = remember { mutableStateOf(-1) }
    val bookmarkedTips = remember { mutableStateListOf<Int>() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Anti‑Scam Advisory Tips", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E1E1E))
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
                "Tap each tip to explore examples and test your awareness.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )
            Spacer(Modifier.height(16.dp))

            tips.forEachIndexed { i, tip ->
                val isExpanded = expandedTipIndex.value == i
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clickable {
                            expandedTipIndex.value = if (isExpanded) -1 else i
                        },
                    colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFF1E1E1E))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Tip ${i + 1}", style = MaterialTheme.typography.titleMedium, color = Color.White)
                            IconButton(onClick = {
                                if (bookmarkedTips.contains(i)) bookmarkedTips.remove(i)
                                else bookmarkedTips.add(i)
                            }) {
                                Icon(
                                    imageVector = if (bookmarkedTips.contains(i)) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Bookmark",
                                    tint = Color.White
                                )
                            }
                        }

                        Spacer(Modifier.height(8.dp))
                        Text(tip, style = MaterialTheme.typography.bodyMedium, color = Color.White)

                        if (isExpanded) {
                            Spacer(Modifier.height(12.dp))
                            Text("Example:", style = MaterialTheme.typography.labelMedium, color = Color.LightGray)
                            Text(
                                when (i) {
                                    0 -> "You receive an SMS from 'BankX' asking to verify your account via a link. The URL is suspicious and not from bankx.com."
                                    1 -> "A message says 'Your Netflix account will be suspended in 24h unless you verify now.' It’s designed to panic you."
                                    2 -> "Someone posing as 'support' asks for your OTP to 'fix your account'. Legitimate support will never ask for this."
                                    3 -> "The sender name is 'Apple Support', but the email is from 'support@apple.fake-domain.com'."
                                    4 -> "You get a delivery SMS with a link to reschedule. But the courier’s official app shows no pending delivery."
                                    else -> ""
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.LightGray
                            )

                            Spacer(Modifier.height(12.dp))
                            Text("Quick Check:", style = MaterialTheme.typography.labelMedium, color = Color.LightGray)
                            Text(
                                when (i) {
                                    0 -> "Would you tap the link if it looked like 'bankx-verification.com'?"
                                    1 -> "Is urgency alone enough to trust a message?"
                                    2 -> "Should you ever share your OTP with anyone?"
                                    3 -> "Can sender names be faked in emails?"
                                    4 -> "Is it safer to use the courier’s app than an SMS link?"
                                    else -> ""
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

