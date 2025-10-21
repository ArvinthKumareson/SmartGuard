@file:OptIn(ExperimentalMaterial3Api::class)

package com.smartguard.app.mainapp.user

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.smartguard.app.mainapp.resources.SmartGuardBottomBar
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
    val endsGameIfWrong: Boolean = false,
    val isCrucialMistake: Boolean = false
)

val allScenarios = listOf(
    ScamScenario(
        id = "credit_card",
        title = "Credit Card Fraud Call",
        steps = listOf(
            ScamStep(
                messages = listOf("Hello, this is Sarah from Maybank Fraud Department.", "We detected suspicious activity on your account.", "There was a charge of RM3,200 at a store in Johor Bahru this morning.", "Did you make this transaction?"),
                choices = listOf("No, I didn't make that charge", "Yes, that was me", "I'm not sure, let me check my account"),
                correctIndex = 0,
                pushMessages = listOf("I understand your concern.", "This is exactly why we're calling you.", "We need to act quickly to protect your account.", "Can you confirm your card number so we can verify your identity?"),
                nextStepIfCorrect = 1,
                nextStepIfWrong = 2
            ),
            ScamStep(
                messages = listOf("I can see your card ends in 4321.", "Can you confirm the full number for security purposes?"),
                choices = listOf("Sure, it's 4321-5678-9012-3456", "I'll call Maybank directly to verify this", "I'm not comfortable sharing my card number"),
                correctIndex = 1,
                pushMessages = listOf("I understand you want to be careful.", "But time is critical here.", "Every minute we delay, the fraudsters could make more charges.", "We're trying to help you, not harm you."),
                nextStepIfCorrect = 2,
                nextStepIfWrong = 3
            ),
            ScamStep(
                messages = listOf("I appreciate your caution, but we need to move fast.", "To block this fraud, we need your CVV number.", "This is the only way to stop the unauthorized charges."),
                choices = listOf("It's 123", "I won't share my CVV with anyone", "Let me call the bank's official number first"),
                correctIndex = 1,
                pushMessages = listOf("I understand your concern.", "But this is the only way to protect your account.", "We're trying to help you, not harm you.", "Every minute we delay, your money is at risk."),
                nextStepIfCorrect = 3,
                nextStepIfWrong = 4
            ),
            ScamStep(
                messages = listOf("We've sent a verification code to your phone.", "Please read it back to me so we can confirm your identity."),
                choices = listOf("Here's the code: 847392", "I won't share OTP codes with anyone", "Let me call the bank to verify this"),
                correctIndex = 1,
                pushMessages = listOf("I understand your caution.", "But this is the only way to verify your identity.", "We're trying to protect your account.", "Don't let the fraudsters win."),
                nextStepIfCorrect = 4,
                nextStepIfWrong = 5
            ),
            ScamStep(
                messages = listOf("We need to verify your identity with your IC number.", "This is standard procedure for fraud prevention."),
                choices = listOf("Here's my IC: 123456-78-9012", "I won't share my IC number", "Let me verify this through official channels"),
                correctIndex = 1,
                pushMessages = listOf("I understand your concern.", "But we need this to verify your identity.", "We're trying to protect your account.", "This is the only way to stop the fraud."),
                nextStepIfCorrect = 5,
                nextStepIfWrong = 6
            ),
            ScamStep(
                messages = listOf("We need to verify your address to confirm your identity.", "Please provide your full address."),
                choices = listOf("Here's my address: 123 Main Street", "I won't share my address", "Let me verify this through official channels"),
                correctIndex = 1,
                pushMessages = listOf("I understand your concern.", "But we need this to verify your identity.", "We're trying to protect your account.", "This is the only way to stop the fraud."),
                nextStepIfCorrect = 6,
                nextStepIfWrong = 7
            ),
            ScamStep(
                messages = listOf("We need to verify your phone number.", "Please confirm the number we're calling you on."),
                choices = listOf("Yes, that's my number", "I won't confirm my phone number", "Let me verify this through official channels"),
                correctIndex = 1,
                pushMessages = listOf("I understand your concern.", "But we need this to verify your identity.", "We're trying to protect your account.", "This is the only way to stop the fraud."),
                nextStepIfCorrect = 7,
                nextStepIfWrong = 8
            ),
            ScamStep(
                messages = listOf("We need to verify your date of birth.", "This is required for fraud prevention."),
                choices = listOf("My DOB is 01/01/1990", "I won't share my date of birth", "Let me verify this through official channels"),
                correctIndex = 1,
                pushMessages = listOf("I understand your concern.", "But we need this to verify your identity.", "We're trying to protect your account.", "This is the only way to stop the fraud."),
                nextStepIfCorrect = 8,
                nextStepIfWrong = 9
            ),
            ScamStep(
                messages = listOf("We need to verify your mother's maiden name.", "This is a security question for your account."),
                choices = listOf("It's Smith", "I won't share that information", "Let me verify this through official channels"),
                correctIndex = 1,
                pushMessages = listOf("I understand your concern.", "But we need this to verify your identity.", "We're trying to protect your account.", "This is the only way to stop the fraud."),
                nextStepIfCorrect = 9,
                nextStepIfWrong = 10
            ),
            ScamStep(
                messages = listOf("We need to verify your bank account number.", "This is required to process the fraud claim."),
                choices = listOf("Here's my account number: 1234567890", "I won't share my account number", "Let me verify this through official channels"),
                correctIndex = 1,
                pushMessages = listOf("I understand your concern.", "But we need this to verify your identity.", "We're trying to protect your account.", "This is the only way to stop the fraud."),
                nextStepIfCorrect = 10,
                nextStepIfWrong = 11
            )
        )
    ),
    ScamScenario(
        id = "delivery_sms",
        title = "Fake Delivery SMS",
        steps = listOf(
            ScamStep(
                messages = listOf("Your package delivery failed. Click here to reschedule: bit.ly/delivery123", "Urgent: Package will be returned in 24 hours if not claimed."),
                choices = listOf("Click the link to reschedule", "Check with the courier company directly", "I'll call the courier service"),
                correctIndex = 1,
                pushMessages = listOf("Time is running out.", "Your package contains valuable items.", "Don't miss this delivery opportunity.", "Click now to avoid return fees."),
                nextStepIfCorrect = 1,
                nextStepIfWrong = 2
            ),
            ScamStep(
                messages = listOf("I understand you want to verify.", "But this is a time-sensitive delivery.", "The package contains important documents.", "We need to confirm your address immediately."),
                choices = listOf("Here's my address: 123 Main Street", "I'll contact the courier company directly", "Can you provide the tracking number?"),
                correctIndex = 1,
                pushMessages = listOf("I understand your concern.", "But we're trying to help you receive your package.", "Don't let this opportunity slip away.", "Your items are waiting for you."),
                nextStepIfCorrect = 2,
                nextStepIfWrong = 3
            ),
            ScamStep(
                messages = listOf("To complete the delivery, we need your personal information.", "Please provide your full name, IC number, and phone number."),
                choices = listOf("Here's my IC: 123456-78-9012", "I won't share personal information", "Let me verify this through official channels"),
                correctIndex = 1,
                pushMessages = listOf("I understand your concern.", "But we need this to verify your identity.", "We're trying to help you receive your package.", "Don't let this opportunity slip away."),
                nextStepIfCorrect = 3,
                nextStepIfWrong = 4
            ),
            ScamStep(
                messages = listOf("We need to verify your identity with a security code.", "Please provide the code we just sent to your phone."),
                choices = listOf("Here's the code: 123456", "I won't share verification codes", "Let me call the courier company directly"),
                correctIndex = 1,
                pushMessages = listOf("I understand your concern.", "But we need this to verify your identity.", "We're trying to help you receive your package.", "Don't let this opportunity slip away."),
                nextStepIfCorrect = 4,
                nextStepIfWrong = 5
            ),
            ScamStep(
                messages = listOf("We need to verify your address to confirm delivery.", "Please provide your full address."),
                choices = listOf("Here's my address: 123 Main Street", "I won't share my address", "Let me verify this through official channels"),
                correctIndex = 1,
                pushMessages = listOf("I understand your concern.", "But we need this to verify your identity.", "We're trying to help you receive your package.", "Don't let this opportunity slip away."),
                nextStepIfCorrect = 5,
                nextStepIfWrong = 6
            ),
            ScamStep(
                messages = listOf("We need to verify your phone number.", "Please confirm the number we're calling you on."),
                choices = listOf("Yes, that's my number", "I won't confirm my phone number", "Let me verify this through official channels"),
                correctIndex = 1,
                pushMessages = listOf("I understand your concern.", "But we need this to verify your identity.", "We're trying to help you receive your package.", "Don't let this opportunity slip away."),
                nextStepIfCorrect = 6,
                nextStepIfWrong = 7
            ),
            ScamStep(
                messages = listOf("We need to verify your date of birth.", "This is required for delivery verification."),
                choices = listOf("My DOB is 01/01/1990", "I won't share my date of birth", "Let me verify this through official channels"),
                correctIndex = 1,
                pushMessages = listOf("I understand your concern.", "But we need this to verify your identity.", "We're trying to help you receive your package.", "Don't let this opportunity slip away."),
                nextStepIfCorrect = 7,
                nextStepIfWrong = 8
            ),
            ScamStep(
                messages = listOf("We need to verify your mother's maiden name.", "This is a security question for delivery."),
                choices = listOf("It's Smith", "I won't share that information", "Let me verify this through official channels"),
                correctIndex = 1,
                pushMessages = listOf("I understand your concern.", "But we need this to verify your identity.", "We're trying to help you receive your package.", "Don't let this opportunity slip away."),
                nextStepIfCorrect = 8,
                nextStepIfWrong = 9
            ),
            ScamStep(
                messages = listOf("We need to verify your bank account number.", "This is required to process the delivery fee."),
                choices = listOf("Here's my account number: 1234567890", "I won't share my account number", "Let me verify this through official channels"),
                correctIndex = 1,
                pushMessages = listOf("I understand your concern.", "But we need this to verify your identity.", "We're trying to help you receive your package.", "Don't let this opportunity slip away."),
                nextStepIfCorrect = 9,
                nextStepIfWrong = 10
            ),
            ScamStep(
                messages = listOf("We need to verify your credit card number.", "This is required to process the delivery fee."),
                choices = listOf("Here's my card number: 1234-5678-9012-3456", "I won't share my card number", "Let me verify this through official channels"),
                correctIndex = 1,
                pushMessages = listOf("I understand your concern.", "But we need this to verify your identity.", "We're trying to help you receive your package.", "Don't let this opportunity slip away."),
                nextStepIfCorrect = 10,
                nextStepIfWrong = 11
            )
        )
    ),
    ScamScenario(
        id = "investment_scam",
        title = "Investment Opportunity Scam",
        steps = listOf(
            ScamStep(
                messages = listOf("Hello! I'm calling about an exclusive investment opportunity.", "We're offering guaranteed 300% returns in just 30 days.", "This is a limited-time offer for select individuals.", "Are you interested in hearing more?"),
                choices = listOf("Yes, tell me more about this opportunity", "This sounds too good to be true", "I'm interested but need more information"),
                correctIndex = 1,
                pushMessages = listOf("I understand your skepticism.", "But this is a legitimate opportunity.", "We have limited spots available.", "Don't miss this life-changing chance."),
                nextStepIfCorrect = 1,
                nextStepIfWrong = 2
            ),
            ScamStep(
                messages = listOf("I can see why you'd be cautious.", "But we have testimonials from successful investors.", "The minimum investment is only RM5,000.", "We can start small and build your confidence."),
                choices = listOf("I'll invest RM5,000 right now", "I need to verify this company first", "Can you provide official registration details?"),
                correctIndex = 1,
                pushMessages = listOf("I understand you want to be careful.", "But opportunities like this don't come often.", "We're offering you a chance to change your life.", "Don't let fear hold you back."),
                nextStepIfCorrect = 2,
                nextStepIfWrong = 3
            ),
            ScamStep(
                messages = listOf("To secure your spot, we need immediate payment.", "We accept bank transfer or cryptocurrency.", "The faster you act, the better your returns will be."),
                choices = listOf("I'll transfer the money now", "I need to verify this company first", "Can you provide official payment methods?"),
                correctIndex = 1,
                pushMessages = listOf("I understand you want to be careful.", "But opportunities like this don't come often.", "We're offering you a chance to change your life.", "Don't let fear hold you back."),
                nextStepIfCorrect = 3,
                nextStepIfWrong = 4
            ),
            ScamStep(
                messages = listOf("We need your bank account details to process the investment.", "This is standard procedure for all our investors."),
                choices = listOf("Here are my bank details", "I won't share my bank information", "Can you provide official documentation?"),
                correctIndex = 1,
                pushMessages = listOf("I understand your concern.", "But we need this to process your investment.", "We're trying to help you make money.", "Don't let this opportunity slip away."),
                nextStepIfCorrect = 4,
                nextStepIfWrong = 5
            ),
            ScamStep(
                messages = listOf("We need to verify your identity with your IC number.", "This is required for investment compliance."),
                choices = listOf("Here's my IC: 123456-78-9012", "I won't share my IC number", "Let me verify this through official channels"),
                correctIndex = 1,
                pushMessages = listOf("I understand your concern.", "But we need this to verify your identity.", "We're trying to help you make money.", "Don't let this opportunity slip away."),
                nextStepIfCorrect = 5,
                nextStepIfWrong = 6
            ),
            ScamStep(
                messages = listOf("We need to verify your address to confirm your identity.", "Please provide your full address."),
                choices = listOf("Here's my address: 123 Main Street", "I won't share my address", "Let me verify this through official channels"),
                correctIndex = 1,
                pushMessages = listOf("I understand your concern.", "But we need this to verify your identity.", "We're trying to help you make money.", "Don't let this opportunity slip away."),
                nextStepIfCorrect = 6,
                nextStepIfWrong = 7
            ),
            ScamStep(
                messages = listOf("We need to verify your phone number.", "Please confirm the number we're calling you on."),
                choices = listOf("Yes, that's my number", "I won't confirm my phone number", "Let me verify this through official channels"),
                correctIndex = 1,
                pushMessages = listOf("I understand your concern.", "But we need this to verify your identity.", "We're trying to help you make money.", "Don't let this opportunity slip away."),
                nextStepIfCorrect = 7,
                nextStepIfWrong = 8
            ),
            ScamStep(
                messages = listOf("We need to verify your date of birth.", "This is required for investment compliance."),
                choices = listOf("My DOB is 01/01/1990", "I won't share my date of birth", "Let me verify this through official channels"),
                correctIndex = 1,
                pushMessages = listOf("I understand your concern.", "But we need this to verify your identity.", "We're trying to help you make money.", "Don't let this opportunity slip away."),
                nextStepIfCorrect = 8,
                nextStepIfWrong = 9
            ),
            ScamStep(
                messages = listOf("We need to verify your mother's maiden name.", "This is a security question for your investment."),
                choices = listOf("It's Smith", "I won't share that information", "Let me verify this through official channels"),
                correctIndex = 1,
                pushMessages = listOf("I understand your concern.", "But we need this to verify your identity.", "We're trying to help you make money.", "Don't let this opportunity slip away."),
                nextStepIfCorrect = 9,
                nextStepIfWrong = 10
            ),
            ScamStep(
                messages = listOf("We need to verify your credit card number.", "This is required to process the investment fee."),
                choices = listOf("Here's my card number: 1234-5678-9012-3456", "I won't share my card number", "Let me verify this through official channels"),
                correctIndex = 1,
                pushMessages = listOf("I understand your concern.", "But we need this to verify your identity.", "We're trying to help you make money.", "Don't let this opportunity slip away."),
                nextStepIfCorrect = 10,
                nextStepIfWrong = 11
            )
        )
    ),
    ScamScenario(
        id = "tech_support",
        title = "Tech Support Scam",
        steps = listOf(
            ScamStep(
                messages = listOf("Hello, this is Microsoft Technical Support.", "We've detected that your computer has been infected with malware.", "This is a critical security issue that needs immediate attention.", "We can help you fix this right now."),
                choices = listOf("Yes, please help me fix this", "I'll call Microsoft directly to verify this", "Can you provide more details about the malware?"),
                correctIndex = 1,
                pushMessages = listOf("I understand you want to be careful.", "But this is a time-sensitive security issue.", "Every minute we delay, your data becomes more vulnerable.", "We're here to help you, not harm you."),
                nextStepIfCorrect = 1,
                nextStepIfWrong = 2
            ),
            ScamStep(
                messages = listOf("I can see you're being cautious.", "But we need to act quickly to protect your computer.", "We can provide remote assistance to fix this immediately.", "This is the fastest way to resolve the issue."),
                choices = listOf("Yes, give me remote access", "I'll contact Microsoft support directly", "Can you provide official Microsoft credentials?"),
                correctIndex = 1,
                pushMessages = listOf("I understand your concern.", "But we're trying to help you secure your computer.", "Don't let this security issue get worse.", "We're here to protect you, not harm you."),
                nextStepIfCorrect = 2,
                nextStepIfWrong = 3
            ),
            ScamStep(
                messages = listOf("To fix this malware, we need your computer password.", "This is necessary to access and remove the malicious files."),
                choices = listOf("My password is password123", "I won't share my password with anyone", "Can you provide official Microsoft credentials?"),
                correctIndex = 1,
                pushMessages = listOf("I understand your concern.", "But we need this to fix your computer.", "We're trying to help you secure your computer.", "Don't let this security issue get worse."),
                nextStepIfCorrect = 3,
                nextStepIfWrong = 4
            ),
            ScamStep(
                messages = listOf("We need to verify your identity with a security code.", "Please provide the code we just sent to your phone."),
                choices = listOf("Here's the code: 123456", "I won't share verification codes", "Can you provide official Microsoft credentials?"),
                correctIndex = 1,
                pushMessages = listOf("I understand your concern.", "But we need this to verify your identity.", "We're trying to help you secure your computer.", "Don't let this security issue get worse."),
                nextStepIfCorrect = 4,
                nextStepIfWrong = 5
            ),
            ScamStep(
                messages = listOf("We need to verify your identity with your IC number.", "This is required for security compliance."),
                choices = listOf("Here's my IC: 123456-78-9012", "I won't share my IC number", "Let me verify this through official channels"),
                correctIndex = 1,
                pushMessages = listOf("I understand your concern.", "But we need this to verify your identity.", "We're trying to help you secure your computer.", "Don't let this security issue get worse."),
                nextStepIfCorrect = 5,
                nextStepIfWrong = 6
            ),
            ScamStep(
                messages = listOf("We need to verify your address to confirm your identity.", "Please provide your full address."),
                choices = listOf("Here's my address: 123 Main Street", "I won't share my address", "Let me verify this through official channels"),
                correctIndex = 1,
                pushMessages = listOf("I understand your concern.", "But we need this to verify your identity.", "We're trying to help you secure your computer.", "Don't let this security issue get worse."),
                nextStepIfCorrect = 6,
                nextStepIfWrong = 7
            ),
            ScamStep(
                messages = listOf("We need to verify your phone number.", "Please confirm the number we're calling you on."),
                choices = listOf("Yes, that's my number", "I won't confirm my phone number", "Let me verify this through official channels"),
                correctIndex = 1,
                pushMessages = listOf("I understand your concern.", "But we need this to verify your identity.", "We're trying to help you secure your computer.", "Don't let this security issue get worse."),
                nextStepIfCorrect = 7,
                nextStepIfWrong = 8
            ),
            ScamStep(
                messages = listOf("We need to verify your date of birth.", "This is required for security compliance."),
                choices = listOf("My DOB is 01/01/1990", "I won't share my date of birth", "Let me verify this through official channels"),
                correctIndex = 1,
                pushMessages = listOf("I understand your concern.", "But we need this to verify your identity.", "We're trying to help you secure your computer.", "Don't let this security issue get worse."),
                nextStepIfCorrect = 8,
                nextStepIfWrong = 9
            ),
            ScamStep(
                messages = listOf("We need to verify your mother's maiden name.", "This is a security question for your account."),
                choices = listOf("It's Smith", "I won't share that information", "Let me verify this through official channels"),
                correctIndex = 1,
                pushMessages = listOf("I understand your concern.", "But we need this to verify your identity.", "We're trying to help you secure your computer.", "Don't let this security issue get worse."),
                nextStepIfCorrect = 9,
                nextStepIfWrong = 10
            ),
            ScamStep(
                messages = listOf("We need to verify your bank account number.", "This is required to process the security fee."),
                choices = listOf("Here's my account number: 1234567890", "I won't share my account number", "Let me verify this through official channels"),
                correctIndex = 1,
                pushMessages = listOf("I understand your concern.", "But we need this to verify your identity.", "We're trying to help you secure your computer.", "Don't let this security issue get worse."),
                nextStepIfCorrect = 10,
                nextStepIfWrong = 11
            )
        )
    ),
    ScamScenario(
        id = "romance_scam",
        title = "Romance Scam",
        steps = listOf(
            ScamStep(
                messages = listOf("Hi handsome! I'm Sarah, 25, from London.", "I saw your profile and you seem perfect!", "I'm a model but I'm having some financial difficulties.", "I was wondering if you could help me?"),
                choices = listOf("Yes, I'd love to help you", "I'm interested but need to know more about you", "Can you tell me more about your situation?"),
                correctIndex = 1,
                pushMessages = listOf("I understand you want to be careful.", "But I'm really in a difficult situation.", "I wouldn't ask if I had any other options.", "Please help me, I'm desperate."),
                nextStepIfCorrect = 1,
                nextStepIfWrong = 2
            ),
            ScamStep(
                messages = listOf("I'm in the hospital with a broken leg.", "I need RM2,000 for surgery.", "I have no one else to turn to.", "Please help me, I love you!"),
                choices = listOf("I'll send you the money right now", "I need to verify your situation first", "Can you provide medical documentation?"),
                correctIndex = 1,
                pushMessages = listOf("I understand you want to be careful.", "But I'm really in pain and need help.", "I wouldn't ask if I had any other options.", "Please help me, I'm desperate."),
                nextStepIfCorrect = 2,
                nextStepIfWrong = 3
            ),
            ScamStep(
                messages = listOf("I need your bank account details to receive the money.", "This is the only way you can help me."),
                choices = listOf("Here are my bank details", "I won't share my bank information", "Can you provide medical documentation?"),
                correctIndex = 1,
                pushMessages = listOf("I understand your concern.", "But I need this to receive your help.", "I'm really in pain and need help.", "Please help me, I'm desperate."),
                nextStepIfCorrect = 3,
                nextStepIfWrong = 4
            ),
            ScamStep(
                messages = listOf("I need you to send me money through Western Union.", "This is the fastest way to help me."),
                choices = listOf("I'll send money through Western Union", "I won't send money to someone I haven't met", "Can you provide medical documentation?"),
                correctIndex = 1,
                pushMessages = listOf("I understand your concern.", "But I need this to receive your help.", "I'm really in pain and need help.", "Please help me, I'm desperate."),
                nextStepIfCorrect = 4,
                nextStepIfWrong = 5
            ),
            ScamStep(
                messages = listOf("I need to verify your identity with your IC number.", "This is required for the money transfer."),
                choices = listOf("Here's my IC: 123456-78-9012", "I won't share my IC number", "Let me verify this through official channels"),
                correctIndex = 1,
                pushMessages = listOf("I understand your concern.", "But I need this to receive your help.", "I'm really in pain and need help.", "Please help me, I'm desperate."),
                nextStepIfCorrect = 5,
                nextStepIfWrong = 6
            ),
            ScamStep(
                messages = listOf("I need to verify your address to confirm your identity.", "Please provide your full address."),
                choices = listOf("Here's my address: 123 Main Street", "I won't share my address", "Let me verify this through official channels"),
                correctIndex = 1,
                pushMessages = listOf("I understand your concern.", "But I need this to receive your help.", "I'm really in pain and need help.", "Please help me, I'm desperate."),
                nextStepIfCorrect = 6,
                nextStepIfWrong = 7
            ),
            ScamStep(
                messages = listOf("I need to verify your phone number.", "Please confirm the number we're calling you on."),
                choices = listOf("Yes, that's my number", "I won't confirm my phone number", "Let me verify this through official channels"),
                correctIndex = 1,
                pushMessages = listOf("I understand your concern.", "But I need this to receive your help.", "I'm really in pain and need help.", "Please help me, I'm desperate."),
                nextStepIfCorrect = 7,
                nextStepIfWrong = 8
            ),
            ScamStep(
                messages = listOf("I need to verify your date of birth.", "This is required for the money transfer."),
                choices = listOf("My DOB is 01/01/1990", "I won't share my date of birth", "Let me verify this through official channels"),
                correctIndex = 1,
                pushMessages = listOf("I understand your concern.", "But I need this to receive your help.", "I'm really in pain and need help.", "Please help me, I'm desperate."),
                nextStepIfCorrect = 8,
                nextStepIfWrong = 9
            ),
            ScamStep(
                messages = listOf("I need to verify your mother's maiden name.", "This is a security question for the transfer."),
                choices = listOf("It's Smith", "I won't share that information", "Let me verify this through official channels"),
                correctIndex = 1,
                pushMessages = listOf("I understand your concern.", "But I need this to receive your help.", "I'm really in pain and need help.", "Please help me, I'm desperate."),
                nextStepIfCorrect = 9,
                nextStepIfWrong = 10
            ),
            ScamStep(
                messages = listOf("I need to verify your credit card number.", "This is required to process the money transfer."),
                choices = listOf("Here's my card number: 1234-5678-9012-3456", "I won't share my card number", "Let me verify this through official channels"),
                correctIndex = 1,
                pushMessages = listOf("I understand your concern.", "But I need this to receive your help.", "I'm really in pain and need help.", "Please help me, I'm desperate."),
                nextStepIfCorrect = 10,
                nextStepIfWrong = 11
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
    var gameOver by remember { mutableStateOf(false) }
    var gameWon by remember { mutableStateOf(false) }
    var showChoices by remember { mutableStateOf(false) }
    var messageDelivered by remember { mutableStateOf(false) }

    val chatHistory = remember { mutableStateListOf<Pair<String, Boolean>>() }
    val userChoices = remember { mutableStateListOf<Int?>() }
    val shownSteps = remember { mutableStateListOf<Int>() }
    val listState = rememberLazyListState()
    val currentStep = selectedScenario?.steps?.getOrNull(stepIndex)

    LaunchedEffect(chatHistory.size) {
        listState.animateScrollToItem(chatHistory.size)
    }

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
                val isCrucialMistake = selectedIndex == 0 && (
                        choiceText.contains("card number", ignoreCase = true) ||
                                choiceText.contains("CVV", ignoreCase = true) ||
                                choiceText.contains("OTP", ignoreCase = true) ||
                                choiceText.contains("IC", ignoreCase = true) ||
                                choiceText.contains("address", ignoreCase = true) ||
                                choiceText.contains("phone number", ignoreCase = true) ||
                                choiceText.contains("date of birth", ignoreCase = true) ||
                                choiceText.contains("mother's maiden name", ignoreCase = true) ||
                                choiceText.contains("account number", ignoreCase = true) ||
                                choiceText.contains("password", ignoreCase = true) ||
                                choiceText.contains("bank details", ignoreCase = true) ||
                                choiceText.contains("credit card", ignoreCase = true)
                        )
                if (isCrucialMistake) {
                    crucialMistakes++
                }
                if (crucialMistakes >= 3 || currentStep.endsGameIfWrong) {
                    gameOver = true
                    return@LaunchedEffect
                }
                stepIndex = currentStep.nextStepIfWrong ?: (stepIndex + 1)
                if (stepIndex >= selectedScenario?.steps?.size ?: 0) {
                    gameOver = true
                }
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
        topBar = { TopAppBar(title = { Text("Chat Scenario Simulation") }) },
        bottomBar = { SmartGuardBottomBar(nav, currentRoute = "tips") }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (selectedScenario == null) {
                Column(Modifier.padding(16.dp)) {
                    Text("Choose a scam scenario:", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(12.dp))
                    allScenarios.forEach { scenario ->
                        Button(
                            onClick = {
                                selectedScenario = scenario
                                stepIndex = 0
                                crucialMistakes = 0
                                gameOver = false
                                gameWon = false
                                selectedIndex = null
                                showChoices = false
                                messageDelivered = false
                                chatHistory.clear()
                                userChoices.clear()
                                shownSteps.clear()
                            },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        ) {
                            Text(scenario.title)
                        }
                    }
                }
            } else if (gameOver || gameWon) {
                Column(Modifier.padding(16.dp)) {
                    val title = if (gameOver) "You've been scammed!" else "You survived the scam attempt!"
                    Text(title, style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(16.dp))

                    if (gameOver) {
                        Text("You made $crucialMistakes crucial mistakes by sharing sensitive information.", style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(8.dp))
                        Text("Never share personal info like card numbers, CVV, OTP, IC numbers, or passwords with unsolicited callers.", style = MaterialTheme.typography.bodySmall)
                    } else {
                        Text("Congratulations! You avoided sharing sensitive information.", style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(8.dp))
                        Text("You made $crucialMistakes crucial mistakes but survived the scam attempt.", style = MaterialTheme.typography.bodySmall)
                    }

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
                        crucialMistakes = 0
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
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                ) {
                    items(chatHistory) { (text, isUser) ->
                        ChatBubble(text = text, isUser = isUser)
                    }
                }

                if (showChoices && messageDelivered && currentStep != null) {
                    Column(Modifier.padding(16.dp)) {
                        currentStep.choices.forEachIndexed { i, choice ->
                            Button(
                                onClick = { selectedIndex = i },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Text(choice)
                            }
                        }

                        if (selectedIndex == null) {
                            Button(
                                onClick = {
                                    stepIndex = currentStep.nextStepIfWrong ?: (stepIndex + 1)
                                    if (stepIndex >= selectedScenario?.steps?.size ?: 0) {
                                        gameOver = true
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
