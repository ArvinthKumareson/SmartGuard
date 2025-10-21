package com.smartguard.app.util

import android.content.Context
import com.smartguard.app.R

/**
 * Helper object to manage multimedia resources for courses.
 * Add your images to res/drawable/ and videos to res/raw/
 * Then reference them here for easy access.
 */
object MediaResourceHelper {
    
    // Example: How to reference an image in drawable folder
    // If you have: res/drawable/phishing_email_example.png
    // Use: val phishingEmailExample = R.drawable.phishing_email_example.toString()
    
    // ===== IMAGE RESOURCES =====
    // Add your drawable resource IDs here as strings
    
    // Phishing Email Course Images
    // val phishingEmailExample = R.drawable.phishing_email_example.toString()
    // val fakeSenderAddress = R.drawable.fake_sender_address.toString()
    // val suspiciousLink = R.drawable.suspicious_link.toString()
    
    // Password Security Images
    // val passwordStrengthChart = R.drawable.password_strength_chart.toString()
    // val passwordManagerExample = R.drawable.password_manager_example.toString()
    
    // 2FA Images
    // val twoFactorSetupSteps = R.drawable.two_factor_setup_steps.toString()
    // val authenticatorAppExample = R.drawable.authenticator_app_example.toString()
    
    
    // ===== VIDEO RESOURCES =====
    // Add your video resource URIs here
    
    /**
     * Generates the proper URI for a raw video resource.
     * @param resourceId The R.raw.xxx resource ID
     * @return URI string in format: android.resource://com.smartguard.app/[id]
     */
    fun getVideoUri(resourceId: Int): String {
        return "android.resource://com.smartguard.app/$resourceId"
    }
    
    // Example video URIs:
    // val passwordCreationDemo = getVideoUri(R.raw.password_creation_demo)
    // val twoFactorSetupTutorial = getVideoUri(R.raw.two_factor_setup_tutorial)
    // val phishingEmailWalkthrough = getVideoUri(R.raw.phishing_email_walkthrough)
    
    
    // ===== RUNTIME RESOURCE LOOKUP =====
    // Use these functions if you want to look up resources by name at runtime
    
    /**
     * Get drawable resource ID by name.
     * @param context Android Context
     * @param imageName Name of the drawable without extension (e.g., "phishing_example")
     * @return Resource ID as string, or null if not found
     */
    fun getImageResourceId(context: Context, imageName: String): String? {
        val resId = context.resources.getIdentifier(imageName, "drawable", context.packageName)
        return if (resId != 0) resId.toString() else null
    }
    
    /**
     * Get video URI by name.
     * @param context Android Context
     * @param videoName Name of the raw video without extension (e.g., "tutorial_video")
     * @return Video URI string, or null if not found
     */
    fun getVideoUriByName(context: Context, videoName: String): String? {
        val resId = context.resources.getIdentifier(videoName, "raw", context.packageName)
        return if (resId != 0) "android.resource://${context.packageName}/$resId" else null
    }
}

