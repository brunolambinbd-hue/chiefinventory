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
         * A robust migration from version 9 to 13.
         * It handles the complex case where the database contains both `parentId` and `parentLocationId`,
         * along with unexpected foreign keys and indices.
         *
         * Strategy: Create a new clean table, copy/merge data, and swap tables.
         */
        val MIGRATION_9_13: Migration = object : Migration(9, 13) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 1. Create the new table with the correct schema (as expected by Room)
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `locations_new` (" +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`name` TEXT NOT NULL, " +
                            "`parentId` INTEGER)"
                )

                // 2. Copy data from the old table to the new one.
                // We use COALESCE to merge data: if `parentId` is null, we take `parentLocationId`.
                // This ensures we preserve relationships regardless of which column was used.
                // We check if the source columns exist to build the correct INSERT statement.
                val cursor = database.query("PRAGMA table_info(locations)")
                val columns = mutableListOf<String>()
                while (cursor.moveToNext()) {
                    columns.add(cursor.getString(cursor.getColumnIndex("name")))
                }
                cursor.close()

                val hasParentId = columns.contains("parentId")
                val hasParentLocationId = columns.contains("parentLocationId")

                val parentSource = when {
                    hasParentId && hasParentLocationId -> "COALESCE(parentId, parentLocationId)"
                    hasParentId -> "parentId"
                    hasParentLocationId -> "parentLocationId"
                    else -> "NULL"
                }

                database.execSQL(
                    "INSERT INTO `locations_new` (id, name, parentId) " +
                            "SELECT id, name, $parentSource FROM locations"
                )

                // 3. Drop the old table
                database.execSQL("DROP TABLE locations")

                // 4. Rename the new table to the original name
                database.execSQL("ALTER TABLE locations_new RENAME TO locations")

                // 5. Create the required index
                database.execSQL("CREATE INDEX IF NOT EXISTS index_locations_parentId ON locations(parentId)")
            }
        }

        // The 12->13 migration is for development devices that might have been left in a broken state.
        val MIGRATION_12_13: Migration = object : Migration(12, 13) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE locations ADD COLUMN parentId INTEGER DEFAULT NULL")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_locations_parentId ON locations(parentId)")
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
