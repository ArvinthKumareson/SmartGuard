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
    val keywords by vm.keywords.collectAsState()

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Add Scam Keyword", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        TextField(
            value = newKeyword,
            onValueChange = { newKeyword = it },
            label = { Text("Keyword") }
        )

        Button(onClick = {
            vm.addKeyword(newKeyword.trim())
            newKeyword = ""
        }) {
            Text("Add")
        }

        Spacer(Modifier.height(24.dp))
        Text("Existing Keywords", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        LazyColumn {
            items(keywords, key = { it.first }) { (id, value) ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(value, style = MaterialTheme.typography.bodyLarge)
                        IconButton(onClick = { vm.deleteKeyword(id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                }
            }
        }
    }
}
