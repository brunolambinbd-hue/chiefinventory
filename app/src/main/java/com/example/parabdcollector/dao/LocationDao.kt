package com.example.parabdcollector.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.parabdcollector.model.Location

@Dao
interface LocationDao {

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
