package com.chefinventory.app.data

import android.net.Uri

/**
 * Interface de gestion des opérations physiques de sauvegarde et de restauration de la base de données.
 */
interface DatabaseBackupManager {
    suspend fun backupDatabase(destinationUri: Uri)
    suspend fun restoreDatabase(sourceUri: Uri)
}
