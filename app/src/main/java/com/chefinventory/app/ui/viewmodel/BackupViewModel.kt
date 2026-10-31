package com.chefinventory.app.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.chefinventory.app.R
import com.chefinventory.app.data.AppDatabase
import com.parabd.lib.traceability.utils.LogManager
import com.parabd.lib.traceability.utils.uException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlin.time.Duration.Companion.milliseconds

/**
 * ViewModel responsible for database backup and restore operations.
 */
@Suppress("FoldInitializerAndIfNull", "kotlin:S6529")
class BackupViewModel(
    application: Application,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : AndroidViewModel(application) {

    private val _operationStatus = MutableLiveData<String>()
    val operationStatus: LiveData<String> = _operationStatus

    fun backupDatabase(destinationUri: Uri): Job = viewModelScope.launch(ioDispatcher) {
        val context = getApplication<Application>()
        try {
            val dbFile = context.getDatabasePath(AppDatabase.DATABASE_NAME)
            if (dbFile == null) {
                throw LogManager.uException(TAG, context.getString(R.string.error_db_file_not_found))
            }
            if (!dbFile.exists()) {
                throw LogManager.uException(TAG, context.getString(R.string.error_db_file_not_found))
            }

            val outputStream = context.contentResolver.openOutputStream(destinationUri)
            if (outputStream != null) {
                outputStream.use { outStream ->
                    FileInputStream(dbFile).use { inputStream ->
                        inputStream.copyTo(outStream)
                    }
                }
            } else {
                throw LogManager.uException(TAG, context.getString(R.string.error_output_stream_unavailable))
            }
            _operationStatus.postValue(context.getString(R.string.backup_success))
        } catch (e: Exception) {
            val fallback = context.getString(R.string.error_unknown)
            val prefix = context.getString(R.string.log_backup_failed_prefix)
            LogManager.e(TAG, prefix, e, fallbackMessage = fallback)
            val rawMsg = e.message
            val errorMsg = if (!rawMsg.isNullOrBlank()) {
                rawMsg
            } else {
                fallback
            }
            _operationStatus.postValue(context.getString(R.string.backup_failed, errorMsg))
        }
    }

    fun restoreDatabase(sourceUri: Uri): Job = viewModelScope.launch(ioDispatcher) {
        val context = getApplication<Application>()
        try {
            val dbFileRaw = context.getDatabasePath(AppDatabase.DATABASE_NAME)
            if (dbFileRaw == null) {
                throw LogManager.uException(TAG, context.getString(R.string.error_db_path_unavailable))
            }

            val dbPath = dbFileRaw.absoluteFile.parent
            if (dbPath == null) {
                throw LogManager.uException(TAG, context.getString(R.string.error_db_parent_dir_unavailable))
            }

            val dbFile = File(dbPath, AppDatabase.DATABASE_NAME)
            if (!dbFile.exists()) {
                throw LogManager.uException(TAG, context.getString(R.string.error_db_file_not_found))
            }

            val walFile = File(dbPath, "${AppDatabase.DATABASE_NAME}-wal")
            val shmFile = File(dbPath, "${AppDatabase.DATABASE_NAME}-shm")

            // ÉTAPE CRITIQUE : Fermer la base de données avant toute manipulation de fichiers
            AppDatabase.closeInstance()

            // Délai pour s'assurer que le système a libéré les fichiers
            delay(500.milliseconds)

            val delDb = deleteFileSafely(dbFile)
            val delWal = deleteFileSafely(walFile)
            val delShm = deleteFileSafely(shmFile)
            val deleteSuccess = delDb and delWal and delShm

            if (!deleteSuccess) {
                _operationStatus.postValue(context.getString(R.string.restore_failed_delete))
                return@launch
            }

            val inputStream = context.contentResolver.openInputStream(sourceUri)
            if (inputStream == null) {
                _operationStatus.postValue(context.getString(R.string.restore_failed, "Flux d'entrée nul"))
                return@launch
            }

            inputStream.use { input ->
                FileOutputStream(dbFile).use { outputStream ->
                    input.copyTo(outputStream)
                }
            }
            _operationStatus.postValue(context.getString(R.string.restore_success))
        } catch (e: Exception) {
            val fallback = context.getString(R.string.error_unknown)
            val prefix = context.getString(R.string.log_restore_failed_prefix)
            LogManager.e(TAG, prefix, e, fallbackMessage = fallback)
            val rawMsg = e.message
            val errorMsg = if (!rawMsg.isNullOrBlank()) {
                rawMsg
            } else {
                fallback
            }
            _operationStatus.postValue(context.getString(R.string.restore_failed, errorMsg))
        }
    }

    companion object {
        private const val TAG = "BackupViewModel"

        private fun deleteFileSafely(file: File): Boolean {
            if (file.exists()) {
                return file.delete()
            }
            return true
        }
    }
}
