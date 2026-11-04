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
import com.chefinventory.app.dao.OpenFoodFactsDao
import com.chefinventory.app.dao.RecipeDao
import com.chefinventory.app.model.Ingredient
import com.chefinventory.app.model.Location
import com.chefinventory.app.model.OpenFoodFactsProduct
import com.chefinventory.app.model.OpenFoodFactsTaxonomy
import com.chefinventory.app.model.Recipe
import com.chefinventory.app.model.RecipeIngredient

@Database(
    entities = [
        Location::class,
        Ingredient::class,
        Recipe::class,
        RecipeIngredient::class,
        OpenFoodFactsProduct::class,
        OpenFoodFactsTaxonomy::class
    ],
    version = 37,
    exportSchema = false
)
@TypeConverters(com.chefinventory.app.data.TypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun locationDao(): LocationDao
    abstract fun ingredientDao(): IngredientDao
    abstract fun recipeDao(): RecipeDao
    abstract fun openFoodFactsDao(): OpenFoodFactsDao

    companion object {
        const val DATABASE_VERSION: Int = 37
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
                db.execSQL("ALTER TABLE ingredients ADD COLUMN stockPolicy TEXT NOT NULL DEFAULT 'PERMANENT'")
            }
        }

        private val MIGRATION_24_25 = object : Migration(24, 25) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE ingredients ADD COLUMN category TEXT")
            }
        }

        private val MIGRATION_25_26 = object : Migration(25, 26) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE ingredients ADD COLUMN description TEXT")
            }
        }

        private val MIGRATION_26_27 = object : Migration(26, 27) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE ingredients ADD COLUMN ocrText TEXT")
            }
        }

        private val MIGRATION_27_28 = object : Migration(27, 28) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE recipes ADD COLUMN category TEXT")
            }
        }

        private val MIGRATION_28_29 = object : Migration(28, 29) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE recipes ADD COLUMN description TEXT")
            }
        }

        private val MIGRATION_29_30 = object : Migration(29, 30) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE recipes ADD COLUMN isFavorite INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_30_31 = object : Migration(30, 31) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE recipes ADD COLUMN prepTimeMinutes INTEGER")
                db.execSQL("ALTER TABLE recipes ADD COLUMN cookTimeMinutes INTEGER")
            }
        }

        private val MIGRATION_31_32 = object : Migration(31, 32) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE recipes ADD COLUMN servings INTEGER")
            }
        }

        private val MIGRATION_32_33 = object : Migration(32, 33) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE recipes ADD COLUMN imageUrl TEXT")
            }
        }

        val MIGRATION_33_34 = object : Migration(33, 34) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS openfoodfacts_products (
                        barcode TEXT NOT NULL PRIMARY KEY,
                        productName TEXT,
                        brand TEXT,
                        quantityStr TEXT,
                        nutriScore TEXT,
                        imageUrl TEXT,
                        ingredientsText TEXT,
                        timestampAdded INTEGER NOT NULL
                    )
                """.trimIndent())

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS openfoodfacts_taxonomy (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        cleanName TEXT NOT NULL,
                        category TEXT,
                        categoryCode INTEGER NOT NULL DEFAULT 10,
                        synonyms TEXT,
                        averageWeightStr TEXT,
                        seasonStr TEXT,
                        carbonFootprintStr TEXT,
                        ciqualName TEXT,
                        ecobalyseCategory TEXT,
                        parentsStr TEXT,
                        childrenStr TEXT
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_34_35 = object : Migration(34, 35) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE openfoodfacts_taxonomy ADD COLUMN averageWeightStr TEXT")
                db.execSQL("ALTER TABLE openfoodfacts_taxonomy ADD COLUMN seasonStr TEXT")
                db.execSQL("ALTER TABLE openfoodfacts_taxonomy ADD COLUMN carbonFootprintStr TEXT")
                db.execSQL("ALTER TABLE openfoodfacts_taxonomy ADD COLUMN ciqualName TEXT")
                db.execSQL("ALTER TABLE openfoodfacts_taxonomy ADD COLUMN ecobalyseCategory TEXT")
            }
        }

        val MIGRATION_35_36 = object : Migration(35, 36) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE openfoodfacts_taxonomy ADD COLUMN parentsStr TEXT")
                db.execSQL("ALTER TABLE openfoodfacts_taxonomy ADD COLUMN childrenStr TEXT")
            }
        }

        val MIGRATION_36_37 = object : Migration(36, 37) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE openfoodfacts_taxonomy ADD COLUMN categoryCode INTEGER NOT NULL DEFAULT 10")
            }
        }

        private val DB_CALLBACK = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                db.execSQL("INSERT OR IGNORE INTO locations (id, name, type) VALUES (1, 'Frigo', 'INGREDIENT')")
                db.execSQL("INSERT OR IGNORE INTO locations (id, name, type) VALUES (2, 'Placard', 'INGREDIENT')")
                db.execSQL("INSERT OR IGNORE INTO locations (id, name, type) VALUES (3, 'Surgélateur', 'INGREDIENT')")
                db.execSQL("INSERT OR IGNORE INTO locations (id, name, type) VALUES (4, 'Cave', 'INGREDIENT')")

                db.execSQL("INSERT OR IGNORE INTO locations (id, name, type) VALUES (5, 'Entrée', 'RECIPE')")
                db.execSQL("INSERT OR IGNORE INTO locations (id, name, type) VALUES (6, 'Plat principal', 'RECIPE')")
                db.execSQL("INSERT OR IGNORE INTO locations (id, name, type) VALUES (7, 'Dessert', 'RECIPE')")
                db.execSQL("INSERT OR IGNORE INTO locations (id, name, type) VALUES (8, 'Boisson', 'RECIPE')")
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
                    MIGRATION_30_31, MIGRATION_31_32, MIGRATION_32_33,
                    MIGRATION_33_34, MIGRATION_34_35, MIGRATION_35_36,
                    MIGRATION_36_37
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
