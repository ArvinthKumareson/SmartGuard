package com.smartguard.app.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.builtins.ListSerializer


/**
 * Preferences DataStore used for storing a lightweight local history.
 */
val Context.historyDataStore by preferencesDataStore("history")

@OptIn(kotlinx.serialization.InternalSerializationApi::class)
@Serializable
data class ScanRecord(
    val message: String,
    val matchedKeywords: List<String>,
    val sourceApp: String?,
    val timestamp: Long,
    val senderName: String? = null,
    val conversationTitle: String? = null
)



/**
 * Wrapper around [preferencesDataStore] that keeps a bounded list of recent
 * scan records on the device.
 *
 * This is separate from the main encrypted Room database and Firestore,
 * and is mainly used for quick display of recent items.
 */
class HistoryStore(private val context: Context) {
    private val KEY = stringPreferencesKey("items")

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Reactive stream of all stored scan records (newest first in storage order).
     */
    val items: Flow<List<ScanRecord>> = context.historyDataStore.data.map { prefs ->
        prefs[KEY]?.let { s ->
            try { json.decodeFromString<List<ScanRecord>>(s) } catch (e: Exception) { emptyList() }
        } ?: emptyList()
    }

    /**
     * Adds a new record to the front of the list and keeps at most 200 items.
     */
    suspend fun add(record: ScanRecord) {
        context.historyDataStore.edit { prefs ->
            val current = prefs[KEY]?.let { s ->
                try { json.decodeFromString<List<ScanRecord>>(s) } catch (e: Exception) { emptyList() }
            } ?: emptyList()
            val updated = (listOf(record) + current).take(200)
            prefs[KEY] = json.encodeToString(ListSerializer(ScanRecord.serializer()), updated)
        }
    }

}