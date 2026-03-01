package com.example.chiefinventory.repo

import androidx.lifecycle.LiveData
import com.example.chiefinventory.dao.LocationDao
import com.example.chiefinventory.model.Location
import com.example.chiefinventory.model.LocationType

/**
 * Repository for managing all data operations for [Location] entities.
 *
 * This class acts as a single source of truth for all location data, abstracting the data source
 * (the [LocationDao]) from the ViewModels.
 *
 * @property locationDao The Data Access Object for location items, provided via constructor injection.
 */
class LocationRepository(private val locationDao: LocationDao) {

    /**
     * Retrieves all locations of a specific type.
     * @param type The [LocationType] to filter by.
     * @return A [LiveData] list of filtered [Location]s.
     */
    fun getAllByType(type: LocationType): LiveData<List<Location>> {
        return locationDao.getAllByType(type)
    }

    /**
     * Retrieves all locations from the database as a LiveData list.
     * @return A [LiveData] list of all [Location]s.
     */
    fun getAll(): LiveData<List<Location>> {
        return locationDao.getAll()
    }

    /**
     * Inserts a new location into the database. This is a suspending function.
     * @param location The [Location] to insert.
     */
    suspend fun insert(location: Location) {
        locationDao.insert(location)
    }

    /**
     * Updates an existing location in the database. This is a suspending function.
     * @param location The [Location] to update.
     */
    suspend fun update(location: Location) {
        locationDao.update(location)
    }

    /**
     * Deletes a location from the database. This is a suspending function.
     * @param location The [Location] to delete.
     */
    suspend fun delete(location: Location) {
        locationDao.delete(location)
    }

    /**
     * Updates the parent of a given location.
     * @param locationId The ID of the location to move.
     * @param newParentId The ID of the new parent. Can be null to move to the root.
     */
    suspend fun updateLocationParent(locationId: Long, newParentId: Long?) {
        locationDao.updateLocationParent(locationId, newParentId)
    }
}
