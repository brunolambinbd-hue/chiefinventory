package com.example.parabdcollector.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Update
import androidx.sqlite.db.SupportSQLiteQuery
import com.example.parabdcollector.model.CollectionItem

@Dao
interface CollectionDao {

    @Query("SELECT * FROM collection_items")
    fun getAll(): LiveData<List<CollectionItem>>

    @Query("SELECT * FROM collection_items")
    suspend fun getAllSuspend(): List<CollectionItem>
    
    @Query("SELECT * FROM collection_items WHERE imageEmbedding IS NOT NULL")
    suspend fun getAllItemsWithEmbeddings(): List<CollectionItem>

    @Query("SELECT locationId, COUNT(id) as count FROM collection_items WHERE locationId IS NOT NULL GROUP BY locationId")
    fun getItemCountByLocation(): LiveData<List<ItemCountForLocation>>

    @Query("SELECT * FROM collection_items WHERE locationId = :locationId")
    fun getItemsByLocationId(locationId: Long): LiveData<List<CollectionItem>>

    @Query("SELECT * FROM collection_items WHERE isPossessed = 1")
    fun getAllPossessed(): LiveData<List<CollectionItem>>

    @Query("SELECT * FROM collection_items WHERE isPossessed = 0")
    fun getAllSought(): LiveData<List<CollectionItem>>

    @Query("SELECT COUNT(id) FROM collection_items")
    fun getTotalCount(): LiveData<Int>

    @Query("SELECT * FROM collection_items WHERE id = :id")
    fun getById(id: Long): LiveData<CollectionItem>

    @Query("SELECT * FROM collection_items WHERE id = :id")
    suspend fun getItemById(id: Long): CollectionItem?

    @Query("SELECT * FROM collection_items WHERE remoteId = :remoteId")
    fun findByRemoteId(remoteId: Int): CollectionItem?

    @Query("SELECT * FROM collection_items WHERE isPossessed = 0 AND (titre LIKE :query OR editeur LIKE :query OR description LIKE :query)")
    suspend fun search(query: String): List<CollectionItem>

    @RawQuery
    suspend fun advancedSearch(query: SupportSQLiteQuery): List<CollectionItem>

    /**
     * Gathers statistics for all super-categories.
     * For each super-category, it counts the number of items possessed and the total number of items.
     */
    @Query("""
        SELECT 
            superCategorie as name, 
            SUM(CASE WHEN isPossessed = 1 THEN 1 ELSE 0 END) as possessedCount, 
            COUNT(id) as totalCount 
        FROM collection_items 
        WHERE superCategorie IS NOT NULL AND superCategorie != '' 
        GROUP BY superCategorie
    """)
    fun getSuperCategoryInfo(): LiveData<List<CategoryInfoFromDb>>

    /**
     * Gathers statistics for all detailed categories within a given super-category.
     * @param superCategory The super-category to filter by.
     */
    @Query("""
        SELECT 
            categorie as name, 
            SUM(CASE WHEN isPossessed = 1 THEN 1 ELSE 0 END) as possessedCount, 
            COUNT(id) as totalCount 
        FROM collection_items 
        WHERE superCategorie = :superCategory AND categorie IS NOT NULL AND categorie != ''
        GROUP BY categorie
    """)
    fun getCategoryInfoForSuperCategory(superCategory: String): LiveData<List<CategoryInfoFromDb>>

    @Query("SELECT * FROM collection_items WHERE superCategorie = :superCategory AND categorie = :category AND isPossessed = :isPossessed")
    fun getItemsBySuperCategoryAndCategory(superCategory: String, category: String, isPossessed: Boolean): LiveData<List<CollectionItem>>
    
    @Query("SELECT id, titre, imageUri, imageEmbedding FROM collection_items")
    fun getSignatureReportItems(): LiveData<List<SignatureReportItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: CollectionItem)

    @Update
    suspend fun update(item: CollectionItem)

    @Delete
    suspend fun delete(item: CollectionItem)
}
