package com.smartguard.app.sms

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
        if (context == null || intent == null) return

        val bundle: Bundle = intent.extras ?: return
        val format = bundle.getString("format")

        @Suppress("DEPRECATION")
        val pdus = bundle.get("pdus") as? Array<*> ?: return

        val messages = pdus.mapNotNull { pdu ->
            val pduBytes = pdu as? ByteArray ?: return@mapNotNull null
            SmsMessage.createFromPdu(pduBytes, format)
        }

        val text = messages.joinToString(" ") { it.messageBody ?: "" }.trim()
        if (text.isBlank()) return

        val keywords = EncryptedKeywords.getKeywords(context)
        val engine = DetectionEngine(keywords)
        val matches = engine.scan(text)

        if (matches.isNotEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                val db = AppDatabase.getInstance(context.applicationContext)
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: run {
                    Log.w("SmsReceiver", "No Firebase user found. Skipping sync.")
                    return@launch
                }

                val entity = ScanRecordEntity(
                    message = text,
                    matchedKeywords = matches.joinToString(","),
                    sourceApp = "SMS",
                    timestamp = System.currentTimeMillis(),
                    userId = userId
                )

                db.scanRecordDao().insert(entity)

                // ✅ Centralized Firestore sync
                val store = UserHistoryStore(context)
                store.save(
                    ScanRecord(
                        message = text,
                        matchedKeywords = matches,
                        sourceApp = "SMS",
                        timestamp = entity.timestamp
                    )
                )
            }
        }
    }
}
