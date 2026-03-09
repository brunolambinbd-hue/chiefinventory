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
    version = 23, // Passage à 23 pour forcer la réparation de la structure
    exportSchema = false
)
@TypeConverters(com.example.chiefinventory.data.TypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun collectionDao(): CollectionDao
    abstract fun locationDao(): LocationDao
    abstract fun ingredientDao(): IngredientDao
    abstract fun recipeDao(): RecipeDao

    companion object {
        const val DATABASE_VERSION: Int = 23
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

        // Gardée pour compatibilité ascendante (21 → 22)
        private val MIGRATION_21_22 = object : Migration(21, 22) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE recipe_ingredients ADD COLUMN supplementalInfo TEXT")
            }
        }

        // Migration robuste pour réparer la structure de recipe_ingredients (22 → 23)
        private val MIGRATION_22_23 = object : Migration(22, 23) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Créer la nouvelle table avec la structure EXACTE attendue (id, FK, Index)
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

                // 2. Copier les données (on sait que supplementalInfo existe déjà, car on vient de la v22)
                db.execSQL("""
                    INSERT INTO recipe_ingredients_new (recipeId, ingredientName, quantityRequired, unit, supplementalInfo)
                    SELECT recipeId, ingredientName, quantityRequired, unit, supplementalInfo FROM recipe_ingredients
                """.trimIndent())

                // 3. Remplacer l'ancienne table
                db.execSQL("DROP TABLE recipe_ingredients")
                db.execSQL("ALTER TABLE recipe_ingredients_new RENAME TO recipe_ingredients")

                // 4. Recréer l'index
                db.execSQL("CREATE INDEX index_recipe_ingredients_recipeId ON recipe_ingredients(recipeId)")
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
                .addMigrations(MIGRATION_18_19, MIGRATION_19_20, MIGRATION_20_21, MIGRATION_21_22, MIGRATION_22_23)
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
