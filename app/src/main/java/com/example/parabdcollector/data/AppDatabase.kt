package com.example.parabdcollector.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.parabdcollector.dao.CollectionDao
import com.example.parabdcollector.dao.LocationDao
import com.example.parabdcollector.model.CollectionItem
import com.example.parabdcollector.model.Location

// Set the definitive new version to 13.
@Database(entities = [CollectionItem::class, Location::class], version = 13, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun collectionDao(): CollectionDao
    abstract fun locationDao(): LocationDao

    companion object {
        const val DATABASE_VERSION = 13
        const val DATABASE_NAME = "collection_database"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * A hollow migration from version 9 to 13.
         * This is for production devices that are already on a correct schema but with an old version number.
         * The schema already contains the parentId column, so no SQL commands are needed.
         */
        val MIGRATION_9_13: Migration = object : Migration(9, 13) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // No schema changes needed. The schema from version 9 was already correct.
                // This migration just bumps the version number.
            }
        }

        // The 12->13 migration is for development devices that might have been left in a broken state.
        val MIGRATION_12_13: Migration = object : Migration(12, 13) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE locations ADD COLUMN parentId INTEGER DEFAULT NULL")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                // Provide a safe upgrade path for production (9->13) and a fix for dev devices (12->13)
                .addMigrations(MIGRATION_9_13, MIGRATION_12_13)
                .build()
                INSTANCE = instance
                instance
            }
        }

        fun closeInstance() {
            INSTANCE?.close()
            INSTANCE = null
        }
    }
}
