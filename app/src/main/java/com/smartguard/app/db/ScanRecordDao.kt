package com.smartguard.app.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanRecordDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(record: ScanRecordEntity)

    @Query("SELECT * FROM scan_records WHERE userId = :userId ORDER BY timestamp DESC")
    fun recentForUser(userId: String): Flow<List<ScanRecordEntity>>

    @Query("DELETE FROM scan_records WHERE userId = :userId")
    suspend fun clearAllForUser(userId: String)

    @Query("SELECT COUNT(*) FROM scan_records WHERE userId = :userId")
    fun countForUser(userId: String): Flow<Int>

    @Query("SELECT * FROM scan_records WHERE userId = :userId AND sourceApp = :source ORDER BY timestamp DESC")
    fun recordsBySource(userId: String, source: String): Flow<List<ScanRecordEntity>>

    @Query("""
        SELECT * FROM scan_records 
        WHERE userId = :userId AND 
              LENGTH(matchedKeywords) - LENGTH(REPLACE(matchedKeywords, ',', '')) + 1 >= :minKeywords 
        ORDER BY timestamp DESC
    """)
    fun riskyRecords(userId: String, minKeywords: Int): Flow<List<ScanRecordEntity>>

    @Query("""
        SELECT * FROM scan_records 
        WHERE userId = :userId AND matchedKeywords LIKE '%' || :keyword || '%' 
        ORDER BY timestamp DESC
    """)
    fun searchByKeyword(userId: String, keyword: String): Flow<List<ScanRecordEntity>>
}
