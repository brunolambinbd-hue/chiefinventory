package com.example.parabdcollector.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.parabdcollector.dao.CollectionDao
import com.example.parabdcollector.data.AppDatabase
import com.example.parabdcollector.model.CollectionItem
import com.example.parabdcollector.repo.CollectionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.junit.runner.RunWith
import java.io.File

/**
 * A local copy of the Test Rule for instrumented tests, as they don't share code with unit tests.
 */
@ExperimentalCoroutinesApi
class MainDispatcherRule(
    val testDispatcher: TestDispatcher = UnconfinedTestDispatcher(),
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

/**
 * Instrumented integration tests for the [ImportViewModel].
 */
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class ImportViewModelInstrumentedTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var db: AppDatabase
    private lateinit var dao: CollectionDao
    private lateinit var repository: CollectionRepository
    private lateinit var viewModel: ImportViewModel
    private lateinit var context: Context

    @Before
    fun createDb() {
        context = ApplicationProvider.getApplicationContext<Application>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .setTransactionExecutor(mainDispatcherRule.testDispatcher.asExecutor())
            .setQueryExecutor(mainDispatcherRule.testDispatcher.asExecutor())
            .build()
        dao = db.collectionDao()
        repository = CollectionRepository(dao)
        viewModel = ImportViewModel(context as Application, repository)
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun importCsv_shouldInsertNewItem() = runTest {
        val csvContent = "remoteId;annee;mois;categorie;titre;editeur;description;col7;col8;col9;superCategorie\n" +
                         "1001;2023;5;BD;New Test Item;Test Editor;New Desc;;;;Test Super"
        val csvUri = createTestCsvFile(csvContent)

        val importJob = viewModel.importCsv(csvUri, mainDispatcherRule.testDispatcher)
        importJob.join()

        val newItem = dao.findByRemoteId(1001)
        assertNotNull(newItem)
        assertEquals("New Test Item", newItem?.titre)
    }

    @Test
    fun importCsv_shouldUpdateExistingItem_andKeepEmbedding() = runTest {
        val existingItem = CollectionItem(
            id = 1, remoteId = 1002, titre = "Old Title", editeur = "Old Editor", annee = 2000, categorie = "Old Cat", description = "",
            isPossessed = true, mois = null, superCategorie = null, materiau = null, tirage = null, dimensions = null,
            prixAchat = null, valeurEstimee = null, lieuAchat = null, imageUri = null, 
            imageEmbedding = byteArrayOf(1, 2, 3), // Dummy embedding to skip network call
            locationId = null
        )
        dao.insert(existingItem)
        val csvContent = "remoteId;annee;mois;categorie;titre;editeur;description;col7;col8;col9;superCategorie\n" +
                         "1002;2024;;;Updated Title;;;;;;"
        val csvUri = createTestCsvFile(csvContent)

        val importJob = viewModel.importCsv(csvUri, mainDispatcherRule.testDispatcher)
        importJob.join()

        val updatedItem = dao.findByRemoteId(1002)
        assertEquals("Updated Title", updatedItem?.titre)
        assertNotNull("Embedding should be preserved", updatedItem?.imageEmbedding)
    }

    @Test
    fun importCsv_withMissingSignature_shouldUpdateItemAndComputeSignature() = runTest {
        // GIVEN: an existing item with no embedding
        val existingItem = CollectionItem(
            id = 2, remoteId = 1003, titre = "Old Title No Sig", editeur = "", annee = 2000, categorie = "", description = "",
            isPossessed = true, mois = null, superCategorie = null, materiau = null, tirage = null, dimensions = null,
            prixAchat = null, valeurEstimee = null, lieuAchat = null, imageUri = null, 
            imageEmbedding = null, // No embedding
            locationId = null
        )
        dao.insert(existingItem)
        val csvContent = "remoteId;annee;mois;categorie;titre;editeur;description;col7;col8;col9;superCategorie\n" +
                         "1003;2024;;;Updated Title No Sig;;;;;;"
        val csvUri = createTestCsvFile(csvContent)

        // WHEN: We run the import. This will trigger a network call which may succeed.
        val importJob = viewModel.importCsv(csvUri, mainDispatcherRule.testDispatcher)
        importJob.join()

        // THEN: The item should be updated, and the signature should now exist (if network was available).
        val updatedItem = dao.findByRemoteId(1003)
        assertEquals("Updated Title No Sig", updatedItem?.titre)
        // This assertion now validates the "happy path": that the signature was computed.
        assertNotNull("Embedding should be computed if network is available", updatedItem?.imageEmbedding)
    }

    private fun createTestCsvFile(content: String): Uri {
        val file = File(context.cacheDir, "test.csv")
        file.writeText(content)
        return Uri.fromFile(file)
    }
}
