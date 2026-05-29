package com.example.parabdcollector.ui.viewmodel

import android.app.Application
import android.content.ContentResolver
import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.parabdcollector.model.CollectionItem
import com.example.parabdcollector.model.ImportSession
import com.example.parabdcollector.repo.CollectionRepository
import com.example.parabdcollector.repo.ImportRepository
import com.example.parabdcollector.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.*
import java.io.ByteArrayInputStream

/**
 * Unit tests for [ImportViewModel].
 */
@ExperimentalCoroutinesApi
class ImportViewModelTest {

    @get:Rule
    val mainDispatcherRule: MainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var application: Application
    private lateinit var contentResolver: ContentResolver
    private lateinit var collectionRepository: CollectionRepository
    private lateinit var importRepository: ImportRepository
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: ImportViewModel

    @Before
    fun setup() {
        application = mock()
        contentResolver = mock()
        collectionRepository = mock()
        importRepository = mock()

        whenever(application.contentResolver).thenReturn(contentResolver)
        
        // Use a test dispatcher for controlled execution
        viewModel = ImportViewModel(application, collectionRepository, importRepository)
    }

    @Test
    fun `importCsv should parse lines and insert new items`() = runTest {
        // GIVEN: A CSV with one header and one data line
        // Format: remoteId;annee;mois;categorie;titre;editeur;description;...;superCategorie
        val csvContent = "ID;Annee;Mois;Cat;Titre;Editeur;Desc;X;Y;Z;SuperCat\n" +
                "101;2024;5;Albums;Tintin au Tibet;Casterman;Description de test; ; ; ;Album"
        val inputStream = ByteArrayInputStream(csvContent.toByteArray(charset("Windows-1252")))
        val mockUri = mock<Uri>()
        
        whenever(contentResolver.openInputStream(mockUri)).thenReturn(inputStream)
        whenever(importRepository.insert(any())).thenReturn(1L)
        whenever(collectionRepository.findByRemoteId(101)).thenReturn(null) // New item

        // WHEN: Importing the CSV
        viewModel.importCsv(mockUri, testDispatcher)
        testDispatcher.scheduler.advanceUntilIdle()

        // THEN: Verify interaction with repositories
        // 1. Session created
        verify(importRepository).insert(argThat { status == "IN_PROGRESS" })
        
        // 2. Item inserted
        verify(collectionRepository).insertAll(argThat { 
            (size == 1) && (first().remoteId == 101) && (first().titre == "Tintin au Tibet")
        })

        // 3. Session marked as success
        verify(importRepository).update(argThat { (status == "SUCCESS") && (itemsAdded == 1) })
    }

    @Test
    fun `importCsv should update existing items`() = runTest {
        // GIVEN: CSV with an item already in database
        val csvContent = "ID;Annee;Mois;Cat;Titre;Editeur;Desc;X;Y;Z;SuperCat\n" +
                "102;2024;5;Albums;Updated Title;Casterman;Desc; ; ; ;Album"
        val inputStream = ByteArrayInputStream(csvContent.toByteArray(charset("Windows-1252")))
        val mockUri = mock<Uri>()
        
        val existingItem = CollectionItem(id = 50, remoteId = 102, titre = "Old Title")
        whenever(contentResolver.openInputStream(mockUri)).thenReturn(inputStream)
        whenever(importRepository.insert(any())).thenReturn(1L)
        whenever(collectionRepository.findByRemoteId(102)).thenReturn(existingItem)

        // WHEN
        viewModel.importCsv(mockUri, testDispatcher)
        testDispatcher.scheduler.advanceUntilIdle()

        // THEN: Verify update instead of insert
        verify(collectionRepository).updateAll(argThat { 
            (size == 1) && (first().id == 50L) && (first().titre == "Updated Title")
        })
        verify(importRepository).update(argThat { (status == "SUCCESS") && (itemsUpdated == 1) })
    }

    @Test
    fun `importCsv should handle error during process`() = runTest {
        // GIVEN: Content resolver throws exception
        val mockUri = mock<Uri>()
        whenever(contentResolver.openInputStream(mockUri)).thenThrow(RuntimeException("IO Error"))
        whenever(importRepository.insert(any())).thenReturn(1L)

        // WHEN
        viewModel.importCsv(mockUri, testDispatcher)
        testDispatcher.scheduler.advanceUntilIdle()

        // THEN: Session should be marked as ERROR
        verify(importRepository).update(argThat { status == "ERROR" })
    }

    @Test
    fun `deleteSession should call importRepository`() = runTest {
        val session = ImportSession(id = 1, fileName = "test.csv")
        
        viewModel.deleteSession(session)
        testDispatcher.scheduler.advanceUntilIdle()
        
        verify(importRepository).delete(session)
    }
}
