package com.example.parabdcollector.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.parabdcollector.dao.CollectionDao
import com.example.parabdcollector.dao.ImportSessionDao
import com.example.parabdcollector.dao.LocationDao
import com.example.parabdcollector.model.CollectionItem
import com.example.parabdcollector.model.ImportSession
import com.example.parabdcollector.model.Location

@Database(entities = [CollectionItem::class, Location::class, ImportSession::class], version = 16, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun collectionDao(): CollectionDao
    abstract fun locationDao(): LocationDao
    abstract fun importSessionDao(): ImportSessionDao

    companion object {
        const val DATABASE_VERSION: Int = 16
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

        /**
         * Migration from 14 to 15: Reorder columns in `collection_items`.
         * New order puts imageEmbedding at the very end, and imageUri just before it.
         */
        val MIGRATION_14_15: Migration = object : Migration(14, 15) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Create the new table with the desired column order
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `collection_items_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `remoteId` INTEGER, 
                        `titre` TEXT NOT NULL, 
                        `editeur` TEXT, 
                        `annee` INTEGER, 
                        `mois` INTEGER, 
                        `categorie` TEXT, 
                        `superCategorie` TEXT, 
                        `materiau` TEXT, 
                        `tirage` TEXT, 
                        `dimensions` TEXT, 
                        `prixAchat` REAL, 
                        `valeurEstimee` REAL, 
                        `lieuAchat` TEXT, 
                        `description` TEXT, 
                        `locationId` INTEGER, 
                        `isPossessed` INTEGER NOT NULL, 
                        `updatedAt` INTEGER NOT NULL, 
                        `imageUri` TEXT, 
                        `imageEmbedding` BLOB
                    )
                """.trimIndent())

                // 2. Copy data from the old table to the new one
                db.execSQL("""
                    INSERT INTO `collection_items_new` (
                        id, remoteId, titre, editeur, annee, mois, categorie, superCategorie, 
                        materiau, tirage, dimensions, prixAchat, valeurEstimee, lieuAchat, 
                        description, locationId, isPossessed, updatedAt, imageUri, imageEmbedding
                    )
                    SELECT 
                        id, remoteId, titre, editeur, annee, mois, categorie, superCategorie, 
                        materiau, tirage, dimensions, prixAchat, valeurEstimee, lieuAchat, 
                        description, locationId, isPossessed, updatedAt, imageUri, imageEmbedding
                    FROM collection_items
                """.trimIndent())

                // 3. Drop the old table
                db.execSQL("DROP TABLE collection_items")

                // 4. Rename the new table to the original name
                db.execSQL("ALTER TABLE collection_items_new RENAME TO collection_items")

                // 5. Re-create the index
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_collection_items_remoteId` ON `collection_items` (`remoteId`)")
            }
        }

        /**
         * Migration from 15 to 16: Adds the `import_sessions` table and the `lastSessionId` column to `collection_items`.
         */
        val MIGRATION_15_16: Migration = object : Migration(15, 16) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `import_sessions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `timestamp` INTEGER NOT NULL, `fileName` TEXT NOT NULL, `itemsAdded` INTEGER NOT NULL, `itemsUpdated` INTEGER NOT NULL, `status` TEXT NOT NULL)")
                db.execSQL("ALTER TABLE collection_items ADD COLUMN lastSessionId INTEGER DEFAULT NULL")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                .addMigrations(MIGRATION_9_13, MIGRATION_12_13, MIGRATION_13_14, MIGRATION_14_15, MIGRATION_15_16)
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
