package com.example.parabdcollector

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.parabdcollector.dao.CollectionDao
import com.example.parabdcollector.db.AppDatabase
import com.example.parabdcollector.model.CollectionItem
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class DatabaseTest {

    // On ajoute cette règle pour forcer les opérations à s'exécuter sur le même thread.
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var db: AppDatabase
    private lateinit var dao: CollectionDao

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.collectionDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun writeAndReadItem() = runBlocking {
        val item = CollectionItem(id = 1, titre = "Test Item", univers = "Test Universe", fabricant = null, annee = null, categorie = null, materiau = null, tirage = null, dimensions = null, prixAchat = null, valeurEstimee = null, lieuAchat = null, notes = null, imageUri = null, localisation = null)
        dao.insert(item)
        val items = dao.getAll().getOrAwaitValue()
        assertEquals(items.first().titre, "Test Item")
    }
}