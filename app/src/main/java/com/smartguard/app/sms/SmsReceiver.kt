package com.smartguard.app.sms

import android.R.attr.text
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.telephony.SmsMessage
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.smartguard.app.data.DetectionEngine
import com.smartguard.app.data.EncryptedKeywords
import com.smartguard.app.data.ScanRecord
import com.smartguard.app.data.UserHistoryStore
import com.smartguard.app.db.AppDatabase
import com.smartguard.app.db.ScanRecordEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("SmsReceiver", "SMS_RECEIVED triggered")

        if (context == null || intent == null) return
        val bundle: Bundle = intent.extras ?: return

        Log.d("SmsReceiver", "Intent action: ${intent.action}")
        Log.d("SmsReceiver", "Bundle keys: ${bundle.keySet()}")
        Log.d("SmsReceiver", "Extracted SMS text: $text")

        val format = bundle.getString("format")

        @Suppress("DEPRECATION")
        val pdus = bundle.get("pdus") as? Array<*> ?: return

        val messages = pdus.mapNotNull { pdu ->
            val pduBytes = pdu as? ByteArray ?: return@mapNotNull null
            SmsMessage.createFromPdu(pduBytes, format)
        }

        val text = messages.joinToString(" ") { it.messageBody ?: "" }.trim()
        if (text.isBlank()) return

        val engine = DetectionEngine(context)
        val matchedKeywords = engine.scanWithExplanations(text)

        if (matchedKeywords.isNotEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getInstance(context.applicationContext)
                    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: run {
                        Log.w("SmsReceiver", "No Firebase user found. Skipping sync.")
                        return@launch
                    }

                    val keywordsList = matchedKeywords.map { it.keyword }
                    val explanationsMap = matchedKeywords.associate { it.keyword to it.explanation }
                    val explanationsJson = com.google.gson.Gson().toJson(explanationsMap)

                    val entity = ScanRecordEntity(
                        message = text,
                        matchedKeywords = keywordsList.joinToString(","),
                        keywordExplanations = explanationsJson,
                        sourceApp = "SMS",
                        timestamp = System.currentTimeMillis(),
                        userId = userId
                    )

                    db.scanRecordDao().insert(entity)

                    val store = UserHistoryStore(context)
                    store.save(
                        ScanRecord(
                            message = text,
                            matchedKeywords = keywordsList,
                            sourceApp = "SMS",
                            timestamp = entity.timestamp
                        )
                    )
                } catch (e: Exception) {
                    Log.e("SmsReceiver", "Failed to save scan record", e)
                }
            }
        }
    }
}
