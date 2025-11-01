package com.example.parabdcollector.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.parabdcollector.model.CollectionItem

@Dao
interface CollectionDao {
    @Query("SELECT * FROM collection_items ORDER BY titre")
    fun getAll(): LiveData<List<CollectionItem>>


    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insert(item: CollectionItem): Long


    @Update
    suspend fun update(item: CollectionItem)


    @Delete
    suspend fun delete(item: CollectionItem)
}