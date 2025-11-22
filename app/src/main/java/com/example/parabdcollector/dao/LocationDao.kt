package com.example.parabdcollector.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.parabdcollector.model.Location

@Dao
interface LocationDao {

    @Query("SELECT * FROM locations ORDER BY name ASC")
    fun getAll(): LiveData<List<Location>>

    @Query("SELECT * FROM locations WHERE parentLocationId IS NULL ORDER BY name ASC")
    fun getRootLocations(): LiveData<List<Location>>

    @Query("SELECT * FROM locations WHERE parentLocationId = :parentId ORDER BY name ASC")
    fun getChildren(parentId: Long): LiveData<List<Location>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(location: Location)

    @Update
    suspend fun update(location: Location)

    @Delete
    suspend fun delete(location: Location)
}
