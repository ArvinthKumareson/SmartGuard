package com.smartguard.app.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data access object for reading and writing [ScanRecordEntity] rows.
 *
 * Exposes convenience queries used by the history UI such as:
 *  - Recent records for a user.
 *  - Filtering by source app.
 *  - Finding "risky" messages that contain many keywords.
 */
@Dao
interface ScanRecordDao {

    /**
     * Inserts a new scan record.
     *
     * On conflict we ignore the insert to avoid duplicating the same
     * record multiple times.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(record: ScanRecordEntity)

    /**
     * Stream of all records for a given user, newest first.
     */
    @Query("SELECT * FROM scan_records WHERE userId = :userId ORDER BY timestamp DESC")
    fun recentForUser(userId: String): Flow<List<ScanRecordEntity>>

    /**
     * Deletes all records belonging to the given user.
     */
    @Query("DELETE FROM scan_records WHERE userId = :userId")
    suspend fun clearAllForUser(userId: String)
    
    /**
     * Deletes a single record by its primary key.
     *
     * @return number of rows affected (0 or 1).
     */
    @Query("DELETE FROM scan_records WHERE id = :id")
    suspend fun deleteById(id: Long): Int  // Returns number of rows deleted

    /**
     * Live count of records for a user (useful for badges/summary UI).
     */
    @Query("SELECT COUNT(*) FROM scan_records WHERE userId = :userId")
    fun countForUser(userId: String): Flow<Int>

    /**
     * Returns all records for a user coming from a specific app (e.g. "SMS").
     */
    @Query("SELECT * FROM scan_records WHERE userId = :userId AND sourceApp = :source ORDER BY timestamp DESC")
    fun recordsBySource(userId: String, source: String): Flow<List<ScanRecordEntity>>

    /**
     * Returns records that are considered "risky" by counting how many
     * keywords were matched (>= [minKeywords]).
     */
    @Query("""
        SELECT * FROM scan_records 
        WHERE userId = :userId AND 
              LENGTH(matchedKeywords) - LENGTH(REPLACE(matchedKeywords, ',', '')) + 1 >= :minKeywords 
        ORDER BY timestamp DESC
    """)
    fun riskyRecords(userId: String, minKeywords: Int): Flow<List<ScanRecordEntity>>

    /**
     * Performs a simple LIKE search on the comma-separated keyword list
     * to find all messages that matched a particular keyword.
     */
    @Query("""
        SELECT * FROM scan_records 
        WHERE userId = :userId AND matchedKeywords LIKE '%' || :keyword || '%' 
        ORDER BY timestamp DESC
    """)
    fun searchByKeyword(userId: String, keyword: String): Flow<List<ScanRecordEntity>>
}
