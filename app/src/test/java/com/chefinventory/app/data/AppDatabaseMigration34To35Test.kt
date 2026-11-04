package com.chefinventory.app.data

import androidx.sqlite.db.SupportSQLiteDatabase
import com.google.common.truth.Truth.assertThat
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Suite de tests unitaires de Type B vérifiant la migration Room `MIGRATION_34_35` de [AppDatabase].
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AppDatabaseMigration34To35Test {

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testMigration34To35_ExecutesAlterTableStatements() {
        val mockDb = mock<SupportSQLiteDatabase>()

        AppDatabase.MIGRATION_34_35.migrate(mockDb)

        verify(mockDb).execSQL(org.mockito.kotlin.check { sql ->
            assertThat(sql).contains("ALTER TABLE openfoodfacts_taxonomy ADD COLUMN averageWeightStr TEXT")
        })

        verify(mockDb).execSQL(org.mockito.kotlin.check { sql ->
            assertThat(sql).contains("ALTER TABLE openfoodfacts_taxonomy ADD COLUMN seasonStr TEXT")
        })

        verify(mockDb).execSQL(org.mockito.kotlin.check { sql ->
            assertThat(sql).contains("ALTER TABLE openfoodfacts_taxonomy ADD COLUMN carbonFootprintStr TEXT")
        })

        verify(mockDb).execSQL(org.mockito.kotlin.check { sql ->
            assertThat(sql).contains("ALTER TABLE openfoodfacts_taxonomy ADD COLUMN ciqualName TEXT")
        })

        verify(mockDb).execSQL(org.mockito.kotlin.check { sql ->
            assertThat(sql).contains("ALTER TABLE openfoodfacts_taxonomy ADD COLUMN ecobalyseCategory TEXT")
        })
    }
}
