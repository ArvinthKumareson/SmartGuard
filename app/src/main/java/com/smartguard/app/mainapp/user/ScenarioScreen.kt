@file:OptIn(ExperimentalMaterial3Api::class)

package com.smartguard.app.mainapp.user

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

data class Scenario(
    val prompt: String,
    val options: List<Pair<String, String>>,
    val correct: Int
)

private val scenarios = listOf(
    Scenario(
        "A 'bank' agent asks you to install a remote-access app to fix an issue.",
        listOf(
            "Install the app and follow instructions" to "❌This gives attackers control of your phone.",
            "Visit branch or call official hotline" to "✅ Correct: verify via official channels.",
            "Share card details over chat" to "❌ Never share card details in chat."
        ),
        1
    ),
    Scenario(
        "You receive an investment tip promising guaranteed profits.",
        listOf(
            "Send crypto to join early" to "❌ High risk of scam.",
            "Ask for license & check regulator" to "✅ Correct: verify the firm is licensed.",
            "Invite friends to earn more" to "❌ Ponzi-like behaviour."
        ),
        1
    )
)

@Composable
fun ScenarioScreen(nav: NavController) {
    var index by remember { mutableStateOf(0) }
    var selected by remember { mutableStateOf<Int?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Scenario Test") })
        }
    ) { padding ->
        val scenario = scenarios[index]
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                "Scenario ${index + 1} of ${scenarios.size}",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(8.dp))
            Text(scenario.prompt, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(16.dp))

            scenario.options.forEachIndexed { i, (optionText, explanation) ->
                val isChosen = selected == i
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    onClick = { selected = i }
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(optionText, style = MaterialTheme.typography.bodyMedium)
                        if (isChosen) {
                            Spacer(Modifier.height(8.dp))
                            Text(explanation, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        if (index > 0) {
                            index--
                            selected = null
                        }
                    }
                ) {
                    Text("Back")
                }

                Button(
                    onClick = {
                        if (index < scenarios.lastIndex) {
                            index++
                            selected = null
                        }
                    }
                ) {
                    Text("Next")
                }
            }
        }
    }
}
