package com.example.parabdcollector.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.parabdcollector.dao.CollectionDao
import com.example.parabdcollector.model.CollectionItem

@Database(entities = [CollectionItem::class], version = 2, exportSchema = false) // Version incrémentée à 2
abstract class AppDatabase : RoomDatabase() {

    abstract fun collectionDao(): CollectionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // On définit la migration de la version 1 à 2
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE collection_items ADD COLUMN isPossessed INTEGER NOT NULL DEFAULT 1")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "collection_database"
                )
                .addMigrations(MIGRATION_1_2) // On ajoute la migration au builder
                .allowMainThreadQueries()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}