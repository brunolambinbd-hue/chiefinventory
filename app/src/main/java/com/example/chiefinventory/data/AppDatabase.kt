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
    version = 27,
    exportSchema = false
)
@TypeConverters(com.example.chiefinventory.data.TypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun collectionDao(): CollectionDao
    abstract fun locationDao(): LocationDao
    abstract fun ingredientDao(): IngredientDao
    abstract fun recipeDao(): RecipeDao

    companion object {
        const val DATABASE_VERSION: Int = 27
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

        private val MIGRATION_20_21 = object : Migration(20, 21) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE ingredients ADD COLUMN supplementalInfo TEXT")
            }
        }

        private val MIGRATION_21_22 = object : Migration(21, 22) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE recipe_ingredients ADD COLUMN supplementalInfo TEXT")
            }
        }

        private val MIGRATION_22_23 = object : Migration(22, 23) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE recipe_ingredients_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        recipeId INTEGER NOT NULL,
                        ingredientName TEXT NOT NULL,
                        quantityRequired REAL,
                        unit TEXT,
                        supplementalInfo TEXT,
                        FOREIGN KEY(recipeId) REFERENCES recipes(id) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("""
                    INSERT INTO recipe_ingredients_new (recipeId, ingredientName, quantityRequired, unit, supplementalInfo)
                    SELECT recipeId, ingredientName, quantityRequired, unit, supplementalInfo FROM recipe_ingredients
                """.trimIndent())
                db.execSQL("DROP TABLE recipe_ingredients")
                db.execSQL("ALTER TABLE recipe_ingredients_new RENAME TO recipe_ingredients")
                db.execSQL("CREATE INDEX index_recipe_ingredients_recipeId ON recipe_ingredients(recipeId)")
            }
        }

        private val MIGRATION_23_24 = object : Migration(23, 24) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE recipes ADD COLUMN restingTimeMinutes INTEGER")
            }
        }

        private val MIGRATION_24_25 = object : Migration(24, 25) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE recipes ADD COLUMN kcalPerServing INTEGER")
                db.execSQL("ALTER TABLE recipes ADD COLUMN difficulty TEXT")
            }
        }

        /**
         * Migration 25 -> 26: Ajout de locationId à recipe_ingredients avec ForeignKey.
         * SQLite ne supporte pas l'ajout de FK via ALTER TABLE, on doit recréer la table.
         */
        private val MIGRATION_25_26 = object : Migration(25, 26) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE recipe_ingredients_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        recipeId INTEGER NOT NULL,
                        ingredientName TEXT NOT NULL,
                        quantityRequired REAL,
                        unit TEXT,
                        supplementalInfo TEXT,
                        locationId INTEGER,
                        FOREIGN KEY(recipeId) REFERENCES recipes(id) ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY(locationId) REFERENCES locations(id) ON UPDATE NO ACTION ON DELETE SET_NULL
                    )
                """.trimIndent())
                
                db.execSQL("""
                    INSERT INTO recipe_ingredients_new (id, recipeId, ingredientName, quantityRequired, unit, supplementalInfo, locationId)
                    SELECT id, recipeId, ingredientName, quantityRequired, unit, supplementalInfo, NULL FROM recipe_ingredients
                """.trimIndent())
                
                db.execSQL("DROP TABLE recipe_ingredients")
                db.execSQL("ALTER TABLE recipe_ingredients_new RENAME TO recipe_ingredients")
                
                db.execSQL("CREATE INDEX IF NOT EXISTS index_recipe_ingredients_recipeId ON recipe_ingredients(recipeId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_recipe_ingredients_locationId ON recipe_ingredients(locationId)")
            }
        }

        /**
         * Migration 26 -> 27: Ajout de nutritionInfo à la table recipes.
         */
        private val MIGRATION_26_27 = object : Migration(26, 27) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE recipes ADD COLUMN nutritionInfo TEXT")
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
                .addMigrations(
                    MIGRATION_18_19, MIGRATION_19_20, MIGRATION_20_21, 
                    MIGRATION_21_22, MIGRATION_22_23, MIGRATION_23_24, 
                    MIGRATION_24_25, MIGRATION_25_26, MIGRATION_26_27
                )
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
