package com.smartguard.app.mainapp.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.smartguard.app.viewmodel.KeywordViewModel

@Composable
fun AdminKeywordManagerScreen(nav: NavController, vm: KeywordViewModel = viewModel()) {
    var newKeyword by remember { mutableStateOf("") }
    var newExplanation by remember { mutableStateOf("") }
    val keywords by vm.keywords.collectAsState()

    Column(modifier = Modifier.padding(16.dp)) {
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

        LazyColumn {
            items(keywords, key = { it.id }) { keywordData ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                keywordData.value,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                keywordData.explanation,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { vm.deleteKeyword(keywordData.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                }
            }
        }
    }
}
