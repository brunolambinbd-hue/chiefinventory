package com.example.parabdcollector.ui.viewmodel

import android.app.Application
import android.content.ContentResolver
import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.parabdcollector.R
import com.example.parabdcollector.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.io.File

/**
 * Unit tests for [BackupViewModel].
 */
@ExperimentalCoroutinesApi
class BackupViewModelTest {

    @get:Rule
    val mainDispatcherRule: MainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var application: Application
    private lateinit var contentResolver: ContentResolver
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: BackupViewModel

    @Before
    fun setup() {
        application = mock()
        contentResolver = mock()
        whenever(application.contentResolver).thenReturn(contentResolver)
        viewModel = BackupViewModel(application, testDispatcher)
    }

    @Test
    fun `backupDatabase should post error if database file not found`(): Unit = runTest {
        // GIVEN: Database path returns null or non-existent file
        whenever(application.getDatabasePath(any())).thenReturn(null)
        whenever(application.getString(R.string.backup_failed, "Fichier de base de données introuvable")).thenReturn("Error: Not Found")

        // WHEN
        viewModel.backupDatabase(mock<Uri>())
        testDispatcher.scheduler.advanceUntilIdle()

        // THEN
        assertEquals("Error: Not Found", viewModel.operationStatus.value)
    }

    @Test
    fun `backupDatabase should post failure if output stream fails`(): Unit = runTest {
        // GIVEN
        val mockFile = mock<File>()
        whenever(application.getDatabasePath(any())).thenReturn(mockFile)
        whenever(contentResolver.openOutputStream(any())).thenThrow(RuntimeException("Write Error"))
        whenever(application.getString(R.string.backup_failed, "Write Error")).thenReturn("Failed")

        // WHEN
        viewModel.backupDatabase(mock<Uri>())
        testDispatcher.scheduler.advanceUntilIdle()

        // THEN
        assertEquals("Failed", viewModel.operationStatus.value)
    }

    @Test
    fun `restoreDatabase should post error if source input stream is null`(): Unit = runTest {
        // GIVEN
        val mockFile = mock<File>()
        whenever(mockFile.absoluteFile).thenReturn(mockFile)
        whenever(mockFile.parent).thenReturn("/fake/path")
        whenever(application.getDatabasePath(any())).thenReturn(mockFile)
        
        whenever(contentResolver.openInputStream(any())).thenReturn(null)
        whenever(application.getString(R.string.restore_failed, "Flux d'entrée nul")).thenReturn("Null Stream")

        // WHEN
        viewModel.restoreDatabase(mock<Uri>())
        testDispatcher.scheduler.advanceUntilIdle()

        // THEN
        assertEquals("Null Stream", viewModel.operationStatus.value)
    }
}
