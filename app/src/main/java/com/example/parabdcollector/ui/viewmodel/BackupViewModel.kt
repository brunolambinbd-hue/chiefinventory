package com.example.parabdcollector.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
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
            try {
                val context = getApplication<Application>()
                val dbFile = context.getDatabasePath(AppDatabase.DATABASE_NAME)

                context.contentResolver.openOutputStream(destinationUri)?.use { outputStream ->
                    FileInputStream(dbFile).use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                _operationStatus.postValue("Sauvegarde réussie !")
            } catch (e: Exception) {
                e.printStackTrace()
                _operationStatus.postValue("Échec de la sauvegarde : ${e.message}")
            }
        }
    }

    /**
     * Restores the database from a specified backup file URI.
     *
     * This is a sensitive operation. It requires the database to be closed before its files can be replaced.
     * The app will need to be restarted after this operation completes.
     *
     * @param sourceUri The URI of the backup file chosen by the user.
     */
    fun restoreDatabase(sourceUri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>()
            val dbFile = context.getDatabasePath(AppDatabase.DATABASE_NAME)

            // Close the database before attempting to overwrite it.
            // This is a critical step.
            AppDatabase.getDatabase(context).close()

            try {
                context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                    FileOutputStream(dbFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                _operationStatus.postValue("Restauration réussie ! Veuillez redémarrer l\'application.")
            } catch (e: Exception) {
                e.printStackTrace()
                _operationStatus.postValue("Échec de la restauration : ${e.message}")
            }
        }
    }
}
