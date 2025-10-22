package com.smartguard.app.util

import android.content.Context
import com.smartguard.app.R


object MediaResourceHelper {
    

    fun getVideoUri(resourceId: Int): String {
        return "android.resource://com.smartguard.app/$resourceId"
    }

    fun getImageResourceId(context: Context, imageName: String): String? {
        val resId = context.resources.getIdentifier(imageName, "drawable", context.packageName)
        return if (resId != 0) resId.toString() else null
    }

    fun getVideoUriByName(context: Context, videoName: String): String? {
        val resId = context.resources.getIdentifier(videoName, "raw", context.packageName)
        return if (resId != 0) "android.resource://${context.packageName}/$resId" else null
    }
}

