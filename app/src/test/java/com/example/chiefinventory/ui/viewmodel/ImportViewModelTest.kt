package com.example.chiefinventory.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import com.example.chiefinventory.model.CollectionItem
import com.example.chiefinventory.repo.CollectionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

/**
 * Suite de tests unitaires Robolectric pour [ImportViewModel].
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ImportViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var app: Application
    private lateinit var mockRepo: CollectionRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        app = ApplicationProvider.getApplicationContext()
        mockRepo = mock()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `importCsv should parse new items from CSV file and insert into repository`() = runTest {
        val csvFile = File(app.cacheDir, "import_test.csv")
        csvFile.writeText(
            "ID;Annee;Mois;Categorie;Titre;Editeur;Description;Prix;Valeur;Lieu;SuperCategorie\n" +
            "100;1985;3;BD;Tintin;Casterman;Tirage limite 500 ex;10.0;15.0;Paris;Franco-Belge"
        )
        val csvUri = Uri.fromFile(csvFile)

        whenever(mockRepo.findByRemoteId(100)).thenReturn(null)

        val mockImageLoader = mock<coil.ImageLoader>()
        val vm = ImportViewModel(app, mockRepo, mockImageLoader)
        vm.importCsv(csvUri, testDispatcher).join()

        verify(mockRepo).insert(any())
        csvFile.delete()
    }

    @Test
    fun `importCsv should update existing items found in repository`() = runTest {
        val csvFile = File(app.cacheDir, "import_update_test.csv")
        csvFile.writeText(
            "ID;Annee;Mois;Categorie;Titre;Editeur;Description;Prix;Valeur;Lieu;SuperCategorie\n" +
            "200;1990;1;Comics;Batman;Urban;Tirage 1000 ex;20.0;25.0;Lyon;Comics"
        )
        val csvUri = Uri.fromFile(csvFile)

        val existingItem = CollectionItem(
            id = 50L,
            remoteId = 200,
            titre = "Old Title",
            imageUri = "content://local/image.png"
        )
        whenever(mockRepo.findByRemoteId(200)).thenReturn(existingItem)

        val mockImageLoader = mock<coil.ImageLoader>()
        val vm = ImportViewModel(app, mockRepo, mockImageLoader)
        vm.importCsv(csvUri, testDispatcher).join()

        verify(mockRepo).update(any())
        csvFile.delete()
    }
}
