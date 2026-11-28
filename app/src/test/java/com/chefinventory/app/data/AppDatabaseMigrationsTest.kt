package com.chefinventory.app.data

import android.content.Context
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Suite de tests unitaires exécutant l'ensemble des 20 migrations Room et le Callback d'initialisation de [AppDatabase].
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AppDatabaseMigrationsTest {

    private lateinit var context: Context
    private lateinit var helper: SupportSQLiteOpenHelper

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        val config = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name("test_migrations.db")
            .callback(object : SupportSQLiteOpenHelper.Callback(1) {
                override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                    // Create base tables needed for early migrations (18 -> 38)
                    db.execSQL("CREATE TABLE IF NOT EXISTS locations (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, type TEXT NOT NULL)")
                    db.execSQL("CREATE TABLE IF NOT EXISTS recipes (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, title TEXT NOT NULL)")
                    db.execSQL("CREATE TABLE IF NOT EXISTS ingredients (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL)")
                    db.execSQL("CREATE TABLE IF NOT EXISTS recipe_ingredients (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, recipeId INTEGER NOT NULL, ingredientName TEXT NOT NULL, quantityRequired REAL, unit TEXT)")
                }

                override fun onUpgrade(db: androidx.sqlite.db.SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
            })
            .build()
        helper = FrameworkSQLiteOpenHelperFactory().create(config)
    }

    @After
    fun tearDown() {
        helper.close()
        context.deleteDatabase("test_migrations.db")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testAllMigrationsAndDbCallbackExecution() {
        val db = helper.writableDatabase

        // Execute all 20 migrations in sequence
        for (migration in AppDatabase.ALL_MIGRATIONS) {
            migration.migrate(db)
        }

        // Execute DB_CALLBACK.onCreate
        AppDatabase.DB_CALLBACK.onCreate(db)

        // Verify locations created by DB_CALLBACK
        val cursor = db.query("SELECT COUNT(*) FROM locations")
        if (cursor.moveToFirst()) {
            val count = cursor.getInt(0)
            assertWithMessage("Le Callback d'initialisation DB_CALLBACK doit insérer 8 emplacements de base")
                .that(count)
                .isEqualTo(8)
        }
        cursor.close()

        // Verify MIGRATION_36_37 added categoryCode column to openfoodfacts_taxonomy
        val cursorTax = db.query("PRAGMA table_info(openfoodfacts_taxonomy)")
        val taxColumns = mutableListOf<String>()
        while (cursorTax.moveToNext()) {
            val colNameIdx = cursorTax.getColumnIndex("name")
            if (colNameIdx >= 0) {
                taxColumns.add(cursorTax.getString(colNameIdx))
            }
        }
        cursorTax.close()
        assertThat(taxColumns).contains("categoryCode")

        // Verify MIGRATION_37_38 added nutriScore column to recipes
        val cursorRecipe = db.query("PRAGMA table_info(recipes)")
        val recipeColumns = mutableListOf<String>()
        while (cursorRecipe.moveToNext()) {
            val colNameIdx = cursorRecipe.getColumnIndex("name")
            if (colNameIdx >= 0) {
                recipeColumns.add(cursorRecipe.getString(colNameIdx))
            }
        }
        cursorRecipe.close()
        assertThat(recipeColumns).contains("nutriScore")

        assertThat(AppDatabase.DATABASE_VERSION).isEqualTo(38)
        assertThat(AppDatabase.DATABASE_NAME).isEqualTo("chiefinventory_database")
    }
}
