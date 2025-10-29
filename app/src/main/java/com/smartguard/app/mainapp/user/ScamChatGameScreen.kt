@file:OptIn(ExperimentalMaterial3Api::class)

package com.smartguard.app.mainapp.user

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.smartguard.app.R
import com.smartguard.app.mainapp.common.BackgroundWrapper
import com.smartguard.app.mainapp.resources.SmartGuardBottomBar
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay

data class ScamScenario(
    val id: String,
    val title: String,
    val description: String,
    val steps: List<ScamStep>
)

data class ScamStep(
    val messages: List<String>,
    val choices: List<String>,
    val correctIndex: Int,
    val explanation: String = "",
    val nextStepIfCorrect: Int? = null,
    val nextStepIfWrong: Int? = null,
    val endsGameIfCorrect: Boolean = false,
    val endsGameIfWrong: Boolean = false,
    val isCrucialMistake: Boolean = false
)

val allScenarios = listOf(
    // SCENARIO 1: Bank Fraud Call - Realistic Maybank scam
    ScamScenario(
        id = "bank_fraud",
        title = "Bank Fraud Alert",
        description = "Scammer pretends to be from Maybank",
        steps = listOf(
            ScamStep(
                messages = listOf(
                    "Good afternoon. This is Ahmad from Maybank Fraud Prevention Unit.",
                    "We're calling because we detected a suspicious transaction of RM4,850 to an online gambling site.",
                    "This happened 15 minutes ago. Did you authorize this payment?"
                ),
                choices = listOf(
                    "No! That wasn't me! Please block it!",
                    "Let me check my app first",
                    "Can you give me a reference number? I'll call the bank directly"
                ),
                correctIndex = 2,
                explanation = "Real banks have reference numbers for all calls. Always verify by calling the official number yourself.",
                nextStepIfCorrect = -1,
                nextStepIfWrong = 1,
                endsGameIfCorrect = true
            ),
            ScamStep(
                messages = listOf(
                    "I understand your concern, sir. Time is critical here.",
                    "The transaction is still pending - we have a 30-minute window to stop it.",
                    "I just need to verify your identity. What are the last 6 digits of your card?"
                ),
                choices = listOf(
                    "It's 456789",
                    "Why do you need that? You called me, you should already know",
                    "I'm hanging up and calling 1300-88-6688 myself"
                ),
                correctIndex = 2,
                explanation = "Banks NEVER ask for card details during outbound calls. They already have your info!",
                nextStepIfCorrect = -1,
                nextStepIfWrong = 2,
                endsGameIfCorrect = true,
                isCrucialMistake = true
            ),
            ScamStep(
                messages = listOf(
                    "Sir, I can assure you this is legitimate.",
                    "Look, I'll send you a verification code to your phone right now as proof.",
                    "Just read back the 6-digit code when you receive it, okay?"
                ),
                choices = listOf(
                    "Okay, the code is 847293",
                    "No, I'll verify this myself. Goodbye.",
                    "Send it to my email instead"
                ),
                correctIndex = 1,
                explanation = "NEVER share OTP codes! That SMS might be the scammer trying to access your account!",
                nextStepIfCorrect = -1,
                nextStepIfWrong = -1,
                endsGameIfCorrect = true,
                endsGameIfWrong = true,
                isCrucialMistake = true
            )
        )
    ),

    // SCENARIO 2: Parcel Delivery Scam
    ScamScenario(
        id = "delivery_scam",
        title = "Failed Delivery Notice",
        description = "Fake courier message about a package",
        steps = listOf(
            ScamStep(
                messages = listOf(
                    "📱 SMS: PosLaju - Parcel delivery failed. Package is held at depot.",
                    "Click to reschedule: bit.ly/poslaju-reschedule-9f2k",
                    "Delivery fee: RM6.50. Valid for 48 hours only."
                ),
                choices = listOf(
                    "Click the link to reschedule",
                    "Ignore it - I'm not expecting anything",
                    "Go to the official PosLaju website to track"
                ),
                correctIndex = 2,
                explanation = "Shortened links (bit.ly, tinyurl) in SMS are RED FLAGS. Always use official websites!",
                nextStepIfCorrect = -1,
                nextStepIfWrong = 1,
                endsGameIfCorrect = true
            ),
            ScamStep(
                messages = listOf(
                    "🌐 [Website loaded]",
                    "PosLaju - Delivery Rescheduling",
                    "Enter your details:",
                    "• Full Name:",
                    "• IC Number:",
                    "• Phone Number:",
                    "• Pay RM6.50 delivery fee via online banking"
                ),
                choices = listOf(
                    "Fill in all details to get my package",
                    "This looks suspicious - real PosLaju doesn't ask for IC",
                    "Enter fake information to test it"
                ),
                correctIndex = 1,
                explanation = "Couriers NEVER ask for IC numbers for redelivery. This is identity theft!",
                nextStepIfCorrect = -1,
                nextStepIfWrong = 2,
                endsGameIfCorrect = true
            ),
            ScamStep(
                messages = listOf(
                    "Payment Required: RM6.50",
                    "Login to your online banking:",
                    "Username: _______",
                    "Password: _______",
                    "Note: Secure connection verified ✓"
                ),
                choices = listOf(
                    "Enter my banking credentials",
                    "Close this immediately - this is a phishing site!",
                    "Use credit card instead"
                ),
                correctIndex = 1,
                explanation = "MAJOR RED FLAG! No legitimate courier asks you to LOGIN to banking on their site!",
                nextStepIfCorrect = -1,
                nextStepIfWrong = -1,
                endsGameIfCorrect = true,
                endsGameIfWrong = true,
                isCrucialMistake = true
            )
        )
    ),

    // SCENARIO 3: Investment Scam
    ScamScenario(
        id = "investment_scam",
        title = "Get Rich Quick Scheme",
        description = "Too-good-to-be-true investment offer",
        steps = listOf(
            ScamStep(
                messages = listOf(
                    "Hi! My name is Daniel, I'm a financial advisor with MQ Global Investments.",
                    "Congratulations! Your name was selected from our premium client list.",
                    "We're offering an EXCLUSIVE opportunity: Invest RM5,000 today, get RM25,000 in 60 days.",
                    "This is a limited time offer - only 10 spots left!"
                ),
                choices = listOf(
                    "Wow! Tell me more! How do I join?",
                    "This sounds too good to be true. Is this registered with SC?",
                    "Not interested. Please remove me from your list."
                ),
                correctIndex = 1,
                explanation = "500% returns in 60 days? Classic scam! Check if investments are registered with Securities Commission.",
                nextStepIfCorrect = 1,
                nextStepIfWrong = 2
            ),
            ScamStep(
                messages = listOf(
                    "Of course we're registered! Let me send you our 'registration certificate'.",
                    "We have over 50,000 successful investors in Malaysia.",
                    "Look, here's testimonial from Dato' Rahman who made RM2 million with us!",
                    "Don't miss out - this offer ends tonight!"
                ),
                choices = listOf(
                    "Okay, that looks legit. Where do I sign up?",
                    "I'll verify your company on SC Malaysia website first",
                    "Can I start with RM1,000 to test?"
                ),
                correctIndex = 1,
                explanation = "Verify ALL investments at www.sc.com.my/investor-alert. Fake certificates are easy to create!",
                nextStepIfCorrect = -1,
                nextStepIfWrong = 3,
                endsGameIfCorrect = true
            ),
            ScamStep(
                messages = listOf(
                    "I understand you're excited! Smart move!",
                    "Our company operates through special private channels to maximize returns.",
                    "That's why we might not be in the public SC database yet.",
                    "Just transfer RM5,000 to this account to secure your spot:",
                    "Maybank: 162847593012 (Ahmad Bin Abdullah)"
                ),
                choices = listOf(
                    "Transfer RM5,000 now",
                    "Ask for official company registration number",
                    "This is clearly a scam. Report and block."
                ),
                correctIndex = 2,
                explanation = "Personal account name instead of company name = SCAM! Plus urgency + unverifiable = Run away!",
                nextStepIfCorrect = -1,
                nextStepIfWrong = -1,
                endsGameIfCorrect = true,
                endsGameIfWrong = true,
                isCrucialMistake = true
            ),
            ScamStep(
                messages = listOf(
                    "Great! You won't regret this.",
                    "After you transfer, send me the receipt and your IC copy for verification.",
                    "We'll activate your investment account within 24 hours!",
                    "Welcome to financial freedom! 🚀💰"
                ),
                choices = listOf(
                    "Send money and IC copy",
                    "Why do you need my IC? This feels wrong.",
                    "Block contact and report to police"
                ),
                correctIndex = 2,
                explanation = "Red flags everywhere: personal account, IC copy request, unrealistic returns, pressure tactics!",
                nextStepIfCorrect = -1,
                nextStepIfWrong = -1,
                endsGameIfCorrect = true,
                endsGameIfWrong = true,
                isCrucialMistake = true
            )
        )
    ),

    // SCENARIO 4: Tech Support Scam
    ScamScenario(
        id = "tech_support",
        title = "Microsoft Virus Alert",
        description = "Fake tech support claiming your PC is infected",
        steps = listOf(
            ScamStep(
                messages = listOf(
                    "🚨 CRITICAL SECURITY ALERT 🚨",
                    "Your computer has been infected with Zeus Trojan Virus!",
                    "Your personal data, passwords and banking information are at risk!",
                    "Call Microsoft Support IMMEDIATELY: 1-800-123-4567",
                    "[OK] [Call Now]"
                ),
                choices = listOf(
                    "Click 'Call Now' - this looks serious!",
                    "Close this pop-up and run my antivirus",
                    "Call the number to fix my computer"
                ),
                correctIndex = 1,
                explanation = "Microsoft NEVER displays pop-ups with phone numbers! This is a classic tech support scam.",
                nextStepIfCorrect = -1,
                nextStepIfWrong = 1,
                endsGameIfCorrect = true
            ),
            ScamStep(
                messages = listOf(
                    "📞 Hello, this is Microsoft Security Center, my name is Steve.",
                    "I can see your computer is broadcasting an infected signal.",
                    "We detected 47 viruses and 3 Trojan horses on your system.",
                    "If we don't act now, hackers could steal your bank details."
                ),
                choices = listOf(
                    "Please help me fix it!",
                    "How did you 'detect' my computer?",
                    "Hang up - Microsoft doesn't cold call users"
                ),
                correctIndex = 2,
                explanation = "Microsoft NEVER makes unsolicited calls about viruses. This is 100% a scam!",
                nextStepIfCorrect = -1,
                nextStepIfWrong = 2,
                endsGameIfCorrect = true
            ),
            ScamStep(
                messages = listOf(
                    "Sir, I need remote access to your computer to remove the viruses.",
                    "Please download TeamViewer and give me the access code.",
                    "This is free service from Microsoft - no cost to you.",
                    "What's the 9-digit code on your screen?"
                ),
                choices = listOf(
                    "Give them the TeamViewer code",
                    "Ask why Microsoft needs TeamViewer - they have Windows Update",
                    "Refuse and disconnect immediately"
                ),
                correctIndex = 2,
                explanation = "Remote access = full control of your computer! They'll install malware or steal your files!",
                nextStepIfCorrect = -1,
                nextStepIfWrong = -1,
                endsGameIfCorrect = true,
                endsGameIfWrong = true,
                isCrucialMistake = true
            )
        )
    ),

    // SCENARIO 5: Romance/Love Scam
    ScamScenario(
        id = "romance_scam",
        title = "Online Romance Scam",
        description = "Love interest asks for money",
        steps = listOf(
            ScamStep(
                messages = listOf(
                    "Message from Jessica (matched 3 days ago):",
                    "Hi! I really enjoy talking to you 😊",
                    "I feel like we have such a strong connection already.",
                    "By the way, I'm a model based in London. Here's my Instagram!"
                ),
                choices = listOf(
                    "I feel the same way! You're so beautiful!",
                    "We just met 3 days ago - this feels rushed",
                    "Check if her photos appear on other accounts (reverse image search)"
                ),
                correctIndex = 2,
                explanation = "Scammers use stolen photos. Do a reverse Google image search to verify!",
                nextStepIfCorrect = 1,
                nextStepIfWrong = 2
            ),
            ScamStep(
                messages = listOf(
                    "You seem suspicious of me 😢",
                    "I thought we had something special...",
                    "Actually, I was planning to fly to Malaysia next month to meet you!",
                    "I already started looking at flight tickets. Would you like that? ❤️"
                ),
                choices = listOf(
                    "Yes! I can't wait to meet you!",
                    "Let's video call first - I want to see you're real",
                    "This is moving too fast. Let's slow down."
                ),
                correctIndex = 1,
                explanation = "Real people video call. Scammers make excuses: broken camera, shy, etc. Insist on video!",
                nextStepIfCorrect = 3,
                nextStepIfWrong = 4
            ),
            ScamStep(
                messages = listOf(
                    "I'm so excited to see you too! 😊",
                    "But something terrible happened...",
                    "My agency didn't pay me this month (some issue with contracts).",
                    "I can't afford the flight ticket. It's RM2,500.",
                    "Could you help me? I promise I'll pay you back when we meet! 🙏"
                ),
                choices = listOf(
                    "Of course! I'll transfer the money now!",
                    "Can you borrow from family or friends instead?",
                    "This is a scam. Block and report."
                ),
                correctIndex = 2,
                explanation = "CLASSIC SCAM PATTERN! They build emotional connection, then ask for money. NEVER send money to people you haven't met!",
                nextStepIfCorrect = -1,
                nextStepIfWrong = -1,
                endsGameIfCorrect = true,
                endsGameIfWrong = true,
                isCrucialMistake = true
            ),
            ScamStep(
                messages = listOf(
                    "Oh actually, my camera is broken right now!",
                    "But I can send you more photos! 📸",
                    "And we can voice call instead if you want?",
                    "By the way, about that flight ticket... it's on sale now, only RM2,200.",
                    "The sale ends tonight. Can you help me please? 🥺"
                ),
                choices = listOf(
                    "Okay, I'll send RM2,200",
                    "Broken camera + urgency + money request = SCAM",
                    "Let me buy the ticket directly for you then"
                ),
                correctIndex = 1,
                explanation = "All the red flags: no video call, urgency, asking for money. 100% a romance scam!",
                nextStepIfCorrect = -1,
                nextStepIfWrong = -1,
                endsGameIfCorrect = true,
                endsGameIfWrong = true,
                isCrucialMistake = true
            )
        )
    ),

    // SCENARIO 6: Job Scam
    ScamScenario(
        id = "job_scam",
        title = "Work From Home Job Offer",
        description = "Fake job offer that's actually a scam",
        steps = listOf(
            ScamStep(
                messages = listOf(
                    "📧 From: HR@amazon-recruitment.com",
                    "Subject: Congratulations! Job Offer - Data Entry Specialist",
                    "Dear Applicant,",
                    "Amazon is hiring! Earn RM3,500-RM8,000/month working from home.",
                    "Requirements: Laptop, internet, 2 hours per day.",
                    "Interested? WhatsApp +60123456789 now!"
                ),
                choices = listOf(
                    "This sounds perfect! WhatsApp them immediately",
                    "Check if this email is from real Amazon (@amazon.com)",
                    "Search for this job on official Amazon careers page"
                ),
                correctIndex = 2,
                explanation = "amazon-recruitment.com is FAKE! Real Amazon uses amazon.com. Verify on official career sites!",
                nextStepIfCorrect = -1,
                nextStepIfWrong = 1,
                endsGameIfCorrect = true
            ),
            ScamStep(
                messages = listOf(
                    "💬 WhatsApp from 'Amazon HR Manager':",
                    "Hi! Congrats on being selected! 🎉",
                    "We just need to process your registration.",
                    "Please pay RM350 for:",
                    "• Training materials",
                    "• Software license",
                    "• Background check",
                    "Transfer to: Maybank 1234567890 (Lisa Wong)"
                ),
                choices = listOf(
                    "Pay RM350 to secure the job",
                    "Real companies don't ask employees to pay for training",
                    "Negotiate to deduct from first salary"
                ),
                correctIndex = 1,
                explanation = "HUGE RED FLAG! Legitimate companies NEVER ask you to pay for a job. This is a scam!",
                nextStepIfCorrect = -1,
                nextStepIfWrong = 2,
                endsGameIfCorrect = true
            ),
            ScamStep(
                messages = listOf(
                    "The RM350 is just a deposit, you'll get it back after 3 months!",
                    "Plus, I need a copy of your IC front and back for HR records.",
                    "And your bank account number for salary deposit.",
                    "Send all documents after payment and we'll start you tomorrow!"
                ),
                choices = listOf(
                    "Send payment, IC copy, and bank details",
                    "This has too many red flags - report as scam",
                    "Ask for company registration number first"
                ),
                correctIndex = 1,
                explanation = "Payment + IC copy + bank details = IDENTITY THEFT SCAM! They'll use your info for illegal activities!",
                nextStepIfCorrect = -1,
                nextStepIfWrong = -1,
                endsGameIfCorrect = true,
                endsGameIfWrong = true,
                isCrucialMistake = true
            )
        )
    )
)

