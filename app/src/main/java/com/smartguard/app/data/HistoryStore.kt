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


val Context.historyDataStore by preferencesDataStore("history")
@OptIn(kotlinx.serialization.InternalSerializationApi::class)
@Serializable
data class ScanRecord(
    val message: String,
    val matchedKeywords: List<String>,
    val sourceApp: String?,
    val timestamp: Long
)



class HistoryStore(private val context: Context) {
    private val KEY = stringPreferencesKey("items")

    private val json = Json { ignoreUnknownKeys = true }

    val items: Flow<List<ScanRecord>> = context.historyDataStore.data.map { prefs ->
        prefs[KEY]?.let { s ->
            try { json.decodeFromString<List<ScanRecord>>(s) } catch (e: Exception) { emptyList() }
        } ?: emptyList()
    }

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