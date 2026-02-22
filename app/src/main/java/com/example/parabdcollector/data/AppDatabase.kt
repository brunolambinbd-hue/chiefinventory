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

@Database(entities = [CollectionItem::class, Location::class], version = 14, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun collectionDao(): CollectionDao
    abstract fun locationDao(): LocationDao

    companion object {
        const val DATABASE_VERSION: Int = 14
        const val DATABASE_NAME: String = "collection_database"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_9_13: Migration = object : Migration(9, 13) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `locations_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `parentId` INTEGER)")
                val cursor = db.query("PRAGMA table_info(locations)")
                val columns = mutableListOf<String>()
                val nameIndex = cursor.getColumnIndexOrThrow("name")
                while (cursor.moveToNext()) { columns.add(cursor.getString(nameIndex)) }
                cursor.close()
                val parentSource = when {
                    columns.contains("parentId") && columns.contains("parentLocationId") -> "COALESCE(parentId, parentLocationId)"
                    columns.contains("parentId") -> "parentId"
                    columns.contains("parentLocationId") -> "parentLocationId"
                    else -> "NULL"
                }
                db.execSQL("INSERT INTO `locations_new` (id, name, parentId) SELECT id, name, $parentSource FROM locations")
                db.execSQL("DROP TABLE locations")
                db.execSQL("ALTER TABLE locations_new RENAME TO locations")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_locations_parentId ON locations(parentId)")
            }
        }

        val MIGRATION_12_13: Migration = object : Migration(12, 13) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE locations ADD COLUMN parentId INTEGER DEFAULT NULL")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_locations_parentId ON locations(parentId)")
            }
        }

        /**
         * Migration from 13 to 14: Adds the `updatedAt` column to the `collection_items` table.
         */
        val MIGRATION_13_14: Migration = object : Migration(13, 14) {
            override fun migrate(db: SupportSQLiteDatabase) {
                val now = System.currentTimeMillis()
                db.execSQL("ALTER TABLE collection_items ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT $now")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                .addMigrations(MIGRATION_9_13, MIGRATION_12_13, MIGRATION_13_14)
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
