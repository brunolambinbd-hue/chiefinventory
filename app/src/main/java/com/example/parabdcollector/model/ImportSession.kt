package com.example.parabdcollector.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a metadata session for a CSV import.
 * Tracks when the import happened, which file was used, and the summary of changes.
 */
@Entity(tableName = "import_sessions")
data class ImportSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** Timestamp of when the session started. */
    val timestamp: Long = System.currentTimeMillis(),
    /** Name of the imported file. */
    val fileName: String,
    /** Number of new items created during this session. */
    val itemsAdded: Int = 0,
    /** Number of existing items updated during this session. */
    val itemsUpdated: Int = 0,
    /** Current status of the import: "IN_PROGRESS", "SUCCESS", "ERROR". */
    val status: String = "SUCCESS"
)
