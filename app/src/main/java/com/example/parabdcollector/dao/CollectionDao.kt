package com.example.parabdcollector.dao

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

    @Query("SELECT * FROM collection_items WHERE isPossessed = 1 ORDER BY titre ASC")
    fun getAllPossessed(): LiveData<List<CollectionItem>>

    @Query("SELECT * FROM collection_items WHERE isPossessed = 0 ORDER BY titre ASC")
    fun getAllSought(): LiveData<List<CollectionItem>>

    @Query("SELECT COUNT(*) FROM collection_items")
    fun getTotalCount(): LiveData<Int>

    @Query("SELECT * FROM collection_items WHERE id = :id")
    fun getById(id: Long): LiveData<CollectionItem>

    @Query("SELECT * FROM collection_items WHERE titre LIKE :query OR univers LIKE :query OR fabricant LIKE :query OR CAST(annee AS TEXT) LIKE :query OR categorie LIKE :query OR materiau LIKE :query OR tirage LIKE :query OR dimensions LIKE :query")
    fun search(query: String): LiveData<List<CollectionItem>>

    @Query("""
        SELECT * FROM collection_items WHERE 
            (:titre IS NULL OR titre LIKE :titre) AND
            (:univers IS NULL OR univers LIKE :univers) AND
            (:fabricant IS NULL OR fabricant LIKE :fabricant) AND
            (:annee IS NULL OR annee = :annee) AND
            (:categorie IS NULL OR categorie LIKE :categorie)
        """)
    fun advancedSearch(titre: String?, univers: String?, fabricant: String?, annee: Int?, categorie: String?): LiveData<List<CollectionItem>>


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: CollectionItem)

    @Update
    suspend fun update(item: CollectionItem)

    @Delete
    suspend fun delete(item: CollectionItem)
}