package com.smartguard.app.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.telephony.SmsMessage
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.smartguard.app.data.DetectionEngine
import com.smartguard.app.data.KeywordRepository
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

        val engine = DetectionEngine(KeywordRepository.getKeywords())
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

                // ✅ Save locally
                db.scanRecordDao().insert(entity)

                // ✅ Sync to Firestore
                val doc = mapOf(
                    "message" to entity.message,
                    "matched_keywords" to entity.matchedKeywords,
                    "source_app" to entity.sourceApp,
                    "timestamp" to entity.timestamp
                )

                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .collection("scamMessages")
                    .add(doc)
                    .addOnSuccessListener {
                        Log.d("SmsReceiver", "Synced SMS to Firestore for user: $userId")
                    }
                    .addOnFailureListener {
                        Log.e("SmsReceiver", "Firestore sync failed: ${it.message}")
                    }
            }
        }
    }
}
