package com.example.parabdcollector.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.parabdcollector.model.Location

/**
 * Data Access Object for the [Location] entity.
 * Defines the SQL queries used to interact with the locations table.
 */
@Dao
interface LocationDao {

    /**
     * Selects all locations from the table, ordered alphabetically by name.
     * @return A [LiveData] list of all [Location]s.
     */
    @Query("SELECT * FROM locations ORDER BY name ASC")
    fun getAll(): LiveData<List<Location>>

    /**
     * Selects only the root locations (those without a parent).
     * @return A [LiveData] list of root [Location]s.
     */
    @Query("SELECT * FROM locations WHERE parentLocationId IS NULL ORDER BY name ASC")
    fun getRootLocations(): LiveData<List<Location>>

    /**
     * Selects all direct children of a given parent location.
     * @param parentId The ID of the parent location.
     * @return A [LiveData] list of child [Location]s.
     */
    @Query("SELECT * FROM locations WHERE parentLocationId = :parentId ORDER BY name ASC")
    fun getChildren(parentId: Long): LiveData<List<Location>>

    /**
     * Inserts a new location. If a location with the same ID already exists, it is replaced.
     * @param location The [Location] to insert.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(location: Location)

    /**
     * Updates an existing location.
     * @param location The [Location] to update.
     */
    @Update
    suspend fun update(location: Location)

    /**
     * Deletes a location. Due to the table's foreign key constraints, this will also delete all its children.
     * @param location The [Location] to delete.
     */
    @Delete
    suspend fun delete(location: Location)
}
