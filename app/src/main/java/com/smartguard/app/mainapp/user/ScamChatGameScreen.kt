@file:OptIn(ExperimentalMaterial3Api::class)

package com.smartguard.app.mainapp.user

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.smartguard.app.mainapp.SmartGuardBottomBar
import kotlinx.coroutines.delay

data class ScamScenario(
    val id: String,
    val title: String,
    val steps: List<ScamStep>
)

data class ScamStep(
    val messages: List<String>,
    val choices: List<String>,
    val correctIndex: Int,
    val pushMessages: List<String> = emptyList(),
    val nextStepIfCorrect: Int? = null,
    val nextStepIfWrong: Int? = null,
    val endsGameIfWrong: Boolean = false
)

val allScenarios = listOf(
    ScamScenario(
        id = "credit_card",
        title = "Credit Card Scenario",
        steps = listOf(
            ScamStep(
                messages = listOf("Hi, this is the Fraud Department.", "We detected a charge of RM3,200 in Johor.", "Was this you?"),
                choices = listOf("No, I didn’t make that charge", "Yes, that was me"),
                correctIndex = 0,
                pushMessages = listOf("We need to act fast.", "Please confirm your card number."),
                nextStepIfCorrect = 1,
                nextStepIfWrong = 2
            ),
            ScamStep(
                messages = listOf("Please confirm your card number ending in 4321."),
                choices = listOf("Sure, it’s 4321‑5678‑9012‑3456", "I’ll call the bank directly"),
                correctIndex = 1,
                pushMessages = listOf("Calling takes time.", "You might lose more money."),
                nextStepIfCorrect = 3,
                nextStepIfWrong = 2
            ),
            ScamStep(
                messages = listOf("To block the fraud, we need your CVV."),
                choices = listOf("It’s 123", "I won’t share that"),
                correctIndex = 1,
                endsGameIfWrong = true
            ),
            ScamStep(
                messages = listOf("We’ve sent a verification code.", "Please read it back."),
                choices = listOf("Here’s the code: 847392", "I won’t share OTPs"),
                correctIndex = 1,
                endsGameIfWrong = true
            )
        )
    )
)

