package com.example.chiefinventory.java_ui

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.chiefinventory.dao.CollectionDao
import com.example.chiefinventory.data.AppDatabase
import com.example.chiefinventory.model.CollectionItem
import com.example.chiefinventory.repo.CollectionRepository
import com.example.chiefinventory.ui.viewmodel.ImportViewModel
import com.example.chiefinventory.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * Instrumented integration tests for the [com.example.chiefinventory.ui.viewmodel.ImportViewModel].
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
        // Use allowMainThreadQueries to force synchronous database operations in tests.
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
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
        val csvContent =
            "remoteId;annee;mois;categorie;titre;editeur;description;col7;col8;col9;superCategorie\n" +
                    "1001;2023;5;BD;New Test Item;Test Editor;New Desc;;;;Test Super"
        val csvUri = createTestCsvFile(csvContent)

        val importJob = viewModel.importCsv(csvUri, mainDispatcherRule.testDispatcher)
        importJob.join()

        val newItem = dao.findByRemoteId(1001)
        Assert.assertNotNull(newItem)
        Assert.assertEquals("New Test Item", newItem?.titre)
    }

    @Test
    fun importCsv_shouldUpdateExistingItem_andKeepEmbedding() = runTest {
        val existingItem = CollectionItem(
            id = 1,
            remoteId = 1002,
            titre = "Old Title",
            editeur = "Old Editor",
            annee = 2000,
            categorie = "Old Cat",
            description = "",
            isPossessed = true,
            mois = 1,
            superCategorie = "",
            materiau = "",
            tirage = "",
            dimensions = "",
            prixAchat = 0.0,
            valeurEstimee = 0.0,
            lieuAchat = "",
            imageUri = "",
            imageEmbedding = byteArrayOf(1, 2, 3), // Dummy embedding to skip network call
            locationId = null
        )
        dao.insert(existingItem)
        val csvContent =
            "remoteId;annee;mois;categorie;titre;editeur;description;col7;col8;col9;superCategorie\n" +
                    "1002;2024;;;Updated Title;;;;;"
        val csvUri = createTestCsvFile(csvContent)

        val importJob = viewModel.importCsv(csvUri, mainDispatcherRule.testDispatcher)
        importJob.join()

        val updatedItem = dao.findByRemoteId(1002)
        Assert.assertEquals("Updated Title", updatedItem?.titre)
        Assert.assertNotNull("Embedding should be preserved", updatedItem?.imageEmbedding)
    }

    @Test
    fun importCsv_withMissingSignature_shouldUpdateItemAndComputeSignature() = runTest {
        val existingItem = CollectionItem(
            id = 2,
            remoteId = 1003,
            titre = "Old Title No Sig",
            editeur = "",
            annee = 2000,
            categorie = "",
            description = "",
            isPossessed = true,
            mois = 1,
            superCategorie = "",
            materiau = "",
            tirage = "",
            dimensions = "",
            prixAchat = 0.0,
            valeurEstimee = 0.0,
            lieuAchat = "",
            imageUri = "",
            imageEmbedding = null, // No embedding
            locationId = null
        )
        dao.insert(existingItem)
        val csvContent =
            "remoteId;annee;mois;categorie;titre;editeur;description;col7;col8;col9;superCategorie\n" +
                    "1003;2024;;;Updated Title No Sig;;;;;"
        val csvUri = createTestCsvFile(csvContent)

        val importJob = viewModel.importCsv(csvUri, mainDispatcherRule.testDispatcher)
        importJob.join()

        val updatedItem = dao.findByRemoteId(1003)
        Assert.assertEquals("Updated Title No Sig", updatedItem?.titre)
        Assert.assertNotNull(
            "Embedding should be computed if network is available",
            updatedItem?.imageEmbedding
        )
    }

    private fun createTestCsvFile(content: String): Uri {
        val file = File(context.cacheDir, "test.csv")
        file.writeText(content)
        return Uri.fromFile(file)
    }
}