package com.example.parabdcollector.repo

import androidx.lifecycle.LiveData
import com.example.parabdcollector.dao.LocationDao
import com.example.parabdcollector.model.Location

class LocationRepository(private val locationDao: LocationDao) {

    fun getAll(): LiveData<List<Location>> {
        return locationDao.getAll()
    }

    fun getRootLocations(): LiveData<List<Location>> {
        return locationDao.getRootLocations()
    }

    fun getChildren(parentId: Long): LiveData<List<Location>> {
        return locationDao.getChildren(parentId)
    }

    suspend fun insert(location: Location) {
        locationDao.insert(location)
    }

    suspend fun update(location: Location) {
        locationDao.update(location)
    }

    suspend fun delete(location: Location) {
        locationDao.delete(location)
    }
}