@Composable
fun ScamChatGameScreen(nav: NavController) {
    var selectedScenario by remember { mutableStateOf<ScamScenario?>(null) }
    var stepIndex by remember { mutableStateOf(0) }
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    var scamPoints by remember { mutableStateOf(0) }
    var gameOver by remember { mutableStateOf(false) }
    var gameWon by remember { mutableStateOf(false) }
    var showChoices by remember { mutableStateOf(false) }
    var messageDelivered by remember { mutableStateOf(false) }

    val chatHistory = remember { mutableStateListOf<Pair<String, Boolean>>() }
    val userChoices = remember { mutableStateListOf<Int?>() }
    val shownSteps = remember { mutableStateListOf<Int>() }

    val currentStep = selectedScenario?.steps?.getOrNull(stepIndex)

    LaunchedEffect(stepIndex, selectedScenario) {
        if (!shownSteps.contains(stepIndex)) {
            shownSteps.add(stepIndex)
        }
        showChoices = false
        messageDelivered = false
        currentStep?.messages?.forEach {
            delay(800L)
            chatHistory.add(it to false)
        }
        messageDelivered = true
        showChoices = true
    }

    LaunchedEffect(selectedIndex) {
        if (selectedIndex != null && currentStep != null) {
            val choiceText = currentStep.choices[selectedIndex!!]
            chatHistory.add(choiceText to true)
            showChoices = false
            messageDelivered = false
            delay(1000L)

            val isCorrect = selectedIndex == currentStep.correctIndex
            if (isCorrect && currentStep.pushMessages.isNotEmpty()) {
                currentStep.pushMessages.forEach {
                    delay(800L)
                    chatHistory.add(it to false)
                }
            }

            while (userChoices.size <= stepIndex) userChoices.add(null)
            userChoices[stepIndex] = selectedIndex

            if (!isCorrect) {
                scamPoints++
                if (currentStep.endsGameIfWrong) {
                    gameOver = true
                    return@LaunchedEffect
                }
                stepIndex = currentStep.nextStepIfWrong ?: (stepIndex + 1)
            } else {
                stepIndex = currentStep.nextStepIfCorrect ?: (stepIndex + 1)
                if (stepIndex >= selectedScenario?.steps?.size ?: 0) {
                    gameWon = true
                }
            }
            selectedIndex = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Chat Scenario Simulation") })
        },
        bottomBar = { SmartGuardBottomBar(nav, currentRoute = "tips") }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (selectedScenario == null) {
                Column(Modifier.padding(16.dp)) {
                    Text("Choose a scam scenario:", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(12.dp))
                    allScenarios.forEach { scenario ->
                        Button(onClick = {
                            selectedScenario = scenario
                            stepIndex = 0
                            scamPoints = 0
                            gameOver = false
                            gameWon = false
                            selectedIndex = null
                            showChoices = false
                            messageDelivered = false
                            chatHistory.clear()
                            userChoices.clear()
                            shownSteps.clear()
                        }, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                            Text(scenario.title)
                        }
                    }
                }
            } else if (gameOver || gameWon) {
                Column(Modifier.padding(16.dp)) {
                    val title = if (gameOver) "You’ve been scammed!" else "You survived the scam attempt!"
                    Text(title, style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(16.dp))
                    Text("Summary of your responses:", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(12.dp))

                    shownSteps.forEach { i ->
                        val step = selectedScenario?.steps?.getOrNull(i) ?: return@forEach
                        val choice = userChoices.getOrNull(i)
                        val isSafe = choice == step.correctIndex
                        val choiceText = choice?.let { step.choices.getOrNull(it) } ?: "No response"
                        val allMessages = step.messages.joinToString(" ")

                        Text("Step ${i + 1}: ${if (isSafe) "Safe" else "Risky"}", style = MaterialTheme.typography.titleSmall)
                        Text("Scammer said: $allMessages", style = MaterialTheme.typography.bodySmall)
                        Text("You chose: $choiceText", style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(12.dp))
                    }

                    Spacer(Modifier.height(16.dp))
                    Button(onClick = {
                        selectedScenario = null
                        stepIndex = 0
                        scamPoints = 0
                        gameOver = false
                        gameWon = false
                        selectedIndex = null
                        showChoices = false
                        messageDelivered = false
                        chatHistory.clear()
                        userChoices.clear()
                        shownSteps.clear()
                    }) {
                        Text("Try Another Scenario")
                    }
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { nav.navigate("home") }) {
                        Text("Back to Home")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    reverseLayout = false
                ) {
                    items(chatHistory) { (text, isUser) ->
                        ChatBubble(text = text, isUser = isUser)
                    }
                }

                if (showChoices && messageDelivered && currentStep != null) {
                    Column(Modifier.padding(16.dp)) {
                        currentStep.choices.forEachIndexed { i, choice ->
                            Button(
                                onClick = {
                                    if (selectedIndex == null) {
                                        selectedIndex = i
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Text(choice)
                            }
                        }

                        if (selectedIndex == null) {
                            Button(
                                onClick = {
                                    while (userChoices.size <= stepIndex) userChoices.add(null)
                                    stepIndex = currentStep.nextStepIfWrong ?: (stepIndex + 1)
                                    if (stepIndex >= selectedScenario?.steps?.size ?: 0) {
                                        gameWon = true
                                    }
                                    showChoices = false
                                    messageDelivered = false
                                },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)

                            ) {
                                Text("Continue without responding")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(text: String, isUser: Boolean) {
    val bubbleColor = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val alignment = if (isUser) Arrangement.End else Arrangement.Start
    val textColor = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = alignment
    ) {
        Surface(
            color = bubbleColor,
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 2.dp
        ) {
            Text(
                text = text,
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = textColor
            )
        }
    }
}
