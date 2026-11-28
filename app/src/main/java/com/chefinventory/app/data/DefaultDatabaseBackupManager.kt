package com.chefinventory.app.data

import android.content.Context
import android.net.Uri
import com.chefinventory.app.R
import com.parabd.lib.traceability.utils.LogManager
import com.parabd.lib.traceability.utils.uException
import kotlinx.coroutines.delay
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlin.time.Duration.Companion.milliseconds

/**
 * Implémentation par défaut Android de [DatabaseBackupManager].
 */
@Suppress("FoldInitializerAndIfNull", "kotlin:S6529")
class DefaultDatabaseBackupManager(
    private val context: Context,
    private val databaseCloser: () -> Unit = { AppDatabase.closeInstance() }
) : DatabaseBackupManager {

    companion object {
        private const val TAG = "DefaultBackupManager"

        private fun deleteFileSafely(file: File): Boolean {
            if (file.exists()) {
                return file.delete()
            }
            return true
        }
    }

    override suspend fun backupDatabase(destinationUri: Uri) {
        val dbFile = context.getDatabasePath(AppDatabase.DATABASE_NAME)
        if (dbFile == null || !dbFile.exists()) {
            throw LogManager.uException(TAG, context.getString(R.string.error_db_file_not_found))
        }

        val outputStream = context.contentResolver.openOutputStream(destinationUri)
            ?: throw LogManager.uException(TAG, context.getString(R.string.error_output_stream_unavailable))

        outputStream.use { outStream ->
            FileInputStream(dbFile).use { inputStream ->
                inputStream.copyTo(outStream)
            }
        }
    }

    override suspend fun restoreDatabase(sourceUri: Uri) {
        val dbFileRaw = context.getDatabasePath(AppDatabase.DATABASE_NAME)
            ?: throw LogManager.uException(TAG, context.getString(R.string.error_db_path_unavailable))

        val dbPath = dbFileRaw.absoluteFile.parent
            ?: throw LogManager.uException(TAG, context.getString(R.string.error_db_parent_dir_unavailable))

        val dbName = dbFileRaw.name
        val dbFile = File(dbPath, dbName)
        if (!dbFile.exists()) {
            throw LogManager.uException(TAG, context.getString(R.string.error_db_file_not_found))
        }

        val walFile = File(dbPath, "$dbName-wal")
        val shmFile = File(dbPath, "$dbName-shm")

        databaseCloser()
        delay(500.milliseconds)

        val delDb = deleteFileSafely(dbFile)
        val delWal = deleteFileSafely(walFile)
        val delShm = deleteFileSafely(shmFile)

        if (!(delDb and delWal and delShm)) {
            throw LogManager.uException(TAG, context.getString(R.string.restore_failed_delete))
        }

        val inputStream = context.contentResolver.openInputStream(sourceUri)
            ?: throw LogManager.uException(TAG, "Flux d'entrée nul")

        inputStream.use { input ->
            FileOutputStream(dbFile).use { outputStream ->
                input.copyTo(outputStream)
            }
        }
    }
}
