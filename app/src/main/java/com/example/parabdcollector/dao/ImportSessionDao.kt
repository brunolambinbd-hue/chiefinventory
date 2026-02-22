package com.example.parabdcollector.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.parabdcollector.model.ImportSession

@Dao
interface ImportSessionDao {
    @Query("SELECT * FROM import_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): LiveData<List<ImportSession>>

    @Query("SELECT * FROM import_sessions WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: Long): ImportSession?

    @Insert
    suspend fun insert(session: ImportSession): Long

    @Update
    suspend fun update(session: ImportSession)

    @Delete
    suspend fun delete(session: ImportSession)
}
