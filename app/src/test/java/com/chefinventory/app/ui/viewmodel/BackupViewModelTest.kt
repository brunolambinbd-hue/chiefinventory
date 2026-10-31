package com.chefinventory.app.ui.viewmodel

import android.app.Application
import android.content.ContentResolver
import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import com.chefinventory.app.R
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
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BackupViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var app: Application
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        app = ApplicationProvider.getApplicationContext()
        app.setTheme(R.style.Theme_ChefInventory)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testBackupDatabase_SuccessAndNullOutputStreamAndExceptionBranches() = runTest {
        val mockContentResolver = mock<ContentResolver>()
        val mockUri = mock<Uri>()
        val outputStream = ByteArrayOutputStream()

        whenever(mockContentResolver.openOutputStream(mockUri)).thenReturn(outputStream)

        val spyApp = spy(app)
        whenever(spyApp.contentResolver).thenReturn(mockContentResolver)

        // Create dummy DB file in test app context path
        val dbFile = spyApp.getDatabasePath("chiefinventory_database")
        dbFile.parentFile?.mkdirs()
        dbFile.writeText("db_content")

        val viewModel = BackupViewModel(spyApp, testDispatcher)
        viewModel.backupDatabase(mockUri).join()
        assertThat(viewModel.operationStatus.value).isEqualTo(spyApp.getString(R.string.backup_success))

        // Null output stream branch
        whenever(mockContentResolver.openOutputStream(mockUri)).thenReturn(null)
        viewModel.backupDatabase(mockUri).join()
        assertThat(viewModel.operationStatus.value).isNotEmpty()

        // Exception branch with null message
        doThrow(FileNotFoundException()).whenever(mockContentResolver).openOutputStream(mockUri)
        viewModel.backupDatabase(mockUri).join()
        assertThat(viewModel.operationStatus.value).contains(spyApp.getString(R.string.error_unknown))

        // Exception branch with empty message
        doThrow(FileNotFoundException("   ")).whenever(mockContentResolver).openOutputStream(mockUri)
        viewModel.backupDatabase(mockUri).join()
        assertThat(viewModel.operationStatus.value).contains(spyApp.getString(R.string.error_unknown))

        // Exception branch with valid message
        doThrow(FileNotFoundException("Disk full")).whenever(mockContentResolver).openOutputStream(mockUri)
        viewModel.backupDatabase(mockUri).join()
        assertThat(viewModel.operationStatus.value).contains("Disk full")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testBackupDatabase_DbFileDoesNotExist_UpdatesOperationStatus() = runTest {
        val spyApp = spy(app)
        val dbFile = spyApp.getDatabasePath("chiefinventory_database")
        if (dbFile.exists()) dbFile.delete()

        val mockUri = mock<Uri>()
        val viewModel = BackupViewModel(spyApp, testDispatcher)
        viewModel.backupDatabase(mockUri).join()

        assertThat(viewModel.operationStatus.value).isNotEmpty()
    }

    @Test
    @CoveredRequirement("6.3.2.9")
    fun testBackupDatabase_NullDbFile_UpdatesOperationStatus() = runTest {
        val spyApp = spy(app)
        doReturn(null).whenever(spyApp).getDatabasePath("chiefinventory_database")

        val mockUri = mock<Uri>()
        val viewModel = BackupViewModel(spyApp, testDispatcher)
        viewModel.backupDatabase(mockUri).join()

        assertThat(viewModel.operationStatus.value).isNotEmpty()
    }

    @Test
    @CoveredRequirement("6.3.2.9")
    fun testRestoreDatabase_DbFileDoesNotExist_UpdatesOperationStatus() = runTest {
        val spyApp = spy(app)
        val dbFile = spyApp.getDatabasePath("chiefinventory_database")
        if (dbFile.exists()) dbFile.delete()

        val mockUri = mock<Uri>()
        val viewModel = BackupViewModel(spyApp, testDispatcher)
        viewModel.restoreDatabase(mockUri).join()

        assertThat(viewModel.operationStatus.value).isNotEmpty()
    }

    @Test
    @CoveredRequirement("6.3.2.9")
    fun testRestoreDatabase_NullDbFileRawAndNullDbPath_UpdatesOperationStatus() = runTest {
        val mockUri = mock<Uri>()

        // 1. null dbFileRaw
        val spyAppNullFile = spy(app)
        doReturn(null).whenever(spyAppNullFile).getDatabasePath("chiefinventory_database")
        val vmNullFile = BackupViewModel(spyAppNullFile, testDispatcher)
        vmNullFile.restoreDatabase(mockUri).join()
        assertThat(vmNullFile.operationStatus.value).isNotEmpty()

        // 2. null dbPath (absoluteFile.parent == null)
        val spyAppNullParent = spy(app)
        val mockFile = mock<File>()
        val mockAbsFile = mock<File>()
        whenever(mockFile.absoluteFile).thenReturn(mockAbsFile)
        whenever(mockAbsFile.parent).thenReturn(null)
        doReturn(mockFile).whenever(spyAppNullParent).getDatabasePath("chiefinventory_database")

        val vmNullParent = BackupViewModel(spyAppNullParent, testDispatcher)
        vmNullParent.restoreDatabase(mockUri).join()
        assertThat(vmNullParent.operationStatus.value).isNotEmpty()
    }

    @Test
    @CoveredRequirement("6.3.2.9")
    fun testRestoreDatabase_DeleteFailed_ReturnsFailedDeleteStatus() = runTest {
        val mockContentResolver = mock<ContentResolver>()
        val mockUri = mock<Uri>()

        val spyApp = spy(app)
        whenever(spyApp.contentResolver).thenReturn(mockContentResolver)

        // Create a non-empty directory at dbFile location so dbFile.delete() returns false
        val dbFile = spyApp.getDatabasePath("chiefinventory_database")
        dbFile.parentFile?.mkdirs()
        dbFile.mkdirs()
        val childFile = File(dbFile, "child.txt")
        childFile.writeText("child_data")

        val viewModel = BackupViewModel(spyApp, testDispatcher)
        viewModel.restoreDatabase(mockUri).join()

        assertThat(viewModel.operationStatus.value).isEqualTo(spyApp.getString(R.string.restore_failed_delete))

        // Cleanup directory
        childFile.delete()
        dbFile.delete()
    }

    @Test
    @CoveredRequirement("6.3.2.9")
    fun testRestoreDatabase_SuccessAndNullInputStreamAndExistingFiles() = runTest {
        val mockContentResolver = mock<ContentResolver>()
        val mockUri = mock<Uri>()

        whenever(mockContentResolver.openInputStream(mockUri)).thenReturn(null)

        val spyApp = spy(app)
        whenever(spyApp.contentResolver).thenReturn(mockContentResolver)

        // Create existing DB, WAL, and SHM files
        val dbFile = spyApp.getDatabasePath("chiefinventory_database")
        dbFile.parentFile?.mkdirs()
        dbFile.writeText("db_data")
        val walFile = File(dbFile.parent, "chiefinventory_database-wal")
        walFile.writeText("wal_data")
        val shmFile = File(dbFile.parent, "chiefinventory_database-shm")
        shmFile.writeText("shm_data")

        val viewModel = BackupViewModel(spyApp, testDispatcher)
        viewModel.restoreDatabase(mockUri).join()

        assertThat(viewModel.operationStatus.value).contains("Flux d'entrée nul")
    }

    @Test
    @CoveredRequirement("6.3.2.9")
    fun testRestoreDatabase_WithValidStream_Succeeds() = runTest {
        val mockContentResolver = mock<ContentResolver>()
        val mockUri = mock<Uri>()
        whenever(mockContentResolver.openInputStream(mockUri)).thenReturn(ByteArrayInputStream("db_data".toByteArray()))

        val spyApp = spy(app)
        whenever(spyApp.contentResolver).thenReturn(mockContentResolver)

        val dbFile = spyApp.getDatabasePath("chiefinventory_database")
        dbFile.parentFile?.mkdirs()
        dbFile.writeText("db_data")

        val viewModel = BackupViewModel(spyApp, testDispatcher)
        viewModel.restoreDatabase(mockUri).join()

        assertThat(viewModel.operationStatus.value).isEqualTo(spyApp.getString(R.string.restore_success))
    }

    @Test
    @CoveredRequirement("6.3.2.9")
    fun testRestoreDatabase_Exception_UpdatesOperationStatus() = runTest {
        val mockContentResolver = mock<ContentResolver>()
        val mockUri = mock<Uri>()

        val spyApp = spy(app)
        val dbFile = spyApp.getDatabasePath("chiefinventory_database")
        dbFile.parentFile?.mkdirs()
        dbFile.writeText("db_data")

        // Null message exception
        doThrow(RuntimeException()).whenever(mockContentResolver).openInputStream(any())
        whenever(spyApp.contentResolver).thenReturn(mockContentResolver)

        val viewModel = BackupViewModel(spyApp, testDispatcher)
        viewModel.restoreDatabase(mockUri).join()
        assertThat(viewModel.operationStatus.value).contains(spyApp.getString(R.string.error_unknown))

        // Blank message exception
        dbFile.writeText("db_data")
        doThrow(RuntimeException("   ")).whenever(mockContentResolver).openInputStream(any())
        viewModel.restoreDatabase(mockUri).join()
        assertThat(viewModel.operationStatus.value).contains(spyApp.getString(R.string.error_unknown))

        // Valid message exception
        dbFile.writeText("db_data")
        doThrow(RuntimeException("Corrupted DB")).whenever(mockContentResolver).openInputStream(any())
        viewModel.restoreDatabase(mockUri).join()
        assertThat(viewModel.operationStatus.value).contains("Corrupted DB")
    }
}
