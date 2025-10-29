package com.smartguard.app.utils

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationManagerCompat

object PermissionUtils {
    
    fun isNotificationListenerEnabled(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val packageName = context.packageName
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                
                // Check if we can access active notifications (this is the proper way to check)
                try {
                    val activeNotifications = notificationManager.activeNotifications
                    Log.d("PermissionUtils", "Can access active notifications: ${activeNotifications != null}")
                    
                    // If we can access notifications, the permission is granted
                    val isEnabled = activeNotifications != null
                    Log.d("PermissionUtils", "Notification listener enabled: $isEnabled")
                    return isEnabled
                } catch (e: SecurityException) {
                    Log.d("PermissionUtils", "SecurityException - notification access not granted: ${e.message}")
                    return false
                } catch (e: Exception) {
                    Log.e("PermissionUtils", "Exception checking notification access: ${e.message}")
                    return false
                }
            } catch (e: Exception) {
                Log.e("PermissionUtils", "Failed to check notification listener status", e)
                // Fallback: assume not enabled to prompt user to check
                false
            }
        } else {
            // For older API levels, we can't check this programmatically
            // Return false to prompt user to check manually
            false
        }
    }
    
    fun openNotificationSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("PermissionUtils", "Failed to open notification settings", e)
            // Fallback to general settings
            try {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.parse("package:${context.packageName}")
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
            } catch (e2: Exception) {
                Log.e("PermissionUtils", "Failed to open app settings", e2)
            }
        }
    }
    
    fun getNotificationPermissionStatus(context: Context): String {
        val isEnabled = isNotificationListenerEnabled(context)
        return if (isEnabled) "Enabled" else "Disabled"
    }
    
    fun debugPermissionStatus(context: Context): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val packageName = context.packageName
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                
                return try {
                    val activeNotifications = notificationManager.activeNotifications
                    "Package: $packageName\nCan access notifications: ${activeNotifications != null}\nEnabled: ${activeNotifications != null}"
                } catch (e: SecurityException) {
                    "Package: $packageName\nSecurityException: ${e.message}\nEnabled: false"
                } catch (e: Exception) {
                    "Package: $packageName\nException: ${e.message}\nEnabled: false"
                }
            } catch (e: Exception) {
                "Error: ${e.message}"
            }
        } else {
            "API < 23"
        }
    }
}
