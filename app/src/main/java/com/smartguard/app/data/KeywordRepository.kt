package com.smartguard.app.data

object KeywordRepository {
    // Basic keyword list. Extend via DataStore updates in future.
    private val defaultKeywords = setOf(
        "invoice", "payment", "pay now", "billing", "billing information", "update payment",
        "account", "account update", "verify", "verification", "verify your account", "password",
        "password reset", "reset password", "account suspended", "account locked", "unusual activity",
        "security alert", "unauthorized", "unauthorized login", "action required", "immediate action required",
        "urgent", "important", "final notice", "overdue", "past due", "due now", "pay invoice", "make payment",
        "receipt", "purchase", "order", "order confirmation", "shipping", "shipment", "tracking number", "delivery",
        "delivery failure", "undelivered", "refund", "refund issued", "transaction", "transaction failed", "payment failed",
        "billing failure", "statement", "statement available", "invoice attached", "attachment", "document",
        "document attached", "view document", "download attachment", "efax", "voicemail", "new message", "unread message",
        "message from", "new scanned fax", "request", "request approval", "approval required", "authorize", "wire transfer",
        "wire details", "remittance", "beneficiary", "beneficiary change", "payroll", "paystub", "salary",
        "direct deposit", "tax", "tax refund", "customs", "customs fee", "chargeback", "payment verification", "confirm",
        "confirm your email", "confirm your account", "verify identity", "identity verification", "one-time password", "otp",
        "verification code", "code", "2fa", "mfa", "two-factor", "security code", "login", "new login", "new sign-in",
        "new sign-in from", "suspicious login", "unusual sign-in activity", "sign in attempt", "re:", "alert", "notice",
        "notification", "service update", "system administrator", "it alert", "software update", "critical update", "security patch",
        "please review", "action needed", "respond immediately", "click here", "click link", "open now", "view now", "confirm payment",
        "payment required", "subscription", "subscription expired", "trial expired", "account expired", "account deactivated",
        "activate account", "activate now", "verify payment", "billing information is out of date", "payment suspended",
        "limited time", "congratulations", "you won", "winner", "prize", "gift card", "lottery", "claim now", "crypto",
        "investment", "guaranteed", "Add admin"
    )

    fun getKeywords(): Set<String> = defaultKeywords
}