package com.example.parabdcollector.repo

import androidx.lifecycle.LiveData
import com.example.parabdcollector.dao.LocationDao
import com.example.parabdcollector.model.Location

class LocationRepository(private val locationDao: LocationDao) {

    fun getAll(): LiveData<List<Location>> {
        return locationDao.getAll()
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
