package com.example.chiefinventory.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.chiefinventory.model.Ingredient

@Dao
interface IngredientDao {
    @Query("SELECT * FROM ingredients ORDER BY name")
    fun getAll(): LiveData<List<Ingredient>>

    @Query("SELECT * FROM ingredients")
    suspend fun getAllSync(): List<Ingredient>

    @Query("SELECT * FROM ingredients WHERE locationId = :locationId ORDER BY name")
    fun getByLocation(locationId: Long): LiveData<List<Ingredient>>

    @Query("SELECT * FROM ingredients WHERE id = :id")
    suspend fun getById(id: Long): Ingredient?

    @Query("SELECT * FROM ingredients WHERE name = :name LIMIT 1")
    suspend fun getByName(name: String): Ingredient?

    @Query("""
        SELECT * FROM ingredients 
        WHERE (:locationId = -1 OR locationId = :locationId)
        AND (name LIKE :query OR description LIKE :query OR ocrText LIKE :query)
        ORDER BY name
    """)
    fun searchInLocation(query: String, locationId: Long): LiveData<List<Ingredient>>

    @Query("SELECT * FROM ingredients WHERE imageEmbedding IS NOT NULL")
    suspend fun getAllWithEmbeddings(): List<Ingredient>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(ingredient: Ingredient): Long

    @Update
    suspend fun update(ingredient: Ingredient)

    @Delete
    suspend fun delete(ingredient: Ingredient)
}
