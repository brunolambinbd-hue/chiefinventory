package com.example.parabdcollector.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.parabdcollector.dao.CollectionDao
import com.example.parabdcollector.dao.LocationDao
import com.example.parabdcollector.model.CollectionItem
import com.example.parabdcollector.model.Location

/**
 * The main Room database class for the application.
 *
 * This abstract class defines the database configuration and serves as the main access point
 * to the persisted data. It lists the entities (tables) and provides abstract methods for each DAO.
 */
@Database(entities = [CollectionItem::class, Location::class], version = 9, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    /**
     * Provides access to the Data Access Object for [CollectionItem]s.
     * @return The [CollectionDao] instance.
     */
    abstract fun collectionDao(): CollectionDao

    /**
     * Provides access to the Data Access Object for [Location]s.
     * @return The [LocationDao] instance.
     */
    abstract fun locationDao(): LocationDao

    companion object {
        /**
         * The name of the database file.
         */
        const val DATABASE_NAME = "collection_database"

        /**
         * The current version of the database schema.
         * This must be incremented when the schema changes.
         */
        const val DATABASE_VERSION = 9

        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Gets the singleton instance of the [AppDatabase].
         *
         * This method uses a synchronized block to ensure thread safety, guaranteeing that only one
         * instance of the database is ever created.
         *
         * @param context The application context.
         * @return The singleton [AppDatabase] instance.
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }

        /**
         * Closes the database connection and invalidates the singleton instance.
         *
         * This is a critical step to perform before any operation that replaces the database file,
         * such as a restore from backup. It ensures that the app "forgets" the old database
         * and is forced to create a fresh connection on next access.
         */
        fun closeInstance() {
            INSTANCE?.close()
            INSTANCE = null
        }
    }
}
