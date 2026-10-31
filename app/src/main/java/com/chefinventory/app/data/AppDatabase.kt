package com.chefinventory.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.chefinventory.app.dao.IngredientDao
import com.chefinventory.app.dao.LocationDao
import com.chefinventory.app.dao.RecipeDao
import com.chefinventory.app.model.Ingredient
import com.chefinventory.app.model.Location
import com.chefinventory.app.model.Recipe
import com.chefinventory.app.model.RecipeIngredient

@Database(
    entities = [
        Location::class,
        Ingredient::class,
        Recipe::class,
        RecipeIngredient::class
    ],
    version = 33,
    exportSchema = false
)
@TypeConverters(com.chefinventory.app.data.TypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun locationDao(): LocationDao
    abstract fun ingredientDao(): IngredientDao
    abstract fun recipeDao(): RecipeDao

    companion object {
        const val DATABASE_VERSION: Int = 33
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
            }
        }

        private val MIGRATION_23_24 = object : Migration(23, 24) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE ingredients ADD COLUMN minThreshold REAL")
                db.execSQL("ALTER TABLE ingredients ADD COLUMN packSize REAL NOT NULL DEFAULT 1.0")
            }
        }

        private val MIGRATION_24_25 = object : Migration(24, 25) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE recipes ADD COLUMN kcalPerServing INTEGER")
            }
        }

        private val MIGRATION_25_26 = object : Migration(25, 26) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE recipes ADD COLUMN difficulty TEXT")
            }
        }

        private val MIGRATION_26_27 = object : Migration(26, 27) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE recipes ADD COLUMN nutritionInfo TEXT")
            }
        }

        private val MIGRATION_27_28 = object : Migration(27, 28) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE recipes ADD COLUMN priceRating TEXT")
            }
        }

        private val MIGRATION_28_29 = object : Migration(28, 29) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE recipes ADD COLUMN locationId INTEGER")
            }
        }

        private val MIGRATION_29_30 = object : Migration(29, 30) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE recipes ADD COLUMN createdAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE recipes ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_30_31 = object : Migration(30, 31) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Migration V31
            }
        }

        private val MIGRATION_31_32 = object : Migration(31, 32) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Migration V32
            }
        }

        private val MIGRATION_32_33 = object : Migration(32, 33) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Migration Pro : Suppression de imageEmbedding sans perte de données utilisateur
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS ingredients_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        quantity REAL,
                        unit TEXT,
                        category TEXT,
                        expirationDate INTEGER,
                        description TEXT,
                        supplementalInfo TEXT,
                        imageUri TEXT,
                        ocrText TEXT,
                        locationId INTEGER,
                        stockPolicy TEXT NOT NULL,
                        minThreshold REAL,
                        packSize REAL NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                """.trimIndent())

                db.execSQL("""
                    INSERT INTO ingredients_new (id, name, quantity, unit, category, expirationDate, description, supplementalInfo, imageUri, ocrText, locationId, stockPolicy, minThreshold, packSize, updatedAt)
                    SELECT id, name, quantity, unit, category, expirationDate, description, supplementalInfo, imageUri, ocrText, locationId, stockPolicy, minThreshold, packSize, updatedAt FROM ingredients
                """.trimIndent())

                db.execSQL("DROP TABLE ingredients")
                db.execSQL("ALTER TABLE ingredients_new RENAME TO ingredients")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_ingredients_locationId ON ingredients(locationId)")
            }
        }

        private val DB_CALLBACK = object : Callback() {
            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                // 1. Initialisation des emplacements par défaut s'ils n'existent pas
                db.execSQL("INSERT OR IGNORE INTO locations (id, name, type) VALUES (1, 'Frigo', 'INGREDIENT')")
                db.execSQL("INSERT OR IGNORE INTO locations (id, name, type) VALUES (2, 'Placard', 'INGREDIENT')")
                db.execSQL("INSERT OR IGNORE INTO locations (id, name, type) VALUES (3, 'Surgélateur', 'INGREDIENT')")
                db.execSQL("INSERT OR IGNORE INTO locations (id, name, type) VALUES (4, 'Cave', 'INGREDIENT')")

                db.execSQL("INSERT OR IGNORE INTO locations (id, name, type) VALUES (5, 'Entrée', 'RECIPE')")
                db.execSQL("INSERT OR IGNORE INTO locations (id, name, type) VALUES (6, 'Plat principal', 'RECIPE')")
                db.execSQL("INSERT OR IGNORE INTO locations (id, name, type) VALUES (7, 'Dessert', 'RECIPE')")
                db.execSQL("INSERT OR IGNORE INTO locations (id, name, type) VALUES (8, 'Boisson', 'RECIPE')")

                // 2. Correction automatique des ingrédients enregistrés avec locationId = 1 (Frigo) ou NULL
                db.execSQL("UPDATE ingredients SET locationId = 2 WHERE (category LIKE '%épicerie%' OR category LIKE '%epicerie%' OR category LIKE '%féculents%' OR category LIKE '%feculents%' OR category LIKE '%condiments%' OR category LIKE '%légumes%' OR category LIKE '%legumes%' OR category LIKE '%pâtisserie%' OR category LIKE '%patisserie%') AND (locationId = 1 OR locationId IS NULL)")
                db.execSQL("UPDATE ingredients SET locationId = 3 WHERE (category LIKE '%surgelé%' OR category LIKE '%surgele%' OR category LIKE '%glace%') AND (locationId = 1 OR locationId IS NULL)")
                db.execSQL("UPDATE ingredients SET locationId = 4 WHERE (category LIKE '%boisson%' OR category LIKE '%cave%' OR category LIKE '%vin%') AND (locationId = 1 OR locationId IS NULL)")

                // 3. Correction automatique des recettes mal attribuées à Boisson (ID 8) ou sans locationId
                db.execSQL("UPDATE recipes SET locationId = 7 WHERE (title LIKE '%cake%' OR title LIKE '%gâteau%' OR title LIKE '%gateau%' OR title LIKE '%chocolat%' OR title LIKE '%tarte%' OR title LIKE '%mousse%' OR title LIKE '%crêpe%' OR title LIKE '%crepe%' OR title LIKE '%gaufre%' OR title LIKE '%dessert%' OR title LIKE '%pâtisserie%' OR title LIKE '%patisserie%') AND (locationId = 8 OR locationId IS NULL)")
                db.execSQL("UPDATE recipes SET locationId = 5 WHERE (title LIKE '%entrée%' OR title LIKE '%entree%' OR title LIKE '%salade%' OR title LIKE '%soupe%' OR title LIKE '%velouté%' OR title LIKE '%amuse%') AND (locationId = 8 OR locationId IS NULL)")
                db.execSQL("UPDATE recipes SET locationId = 6 WHERE (title LIKE '%poulet%' OR title LIKE '%boeuf%' OR title LIKE '%bœuf%' OR title LIKE '%porc%' OR title LIKE '%saumon%' OR title LIKE '%pâte%' OR title LIKE '%pate%' OR title LIKE '%risotto%' OR title LIKE '%lasagne%' OR title LIKE '%rôti%' OR title LIKE '%roti%' OR title LIKE '%plat%') AND (locationId = 8 OR locationId IS NULL)")
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
                    MIGRATION_24_25, MIGRATION_25_26, MIGRATION_26_27,
                    MIGRATION_27_28, MIGRATION_28_29, MIGRATION_29_30, 
                    MIGRATION_30_31, MIGRATION_31_32, MIGRATION_32_33
                )
                .addCallback(DB_CALLBACK)
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
