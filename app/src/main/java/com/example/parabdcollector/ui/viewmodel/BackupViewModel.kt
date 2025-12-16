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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class BackupViewModel(application: Application) : AndroidViewModel(application) {

    private val _operationStatus = MutableLiveData<String>()
    val operationStatus: LiveData<String> = _operationStatus

    fun backupDatabase(destinationUri: Uri, dispatcher: CoroutineDispatcher = Dispatchers.IO) {
        viewModelScope.launch(dispatcher) {
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

    fun restoreDatabase(sourceUri: Uri, dispatcher: CoroutineDispatcher = Dispatchers.IO) {
        viewModelScope.launch(dispatcher) {
            val context = getApplication<Application>()
            val dbPath = context.getDatabasePath(AppDatabase.DATABASE_NAME).parent ?: return@launch
            val dbFile = File(dbPath, AppDatabase.DATABASE_NAME)
            val walFile = File(dbPath, "${AppDatabase.DATABASE_NAME}-wal")
            val shmFile = File(dbPath, "${AppDatabase.DATABASE_NAME}-shm")

            // CRITICAL STEP: Close the database connection and destroy the singleton instance.
            AppDatabase.closeInstance()

            // Allow some time for the system to release file locks.
            Thread.sleep(500)

            val deleteSuccess = (!dbFile.exists() || dbFile.delete()) &&
                                (!walFile.exists() || walFile.delete()) &&
                                (!shmFile.exists() || shmFile.delete())

            if (!deleteSuccess) {
                Log.e(TAG, "Failed to delete one or more old database files. Aborting restore.")
                _operationStatus.postValue(context.getString(R.string.restore_failed_delete))
                return@launch
            }

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
