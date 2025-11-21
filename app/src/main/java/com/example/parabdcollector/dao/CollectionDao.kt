package com.example.parabdcollector.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.parabdcollector.model.CategoryInfo
import com.example.parabdcollector.model.CollectionItem

@Dao
interface CollectionDao {

    @Query("SELECT * FROM collection_items")
    fun getAll(): LiveData<List<CollectionItem>>

    @Query("SELECT * FROM collection_items WHERE isPossessed = 1 ORDER BY titre ASC")
    fun getAllPossessed(): LiveData<List<CollectionItem>>

    @Query("SELECT * FROM collection_items WHERE isPossessed = 0 ORDER BY titre ASC")
    fun getAllSought(): LiveData<List<CollectionItem>>

    @Query("SELECT COUNT(*) FROM collection_items")
    fun getTotalCount(): LiveData<Int>

    @Query("SELECT * FROM collection_items WHERE id = :id")
    fun getById(id: Long): LiveData<CollectionItem>

    @Query("SELECT * FROM collection_items WHERE remoteId = :remoteId")
    fun findByRemoteId(remoteId: Int): CollectionItem?

    @Query("SELECT * FROM collection_items WHERE titre LIKE :query OR editeur LIKE :query OR CAST(annee AS TEXT) LIKE :query OR categorie LIKE :query OR materiau LIKE :query OR tirage LIKE :query OR dimensions LIKE :query ORDER BY annee DESC, mois DESC")
    suspend fun search(query: String): List<CollectionItem>

    @Query(
        """
        SELECT * FROM collection_items WHERE 
            (:titre IS NULL OR titre LIKE :titre) AND
            (:editeur IS NULL OR editeur LIKE :editeur) AND
            (:annee IS NULL OR annee = :annee) AND
            (:mois IS NULL OR mois = :mois) AND
            (:superCategorie IS NULL OR superCategorie = :superCategorie) AND
            (:categorie IS NULL OR categorie LIKE :categorie) AND
            (:description IS NULL OR description LIKE :description) AND
            (:tirage IS NULL OR tirage LIKE :tirage) AND
            (:dimensions IS NULL OR dimensions LIKE :dimensions)
        ORDER BY annee DESC, mois DESC
        """
    )
    suspend fun advancedSearch(titre: String?, editeur: String?, annee: Int?, mois: Int?, superCategorie: String?, categorie: String?, description: String?, tirage: String?, dimensions: String?): List<CollectionItem>

    @Query("SELECT superCategorie as name, COUNT(*) as count FROM collection_items WHERE isPossessed = :isPossessed AND superCategorie IS NOT NULL AND superCategorie != '' GROUP BY superCategorie ORDER BY superCategorie ASC")
    fun getSuperCategoryInfo(isPossessed: Boolean): LiveData<List<CategoryInfo>>

    @Query("SELECT categorie as name, COUNT(*) as count FROM collection_items WHERE superCategorie = :superCategory AND isPossessed = :isPossessed AND categorie IS NOT NULL AND categorie != '' GROUP BY categorie ORDER BY categorie ASC")
    fun getCategoryInfoForSuperCategory(superCategory: String, isPossessed: Boolean): LiveData<List<CategoryInfo>>

    @Query("SELECT * FROM collection_items WHERE superCategorie = :superCategory AND categorie = :category AND isPossessed = :isPossessed ORDER BY titre ASC")
    fun getItemsBySuperCategoryAndCategory(superCategory: String, category: String, isPossessed: Boolean): LiveData<List<CollectionItem>>


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: CollectionItem)

    @Update
    suspend fun update(item: CollectionItem)

    @Delete
    suspend fun delete(item: CollectionItem)
}