@Composable
fun ScamChatGameScreen(nav: NavController) {
    var selectedScenario by remember { mutableStateOf<ScamScenario?>(null) }
    var stepIndex by remember { mutableStateOf(0) }
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    var crucialMistakes by remember { mutableStateOf(0) }
    var totalMistakes by remember { mutableStateOf(0) }
    var gameOver by remember { mutableStateOf(false) }
    var gameWon by remember { mutableStateOf(false) }
    var showChoices by remember { mutableStateOf(false) }
    var messageDelivered by remember { mutableStateOf(false) }
    var showExplanation by remember { mutableStateOf(false) }
    var currentExplanation by remember { mutableStateOf("") }

    val chatHistory = remember { mutableStateListOf<Pair<String, Boolean>>() }
    val userChoices = remember { mutableStateListOf<Int?>() }
    val shownSteps = remember { mutableStateListOf<Int>() }
    val listState = rememberLazyListState()
    val currentStep = selectedScenario?.steps?.getOrNull(stepIndex)

    LaunchedEffect(chatHistory.size) {
        if (chatHistory.isNotEmpty()) {
            listState.animateScrollToItem(chatHistory.size - 1)
        }
    }

    LaunchedEffect(stepIndex, selectedScenario) {
        if (selectedScenario != null && !shownSteps.contains(stepIndex)) {
            shownSteps.add(stepIndex)
            showChoices = false
            messageDelivered = false
            showExplanation = false
            currentStep?.messages?.forEach {
                delay(800L)
                chatHistory.add(it to false)
            }
            messageDelivered = true
            showChoices = true
        }
    }

    LaunchedEffect(selectedIndex) {
        if (selectedIndex != null && currentStep != null) {
            val choiceText = currentStep.choices[selectedIndex!!]
            chatHistory.add(choiceText to true)
            showChoices = false
            messageDelivered = false
            delay(1000L)

            while (userChoices.size <= stepIndex) userChoices.add(null)
            userChoices[stepIndex] = selectedIndex

            val isCorrect = selectedIndex == currentStep.correctIndex

            // Show explanation for this choice
            if (currentStep.explanation.isNotEmpty()) {
                currentExplanation = currentStep.explanation
                showExplanation = true
                delay(3000L)
            }

            if (!isCorrect) {
                totalMistakes++
                if (currentStep.isCrucialMistake) {
                    crucialMistakes++
                }
            }

            // Check game ending conditions
            when {
                currentStep.endsGameIfCorrect && isCorrect -> gameWon = true
                currentStep.endsGameIfWrong && !isCorrect -> gameOver = true
                crucialMistakes >= 3 -> gameOver = true
                else -> {
                    val nextStep =
                        if (isCorrect) currentStep.nextStepIfCorrect else currentStep.nextStepIfWrong
                    if (nextStep == null || nextStep == -1) {
                        gameWon = isCorrect
                        gameOver = !isCorrect
                    } else {
                        stepIndex = nextStep
                    }
                }
            }
            selectedIndex = null
        }
    }

    BackgroundWrapper(imageResId = R.drawable.bg_profile) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Scam Scenario Simulator", color = Color.White) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E1E1E)),
                    navigationIcon = {
                        IconButton(onClick = { nav.popBackStack() }) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    }
                )
            },
            bottomBar = { SmartGuardBottomBar(nav, currentRoute = "tips") }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                if (selectedScenario == null) {
                    // Scenario Selection Screen
                    Column(
                        Modifier
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            "Choose a Scam Scenario",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Test your ability to spot scams! Each scenario is based on real scam tactics used in Malaysia.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                        Spacer(Modifier.height(24.dp))

                        allScenarios.forEach { scenario ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                onClick = {
                                    selectedScenario = scenario
                                    stepIndex = 0
                                    crucialMistakes = 0
                                    totalMistakes = 0
                                    gameOver = false
                                    gameWon = false
                                    selectedIndex = null
                                    showChoices = false
                                    messageDelivered = false
                                    showExplanation = false
                                    chatHistory.clear()
                                    userChoices.clear()
                                    shownSteps.clear()
                                }
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    Text(
                                        scenario.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        scenario.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                } else if (gameOver || gameWon) {
                    // Game Over / Won Screen
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        val title = if (gameWon) "You Stayed Safe!" else "You Got Scammed!"
                        val titleColor =
                            if (gameWon) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error

                        Text(
                            title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = titleColor
                        )
                        Spacer(Modifier.height(16.dp))

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (gameWon) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                if (gameWon) {
                                    Text(
                                        "Great job! You successfully identified the scam and protected yourself.",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        "Mistakes made: $totalMistakes",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                } else {
                                    Text(
                                        "You fell for the scam!",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        "Critical mistakes: $crucialMistakes | Total mistakes: $totalMistakes",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Key Lessons:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))

                        when (selectedScenario!!.id) {
                            "bank_fraud" -> {
                                Card {
                                    Column(Modifier.padding(16.dp)) {
                                        Text(
                                            "• Banks NEVER call to ask for card numbers, CVV, or OTP\n" +
                                                    "• Always call the official number yourself (back of card)\n" +
                                                    "• Verify with reference numbers\n" +
                                                    "• Don't trust caller ID - it can be spoofed\n" +
                                                    "• Take your time - urgency is a scam tactic",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }

                            "delivery_scam" -> {
                                Card {
                                    Column(Modifier.padding(16.dp)) {
                                        Text(
                                            "• Avoid shortened links (bit.ly, tinyurl) in SMS\n" +
                                                    "• Couriers don't ask for IC numbers\n" +
                                                    "• Never login to banking on external sites\n" +
                                                    "• Check official courier websites directly\n" +
                                                    "• Real tracking numbers work on official sites",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }

                            "investment_scam" -> {
                                Card {
                                    Column(Modifier.padding(16.dp)) {
                                        Text(
                                            "• If it sounds too good to be true, it IS\n" +
                                                    "• Verify investments at www.sc.com.my\n" +
                                                    "• Personal accounts = RED FLAG\n" +
                                                    "• Never invest based on urgency\n" +
                                                    "• Check SC Investor Alert list",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }

                            "tech_support" -> {
                                Card {
                                    Column(Modifier.padding(16.dp)) {
                                        Text(
                                            "• Microsoft NEVER makes unsolicited calls\n" +
                                                    "• Pop-ups with phone numbers are SCAMS\n" +
                                                    "• Never give remote access to strangers\n" +
                                                    "• Use Task Manager to close fake pop-ups\n" +
                                                    "• Your antivirus is enough - no need to call anyone",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }

                            "romance_scam" -> {
                                Card {
                                    Column(Modifier.padding(16.dp)) {
                                        Text(
                                            "• Reverse image search suspicious profiles\n" +
                                                    "• Always insist on video calls\n" +
                                                    "• NEVER send money to people you haven't met\n" +
                                                    "• Broken camera + money request = SCAM\n" +
                                                    "• Real relationships don't need urgent money",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }

                            "job_scam" -> {
                                Card {
                                    Column(Modifier.padding(16.dp)) {
                                        Text(
                                            "• Legitimate jobs NEVER ask for payment\n" +
                                                    "• Verify company emails (@amazon.com not amazon-recruitment.com)\n" +
                                                    "• Check company on SSM website\n" +
                                                    "• Never send IC copies via WhatsApp\n" +
                                                    "• Apply through official career pages only",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(24.dp))
                        Button(
                            onClick = {
                                selectedScenario = null
                                stepIndex = 0
                                crucialMistakes = 0
                                totalMistakes = 0
                                gameOver = false
                                gameWon = false
                                selectedIndex = null
                                showChoices = false
                                messageDelivered = false
                                chatHistory.clear()
                                userChoices.clear()
                                shownSteps.clear()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Try Another Scenario")
                        }
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { nav.navigate("home") },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Back to Home")
                        }
                    }
                } else {
                    // Game Play Screen
                    // Progress bar similar to CourseDetailScreen
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 0.dp)
                    ) {
                        val total = selectedScenario?.steps?.size ?: 1
                        val current = stepIndex + 1
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF1E1E1E))
                                .padding(horizontal = 20.dp, vertical = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Scenario Progress",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.Gray
                                )
                                Text(
                                    "$current / $total",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { current.toFloat() / total.coerceAtLeast(1) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp),
                                color = Color(0xFF4CAF50),
                                trackColor = Color(0xFF424242),
                            )
                        }
                    }

                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(chatHistory.toList()) { (text, isUser) ->
                            ChatBubble(text = text, isUser = isUser)
                        }

                        if (showExplanation && currentExplanation.isNotEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                                    )
                                ) {
                                    Row(Modifier.padding(12.dp)) {
                                        Text("💡 ", style = MaterialTheme.typography.titleMedium)
                                        Text(
                                            currentExplanation,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        if (showChoices && messageDelivered && currentStep != null) {
                            item {
                                Column(Modifier.padding(16.dp)) {
                                    if (totalMistakes > 0) {
                                        Text(
                                            "⚠️ Mistakes: $totalMistakes (Critical: $crucialMistakes/3)",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                    }

                                    currentStep.choices.forEachIndexed { i, choice ->
                                        Button(
                                            onClick = { selectedIndex = i },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .defaultMinSize(minHeight = 50.dp)
                                                .padding(vertical = 4.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFF4CAF50)
                                            ),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text(choice, maxLines = 3)
                                        }
                                    }
                                }
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
        val bubbleColor =
            if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
        val alignment = if (isUser) Arrangement.End else Arrangement.Start
        val textColor =
            if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = alignment
        ) {
            Surface(
                color = bubbleColor,
                shape = MaterialTheme.shapes.medium,
                tonalElevation = 2.dp,
                modifier = Modifier.widthIn(max = 280.dp)
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

