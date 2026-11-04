package com.chefinventory.app.data

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Suite de tests unitaires de Type B vérifiant la migration Room `MIGRATION_33_34` de [AppDatabase].
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AppDatabaseMigration33To34Test {

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testMigration33To34_ExecutesCreateTableStatements() {
        val mockDb = mock<SupportSQLiteDatabase>()

        AppDatabase.MIGRATION_33_34.migrate(mockDb)

        verify(mockDb).execSQL(org.mockito.kotlin.check { sql ->
            assertThat(sql).contains("CREATE TABLE IF NOT EXISTS openfoodfacts_products")
        })

        verify(mockDb).execSQL(org.mockito.kotlin.check { sql ->
            assertThat(sql).contains("CREATE TABLE IF NOT EXISTS openfoodfacts_taxonomy")
        })
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testOpenFoodFactsDao_AccessFromDatabaseInstance() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = AppDatabase.getDatabase(context)

        val dao = db.openFoodFactsDao()
        assertThat(dao).isNotNull()

        AppDatabase.closeInstance()
    }
}
