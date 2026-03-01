package com.example.chiefinventory.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.chiefinventory.model.Location
import com.example.chiefinventory.model.LocationType

@Dao
interface LocationDao {

    @Query("SELECT * FROM locations WHERE type = :type ORDER BY parentId, name")
    fun getAllByType(type: LocationType): LiveData<List<Location>>

    @Query("SELECT * FROM locations ORDER BY parentId, name")
    fun getAll(): LiveData<List<Location>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(location: Location)

    @Update
    suspend fun update(location: Location)

    @Delete
    suspend fun delete(location: Location)

    @Query("UPDATE locations SET parentId = :newParentId WHERE id = :locationId")
    suspend fun updateLocationParent(locationId: Long, newParentId: Long?)
}
