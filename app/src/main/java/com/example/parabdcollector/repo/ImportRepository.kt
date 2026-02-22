package com.example.parabdcollector.repo

import androidx.lifecycle.LiveData
import com.example.parabdcollector.dao.ImportSessionDao
import com.example.parabdcollector.model.ImportSession

/**
 * Repository for managing import session data.
 */
class ImportRepository(private val importSessionDao: ImportSessionDao) {

    /**
     * Returns all import sessions ordered by timestamp descending.
     */
    val allSessions: LiveData<List<ImportSession>> = importSessionDao.getAllSessions()

    /**
     * Retrieves a specific session by its ID.
     */
    suspend fun getSessionById(sessionId: Long): ImportSession? {
        return importSessionDao.getSessionById(sessionId)
    }

    /**
     * Inserts a new import session and returns its ID.
     */
    suspend fun insert(session: ImportSession): Long {
        return importSessionDao.insert(session)
    }

    /**
     * Updates an existing import session.
     */
    suspend fun update(session: ImportSession) {
        importSessionDao.update(session)
    }

    /**
     * Deletes an import session.
     */
    suspend fun delete(session: ImportSession) {
        importSessionDao.delete(session)
    }
}
