package com.example.chiefinventory.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.chiefinventory.dao.CollectionDao
import com.example.chiefinventory.dao.IngredientDao
import com.example.chiefinventory.dao.LocationDao
import com.example.chiefinventory.dao.RecipeDao
import com.example.chiefinventory.model.CollectionItem
import com.example.chiefinventory.model.Ingredient
import com.example.chiefinventory.model.Location
import com.example.chiefinventory.model.Recipe
import com.example.chiefinventory.model.RecipeIngredient

@Database(
    entities = [
        CollectionItem::class,
        Location::class,
        Ingredient::class,
        Recipe::class,
        RecipeIngredient::class
    ],
    version = 20, // Incremented from 19 to 20
    exportSchema = false
)
@TypeConverters(com.example.chiefinventory.data.TypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun collectionDao(): CollectionDao
    abstract fun locationDao(): LocationDao
    abstract fun ingredientDao(): IngredientDao
    abstract fun recipeDao(): RecipeDao

    companion object {
        const val DATABASE_VERSION: Int = 20
        const val DATABASE_NAME: String = "chiefinventory_database"

        private val MIGRATION_18_19 = object : Migration(18, 19) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE recipes ADD COLUMN wineRecommendation TEXT")
            }
        }

        private val MIGRATION_19_20 = object : Migration(19, 20) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE recipes ADD COLUMN source TEXT")
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                .addMigrations(MIGRATION_18_19, MIGRATION_19_20)
                .fallbackToDestructiveMigration()
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
