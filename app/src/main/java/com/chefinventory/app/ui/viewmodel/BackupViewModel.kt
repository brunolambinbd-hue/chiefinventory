package com.chefinventory.app.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.chefinventory.app.R
import com.chefinventory.app.data.DatabaseBackupManager
import com.chefinventory.app.data.DefaultDatabaseBackupManager
import com.parabd.lib.traceability.utils.LogManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * ViewModel gérant la sauvegarde et la restauration de la base de données.
 */
@Suppress("FoldInitializerAndIfNull", "kotlin:S6529")
class BackupViewModel(
    application: Application,
    private val backupManager: DatabaseBackupManager = DefaultDatabaseBackupManager(application),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : AndroidViewModel(application) {

    private val _operationStatus = MutableLiveData<String>()
    val operationStatus: LiveData<String> = _operationStatus

    fun backupDatabase(destinationUri: Uri): Job = viewModelScope.launch(ioDispatcher) {
        val context = getApplication<Application>()
        try {
            backupManager.backupDatabase(destinationUri)
            _operationStatus.postValue(context.getString(R.string.backup_success))
        } catch (e: Exception) {
            val fallback = context.getString(R.string.error_unknown)
            val prefix = context.getString(R.string.log_backup_failed_prefix)
            LogManager.e(TAG, prefix, e, fallbackMessage = fallback)
            val rawMsg = e.message
            val errorMsg = if (!rawMsg.isNullOrBlank()) rawMsg else fallback
            _operationStatus.postValue(context.getString(R.string.backup_failed, errorMsg))
        }
    }

    fun restoreDatabase(sourceUri: Uri): Job = viewModelScope.launch(ioDispatcher) {
        val context = getApplication<Application>()
        try {
            backupManager.restoreDatabase(sourceUri)
            _operationStatus.postValue(context.getString(R.string.restore_success))
        } catch (e: Exception) {
            val fallback = context.getString(R.string.error_unknown)
            val prefix = context.getString(R.string.log_restore_failed_prefix)
            LogManager.e(TAG, prefix, e, fallbackMessage = fallback)
            val rawMsg = e.message
            val errorMsg = if (!rawMsg.isNullOrBlank()) rawMsg else fallback
            _operationStatus.postValue(context.getString(R.string.restore_failed, errorMsg))
        }
    }

    companion object {
        private const val TAG = "BackupViewModel"
    }
}
