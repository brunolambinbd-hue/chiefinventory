package com.chefinventory.app.data

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.chefinventory.app.R
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File

/**
 * Suite de tests unitaires vérifiant [DefaultDatabaseBackupManager].
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DefaultDatabaseBackupManagerTest {

    private lateinit var context: Context
    private lateinit var tempDir: File

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.setTheme(R.style.Theme_ChefInventory)

        tempDir = File(context.cacheDir, "default_backup_test_${System.currentTimeMillis()}")
        tempDir.mkdirs()
    }

    @After
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testBackupDatabase_SuccessAndErrorBranches() = runBlocking {
        val mockContentResolver = mock<ContentResolver>()
        val mockUri = mock<Uri>()
        val outputStream = ByteArrayOutputStream()

        whenever(mockContentResolver.openOutputStream(mockUri)).thenReturn(outputStream)

        val spyContext = spy(context)
        whenever(spyContext.contentResolver).thenReturn(mockContentResolver)

        val dbFile = File(tempDir, "test_backup.db")
        dbFile.writeText("db_data_test")
        doReturn(dbFile).whenever(spyContext).getDatabasePath(any())

        val manager = DefaultDatabaseBackupManager(spyContext)
        manager.backupDatabase(mockUri)

        assertThat(outputStream.toString(Charsets.UTF_8)).isEqualTo("db_data_test")

        // Error branch: null output stream
        whenever(mockContentResolver.openOutputStream(mockUri)).thenReturn(null)
        try {
            manager.backupDatabase(mockUri)
            assertWithMessage("Exception attendue").fail()
        } catch (e: Exception) {
            assertThat(e.message).contains(context.getString(R.string.error_output_stream_unavailable))
        }

        // Error branch: dbFile null or non-existent
        doReturn(File(tempDir, "nonexistent.db")).whenever(spyContext).getDatabasePath(any())
        try {
            manager.backupDatabase(mockUri)
            assertWithMessage("Exception attendue").fail()
        } catch (e: Exception) {
            assertThat(e.message).contains(context.getString(R.string.error_db_file_not_found))
        }
    }

    @Test
    @CoveredRequirement("6.3.2.9")
    fun testRestoreDatabase_SuccessAndErrorBranches() = runBlocking {
        val mockContentResolver = mock<ContentResolver>()
        val mockUri = mock<Uri>()
        val inputStream = ByteArrayInputStream("restored_db_data".toByteArray())

        whenever(mockContentResolver.openInputStream(mockUri)).thenReturn(inputStream)

        val spyContext = spy(context)
        whenever(spyContext.contentResolver).thenReturn(mockContentResolver)

        val dbFile = File(tempDir, "test_restore.db")
        dbFile.writeText("old_db_data")
        doReturn(dbFile).whenever(spyContext).getDatabasePath(any())

        var closerCalled = false
        val manager = DefaultDatabaseBackupManager(spyContext, databaseCloser = { closerCalled = true })

        manager.restoreDatabase(mockUri)

        assertThat(closerCalled).isTrue()
        assertThat(dbFile.readText()).isEqualTo("restored_db_data")
    }

    @Test
    @CoveredRequirement("6.3.2.9")
    fun testRestoreDatabase_DbFileDoesNotExistAndNullStream_ThrowsExceptions() = runBlocking {
        val mockContentResolver = mock<ContentResolver>()
        val mockUri = mock<Uri>()

        val spyContext = spy(context)
        whenever(spyContext.contentResolver).thenReturn(mockContentResolver)

        // Non-existent DB file
        val nonexistentDb = File(tempDir, "nonexistent.db")
        doReturn(nonexistentDb).whenever(spyContext).getDatabasePath(any())

        val manager = DefaultDatabaseBackupManager(spyContext, databaseCloser = {})

        try {
            manager.restoreDatabase(mockUri)
            assertWithMessage("Exception attendue").fail()
        } catch (e: Exception) {
            assertThat(e.message).contains(context.getString(R.string.error_db_file_not_found))
        }

        // Null input stream
        val existingDb = File(tempDir, "existing.db")
        existingDb.writeText("data")
        doReturn(existingDb).whenever(spyContext).getDatabasePath(any())
        whenever(mockContentResolver.openInputStream(mockUri)).thenReturn(null)

        try {
            manager.restoreDatabase(mockUri)
            assertWithMessage("Exception attendue").fail()
        } catch (e: Exception) {
            assertThat(e.message).contains("Flux d'entrée nul")
        }
    }

    @Test
    @CoveredRequirement("6.3.2.9")
    fun testRestoreDatabase_DefaultCloserLambdaAndWalShmExisting() = runBlocking {
        val mockContentResolver = mock<ContentResolver>()
        val mockUri = mock<Uri>()
        val inputStream = ByteArrayInputStream("restored_data".toByteArray())
        whenever(mockContentResolver.openInputStream(mockUri)).thenReturn(inputStream)

        val spyContext = spy(context)
        whenever(spyContext.contentResolver).thenReturn(mockContentResolver)

        val dbFile = File(tempDir, "test_default_closer.db")
        dbFile.writeText("old_data")
        val walFile = File(tempDir, "test_default_closer.db-wal")
        walFile.writeText("wal_data")
        val shmFile = File(tempDir, "test_default_closer.db-shm")
        shmFile.writeText("shm_data")

        doReturn(dbFile).whenever(spyContext).getDatabasePath(any())

        val manager = DefaultDatabaseBackupManager(spyContext)
        manager.restoreDatabase(mockUri)

        assertThat(dbFile.readText()).isEqualTo("restored_data")
        assertThat(walFile.exists()).isFalse()
        assertThat(shmFile.exists()).isFalse()
    }

    @Test
    @CoveredRequirement("6.3.2.9")
    fun testRestoreDatabase_NullDatabasePath_ThrowsException() = runBlocking {
        val spyContext = spy(context)
        doReturn(null).whenever(spyContext).getDatabasePath(any())

        val manager = DefaultDatabaseBackupManager(spyContext)
        try {
            manager.restoreDatabase(mock<Uri>())
            assertWithMessage("Exception attendue").fail()
        } catch (e: Exception) {
            assertThat(e.message).contains(context.getString(R.string.error_db_path_unavailable))
        }
    }

    @Test
    @CoveredRequirement("6.3.2.9")
    fun testRestoreDatabase_NullParentDir_ThrowsException() = runBlocking {
        val mockFile = mock<File>()
        val mockAbsFile = mock<File>()
        whenever(mockFile.absoluteFile).thenReturn(mockAbsFile)
        whenever(mockAbsFile.parent).thenReturn(null)

        val spyContext = spy(context)
        doReturn(mockFile).whenever(spyContext).getDatabasePath(any())

        val manager = DefaultDatabaseBackupManager(spyContext)
        try {
            manager.restoreDatabase(mock<Uri>())
            assertWithMessage("Exception attendue").fail()
        } catch (e: Exception) {
            assertThat(e.message).contains(context.getString(R.string.error_db_parent_dir_unavailable))
        }
    }

    @Test
    @CoveredRequirement("6.3.2.9")
    fun testRestoreDatabase_DeleteFileFails_ThrowsException() = runBlocking {
        val mockContentResolver = mock<ContentResolver>()
        val mockUri = mock<Uri>()

        val spyContext = spy(context)
        whenever(spyContext.contentResolver).thenReturn(mockContentResolver)

        val lockedDbFile = File(tempDir, "test_locked.db")
        lockedDbFile.writeText("locked_db_data")

        // Open file stream to lock it on Windows so delete() fails
        val lockStream = java.io.FileOutputStream(lockedDbFile)

        doReturn(lockedDbFile).whenever(spyContext).getDatabasePath(any())

        val manager = DefaultDatabaseBackupManager(spyContext, databaseCloser = {})
        try {
            manager.restoreDatabase(mockUri)
            assertWithMessage("Exception attendue").fail()
        } catch (e: Exception) {
            assertThat(e.message).contains(context.getString(R.string.restore_failed_delete))
        } finally {
            lockStream.close()
        }
    }
}
