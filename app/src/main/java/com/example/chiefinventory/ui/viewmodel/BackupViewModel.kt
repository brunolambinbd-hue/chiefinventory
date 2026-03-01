package com.example.chiefinventory.ui.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.chiefinventory.R
import com.example.chiefinventory.data.AppDatabase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * ViewModel responsible for database backup and restore operations.
 */
class BackupViewModel(
    application: Application,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : AndroidViewModel(application) {

    private val _operationStatus = MutableLiveData<String>()
    val operationStatus: LiveData<String> = _operationStatus

    fun backupDatabase(destinationUri: Uri) {
        viewModelScope.launch(ioDispatcher) {
            val context = getApplication<Application>()
            try {
                val dbFile = context.getDatabasePath(AppDatabase.DATABASE_NAME) 
                    ?: throw Exception("Fichier de base de données introuvable")

                context.contentResolver.openOutputStream(destinationUri)?.use { outputStream ->
                    FileInputStream(dbFile).use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                _operationStatus.postValue(context.getString(R.string.backup_success))
            } catch (e: Exception) {
                Log.e(TAG, "Backup failed", e)
                val errorMsg = e.message ?: "Erreur inconnue"
                _operationStatus.postValue(context.getString(R.string.backup_failed, errorMsg))
            }
        }
    }

    fun restoreDatabase(sourceUri: Uri) {
        viewModelScope.launch(ioDispatcher) {
            val context = getApplication<Application>()
            try {
                val dbFileRaw = context.getDatabasePath(AppDatabase.DATABASE_NAME)
                    ?: throw Exception("Impossible d'obtenir le chemin de la base de données")
                
                val dbPath = dbFileRaw.absoluteFile.parent 
                    ?: throw Exception("Impossible de déterminer le répertoire parent")
                
                val dbFile = File(dbPath, AppDatabase.DATABASE_NAME)
                val walFile = File(dbPath, "${AppDatabase.DATABASE_NAME}-wal")
                val shmFile = File(dbPath, "${AppDatabase.DATABASE_NAME}-shm")

                // ÉTAPE CRITIQUE : Fermer la base de données avant toute manipulation de fichiers
                AppDatabase.closeInstance()

                // Délai pour s'assurer que le système a libéré les fichiers
                delay(500)

                val deleteSuccess = (!dbFile.exists() || dbFile.delete()) &&
                                    (!walFile.exists() || walFile.delete()) &&
                                    (!shmFile.exists() || shmFile.delete())

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
                Log.e(TAG, "Restore failed", e)
                val errorMsg = e.message ?: "Erreur inconnue"
                _operationStatus.postValue(context.getString(R.string.restore_failed, errorMsg))
            }
        }
    }

    companion object {
        private const val TAG = "BackupViewModel"
    }
}
