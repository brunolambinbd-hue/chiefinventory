package com.example.chiefinventory.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Suite de tests unitaires Robolectric de Type B assurant la couverture de [AppDatabase.Companion]
 * et de l'instanciation du Singleton SQLite Room.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AppDatabaseRobolectricTypeBTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        AppDatabase.closeInstance()
    }

    @After
    fun tearDown() {
        AppDatabase.closeInstance()
    }

    @Test
    fun `getDatabase should return singleton instance of AppDatabase`() {
        val db1 = AppDatabase.getDatabase(context)
        val db2 = AppDatabase.getDatabase(context)

        assertThat(db1).isNotNull()
        assertThat(db1).isSameInstanceAs(db2)
        assertThat(db1.collectionDao()).isNotNull()
        assertThat(db1.ingredientDao()).isNotNull()
        assertThat(db1.recipeDao()).isNotNull()
        assertThat(db1.locationDao()).isNotNull()
    }

    @Test
    fun `closeInstance should close database and reset singleton INSTANCE`() {
        val db = AppDatabase.getDatabase(context)
        assertThat(db).isNotNull()

        AppDatabase.closeInstance()

        val newDb = AppDatabase.getDatabase(context)
        assertThat(newDb).isNotSameInstanceAs(db)
    }
}
