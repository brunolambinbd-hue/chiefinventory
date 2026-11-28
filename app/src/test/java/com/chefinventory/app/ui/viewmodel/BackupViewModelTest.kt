package com.chefinventory.app.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.chefinventory.app.R
import com.chefinventory.app.data.DatabaseBackupManager
import com.google.common.truth.Truth.assertThat
import com.parabd.lib.traceability.CoveredRequirement
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
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Suite de tests unitaires purement JVM vérifiant [BackupViewModel] avec un FakeManager.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BackupViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var mockApp: Application

    class FakeDatabaseBackupManager : DatabaseBackupManager {
        var shouldFailBackup = false
        var backupException: Exception? = null

        var shouldFailRestore = false
        var restoreException: Exception? = null

        override suspend fun backupDatabase(destinationUri: Uri) {
            if (shouldFailBackup) {
                throw backupException ?: RuntimeException("Backup failed")
            }
        }

        override suspend fun restoreDatabase(sourceUri: Uri) {
            if (shouldFailRestore) {
                throw restoreException ?: RuntimeException("Restore failed")
            }
        }
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        mockApp = mock()
        whenever(mockApp.getString(anyInt())).thenReturn("string_resource")
        whenever(mockApp.getString(anyInt(), anyOrNull())).thenAnswer { inv -> "formatted: ${inv.arguments.getOrNull(1)}" }
        whenever(mockApp.getString(anyInt(), anyOrNull(), anyOrNull())).thenAnswer { inv -> "formatted: ${inv.arguments.getOrNull(1)}" }

        whenever(mockApp.getString(R.string.backup_success)).thenReturn("Sauvegarde réussie")
        whenever(mockApp.getString(R.string.restore_success)).thenReturn("Restauration réussie")
        whenever(mockApp.getString(R.string.error_unknown)).thenReturn("Erreur inconnue")
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testBackupDatabase_SuccessAndFailureBranches() = runTest {
        val fakeManager = FakeDatabaseBackupManager()
        val viewModel = BackupViewModel(mockApp, fakeManager, testDispatcher)
        val mockUri = mockUri()

        // 1. Success
        val j1 = viewModel.backupDatabase(mockUri)
        testDispatcher.scheduler.advanceUntilIdle()
        j1.join()
        assertThat(viewModel.operationStatus.value).isEqualTo("Sauvegarde réussie")

        // 2. Exception with message
        fakeManager.shouldFailBackup = true
        fakeManager.backupException = RuntimeException("Disk full")
        val j2 = viewModel.backupDatabase(mockUri)
        testDispatcher.scheduler.advanceUntilIdle()
        j2.join()
        assertThat(viewModel.operationStatus.value).contains("Disk full")

        // 3. Exception with blank message -> fallback
        fakeManager.backupException = RuntimeException("   ")
        val j3 = viewModel.backupDatabase(mockUri)
        testDispatcher.scheduler.advanceUntilIdle()
        j3.join()
        assertThat(viewModel.operationStatus.value).contains("Erreur inconnue")

        // 4. Exception with null message -> fallback
        fakeManager.backupException = RuntimeException(null as String?)
        val j4 = viewModel.backupDatabase(mockUri)
        testDispatcher.scheduler.advanceUntilIdle()
        j4.join()
        assertThat(viewModel.operationStatus.value).contains("Erreur inconnue")
    }

    @Test
    @CoveredRequirement("6.3.2.9")
    fun testRestoreDatabase_SuccessAndFailureBranches() = runTest {
        val fakeManager = FakeDatabaseBackupManager()
        val viewModel = BackupViewModel(mockApp, fakeManager, testDispatcher)
        val mockUri = mockUri()

        // 1. Success
        val j1 = viewModel.restoreDatabase(mockUri)
        testDispatcher.scheduler.advanceUntilIdle()
        j1.join()
        assertThat(viewModel.operationStatus.value).isEqualTo("Restauration réussie")

        // 2. Exception with message
        fakeManager.shouldFailRestore = true
        fakeManager.restoreException = RuntimeException("Corrupted DB")
        val j2 = viewModel.restoreDatabase(mockUri)
        testDispatcher.scheduler.advanceUntilIdle()
        j2.join()
        assertThat(viewModel.operationStatus.value).contains("Corrupted DB")

        // 3. Exception with blank message -> fallback
        fakeManager.restoreException = RuntimeException("   ")
        val j3 = viewModel.restoreDatabase(mockUri)
        testDispatcher.scheduler.advanceUntilIdle()
        j3.join()
        assertThat(viewModel.operationStatus.value).contains("Erreur inconnue")

        // 4. Exception with null message -> fallback
        fakeManager.restoreException = RuntimeException(null as String?)
        val j4 = viewModel.restoreDatabase(mockUri)
        testDispatcher.scheduler.advanceUntilIdle()
        j4.join()
        assertThat(viewModel.operationStatus.value).contains("Erreur inconnue")
    }

    private fun mockUri(): Uri {
        return mock<Uri>()
    }
}
