package com.example.parabdcollector.ui.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.parabdcollector.R
import com.example.parabdcollector.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * ViewModel for the database backup and restore functionality.
 *
 * This class handles the file I/O operations required to copy the Room database files
 * to and from an external location chosen by the user.
 */
class BackupViewModel(application: Application) : AndroidViewModel(application) {

    private val _operationStatus = MutableLiveData<String>()
    val operationStatus: LiveData<String> = _operationStatus

    /**
     * Creates a backup of the current database to the specified destination URI.
     *
     * @param destinationUri The URI chosen by the user via the file picker.
     */
    fun backupDatabase(destinationUri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>()
            try {
                val dbFile = context.getDatabasePath(AppDatabase.DATABASE_NAME)

                context.contentResolver.openOutputStream(destinationUri)?.use { outputStream ->
                    FileInputStream(dbFile).use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                _operationStatus.postValue(context.getString(R.string.backup_success))
            } catch (e: Exception) {
                Log.e(TAG, "Backup failed", e)
                _operationStatus.postValue(context.getString(R.string.backup_failed, e.message))
            }
        }
    }

    /**
     * Restores the database from a specified backup file URI.
     *
     * This is a sensitive operation that involves several critical steps:
     * 1. Close the current database connection AND destroy the singleton instance.
     * 2. Delete the existing database files, including the main .db file and its
     *    journaling files (-shm and -wal), to prevent data corruption.
     * 3. Copy the backup file into the app's database directory.
     * The app must be restarted after this operation for the changes to take effect.
     *
     * @param sourceUri The URI of the backup file chosen by the user.
     */
    fun restoreDatabase(sourceUri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>()
            val dbPath = context.getDatabasePath(AppDatabase.DATABASE_NAME).parent ?: return@launch
            val dbFile = File(dbPath, AppDatabase.DATABASE_NAME)
            val walFile = File(dbPath, "${AppDatabase.DATABASE_NAME}-wal")
            val shmFile = File(dbPath, "${AppDatabase.DATABASE_NAME}-shm")

            // This is the most critical step. We must close the database and clear the singleton
            // instance to ensure the app creates a new connection on next launch.
            AppDatabase.closeInstance()

            // Delete the old database files. If any deletion fails, abort the restore.
            val deleteSuccess = (!dbFile.exists() || dbFile.delete()) &&
                                (!walFile.exists() || walFile.delete()) &&
                                (!shmFile.exists() || shmFile.delete())

            if (!deleteSuccess) {
                Log.e(TAG, "Failed to delete one or more old database files. Aborting restore.")
                _operationStatus.postValue(context.getString(R.string.restore_failed_delete))
                return@launch
            }

            // Now, copy the backup file to the database location.
            try {
                context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                    FileOutputStream(dbFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                _operationStatus.postValue(context.getString(R.string.restore_success))
            } catch (e: Exception) {
                Log.e(TAG, "Restore failed during copy", e)
                _operationStatus.postValue(context.getString(R.string.restore_failed, e.message))
            }
        }
    }

    companion object {
        private const val TAG = "BackupViewModel"
    }
}
