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
 * Suite de tests unitaires de Type B vérifiant la migration Room `MIGRATION_35_36` de [AppDatabase].
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AppDatabaseMigration35To36Test {

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testMigration35To36_ExecutesAlterTableStatements() {
        val mockDb = mock<SupportSQLiteDatabase>()

        AppDatabase.MIGRATION_35_36.migrate(mockDb)

        verify(mockDb).execSQL(org.mockito.kotlin.check { sql ->
            assertThat(sql).contains("ALTER TABLE openfoodfacts_taxonomy ADD COLUMN parentsStr TEXT")
        })

        verify(mockDb).execSQL(org.mockito.kotlin.check { sql ->
            assertThat(sql).contains("ALTER TABLE openfoodfacts_taxonomy ADD COLUMN childrenStr TEXT")
        })
    }
}
