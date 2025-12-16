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

// Set the definitive new version to 13 to fix schema inconsistencies.
@Database(entities = [CollectionItem::class, Location::class], version = 13, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun collectionDao(): CollectionDao
    abstract fun locationDao(): LocationDao

    companion object {
        const val DATABASE_VERSION = 13
        const val DATABASE_NAME = "collection_database"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        private fun performRepairMigration(database: SupportSQLiteDatabase) {
            // This migration rebuilds the locations table to fix any schema inconsistencies.
            // This will reset the locations hierarchy but preserve all collection items.
            database.execSQL("DROP TABLE IF EXISTS locations")
            database.execSQL("CREATE TABLE `locations` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `parentId` INTEGER)")
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_locations_parentId` ON `locations` (`parentId`)")
        }

        val MIGRATION_9_13: Migration = object : Migration(9, 13) {
            override fun migrate(database: SupportSQLiteDatabase) = performRepairMigration(database)
        }
        val MIGRATION_10_13: Migration = object : Migration(10, 13) {
            override fun migrate(database: SupportSQLiteDatabase) = performRepairMigration(database)
        }
        val MIGRATION_11_13: Migration = object : Migration(11, 13) {
            override fun migrate(database: SupportSQLiteDatabase) = performRepairMigration(database)
        }
        val MIGRATION_12_13: Migration = object : Migration(12, 13) {
            override fun migrate(database: SupportSQLiteDatabase) = performRepairMigration(database)
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                // This provides a recovery path from any recent version to the new stable version 13.
                // It will reset the locations hierarchy but preserve all collection items.
                .addMigrations(MIGRATION_9_13, MIGRATION_10_13, MIGRATION_11_13, MIGRATION_12_13)
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
