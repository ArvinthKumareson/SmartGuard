package com.smartguard.app.mainapp.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.smartguard.app.R
import com.smartguard.app.mainapp.common.BackgroundWrapper
import com.smartguard.app.viewmodel.KeywordViewModel

/**
 * Admin screen for managing scam detection keywords.
 *
 * Allows admins to add new keywords with explanations, search existing ones,
 * and delete them; the underlying data is stored in the Firestore
 * "keywords" collection and feeds the detection engine.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminKeywordManagerScreen(nav: NavController, vm: KeywordViewModel = viewModel()) {
    // Input fields for new keyword and explanation
    var newKeyword by remember { mutableStateOf("") }
    var newExplanation by remember { mutableStateOf("") }
    // Search query to filter keywords
    var searchQuery by remember { mutableStateOf("") }
    val keywords by vm.keywords.collectAsState()
    
    // Filter keywords by search query - searches both keyword text and explanation
    val filteredKeywords = remember(keywords, searchQuery) {
        if (searchQuery.isEmpty()) {
            keywords
        } else {
            keywords.filter { keywordData ->
                // Match if search text appears in keyword or explanation
                keywordData.value.contains(searchQuery, ignoreCase = true) ||
                keywordData.explanation.contains(searchQuery, ignoreCase = true)
            }
        }
    }
    BackgroundWrapper(imageResId = R.drawable.bg_profile) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Manage Keywords", color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = { nav.popBackStack() }) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E1E1E))
                )
            },
            containerColor = Color(0x00000000), // Transparent
            contentColor = Color.White
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)   // ✅ important
                    .padding(16.dp)
                    .fillMaxSize()      // ✅ recommended
            ) {
                Text("Add Scam Keyword", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))

                TextField(
                    value = newKeyword,
                    onValueChange = { newKeyword = it },
                    label = { Text("Keyword") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                TextField(
                    value = newExplanation,
                    onValueChange = { newExplanation = it },
                    label = { Text("Why is this a scam indicator?") },
                    placeholder = { Text("e.g., Scammers use this to create urgency") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = {
                        vm.addKeyword(newKeyword.trim(), newExplanation.trim())
                        newKeyword = ""
                        newExplanation = ""
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add Keyword")
                }

                Spacer(Modifier.height(24.dp))
                Text("Existing Keywords", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search keywords...", color = Color.Gray) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray)
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
                    singleLine = true
                )

                Spacer(Modifier.height(8.dp))
                Text(
                    "Found ${filteredKeywords.size} keyword(s)",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
                Spacer(Modifier.height(8.dp))

                LazyColumn {
                    items(filteredKeywords, key = { it.id }) { keywordData ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF2D2D2D) // Dark custom card color
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        keywordData.value,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        keywordData.explanation,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.LightGray
                                    )
                                }
                                IconButton(onClick = { vm.deleteKeyword(keywordData.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
