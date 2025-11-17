package com.smartguard.app.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory

/**
 * Main encrypted Room database for storing scan records on the device.
 *
 * The underlying file is encrypted using SQLCipher via [SupportFactory],
 * so even if the database file is extracted from the device, its contents
 * cannot be read without the passphrase.
 */
@Database(
    entities = [ScanRecordEntity::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun scanRecordDao(): ScanRecordDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        // Migration from version 3 to 4.
        // Adds extra metadata about the sender/conversation to existing rows.
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add new columns for sender information
                database.execSQL("ALTER TABLE scan_records ADD COLUMN senderName TEXT")
                database.execSQL("ALTER TABLE scan_records ADD COLUMN conversationTitle TEXT")
            }
        }

        /**
         * Returns a singleton instance of the encrypted [AppDatabase].
         *
         * This prevents accidental creation of multiple database instances,
         * which could lead to file locks or inconsistent migrations.
         */
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val passphrase: ByteArray = SQLiteDatabase.getBytes("smartguard-secret".toCharArray())
                val factory = SupportFactory(passphrase)

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "smartguard.db"
                )
                    .openHelperFactory(factory)
                    .addMigrations(MIGRATION_3_4)
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
