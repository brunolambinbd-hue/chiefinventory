package com.example.chiefinventory.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import com.example.chiefinventory.R
import com.example.chiefinventory.data.AppDatabase
import com.google.common.truth.Truth.assertThat
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
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

/**
 * Suite de tests unitaires Robolectric pour [BackupViewModel].
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BackupViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var app: Application

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        app = ApplicationProvider.getApplicationContext()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `backupDatabase should handle error when database file does not exist`() = runTest {
        val vm = BackupViewModel(app, testDispatcher)
        val dummyUri = Uri.parse("content://dummy/backup.db")

        vm.backupDatabase(dummyUri).join()

        assertThat(vm.operationStatus.value).isNotNull()
        assertThat(vm.operationStatus.value).contains(app.getString(R.string.backup_failed, "").substringBefore("%s"))
    }

    @Test
    fun `backupDatabase should backup successfully when database file exists`() = runTest {
        val dbFile = app.getDatabasePath(AppDatabase.DATABASE_NAME)
        dbFile.parentFile?.mkdirs()
        dbFile.writeText("sample db content")

        val targetFile = File(app.cacheDir, "dest.db")
        val destUri = Uri.fromFile(targetFile)

        val vm = BackupViewModel(app, testDispatcher)
        vm.backupDatabase(destUri).join()

        assertThat(vm.operationStatus.value).isEqualTo(app.getString(R.string.backup_success))
        assertThat(targetFile.readText()).isEqualTo("sample db content")

        dbFile.delete()
        targetFile.delete()
    }

    @Test
    fun `restoreDatabase should handle error when source input stream is null or invalid`() = runTest {
        val vm = BackupViewModel(app, testDispatcher)
        val invalidUri = Uri.parse("content://invalid/source.db")

        vm.restoreDatabase(invalidUri).join()

        assertThat(vm.operationStatus.value).isNotNull()
        assertThat(vm.operationStatus.value).contains(app.getString(R.string.restore_failed, "").substringBefore("%s"))
    }

    @Test
    fun `restoreDatabase should restore successfully when valid source file is provided`() = runTest {
        val sourceFile = File(app.cacheDir, "source.db")
        sourceFile.writeText("restored db content")
        val sourceUri = Uri.fromFile(sourceFile)

        val vm = BackupViewModel(app, testDispatcher)
        vm.restoreDatabase(sourceUri).join()

        assertThat(vm.operationStatus.value).isEqualTo(app.getString(R.string.restore_success))

        sourceFile.delete()
    }
}
