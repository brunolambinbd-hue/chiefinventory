package com.example.chiefinventory.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
import com.example.chiefinventory.model.CollectionItem

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

    @Query("SELECT * FROM collection_items WHERE isPossessed = 1 ORDER BY annee DESC, mois DESC")
    fun getAllPossessed(): LiveData<List<CollectionItem>>

    @Query("SELECT * FROM collection_items WHERE isPossessed = 0 ORDER BY annee DESC, mois DESC")
    fun getAllSought(): LiveData<List<CollectionItem>>

    @Query("SELECT * FROM collection_items WHERE locationId IS NULL AND isPossessed = 1")
    fun getUnlocatedItems(): LiveData<List<CollectionItem>>

    @Query("SELECT * FROM collection_items WHERE locationId IS NOT NULL AND isPossessed = 0")
    fun getLocatedNotPossessedItems(): LiveData<List<CollectionItem>>

    @Query("SELECT * FROM collection_items WHERE isPossessed = 1 ORDER BY updatedAt DESC LIMIT 50")
    fun getRecentPossessed(): LiveData<List<CollectionItem>>

    @Query("SELECT * FROM collection_items WHERE locationId IS NOT NULL ORDER BY updatedAt DESC LIMIT 50")
    fun getRecentLocated(): LiveData<List<CollectionItem>>

    @Query("SELECT COUNT(id) FROM collection_items")
    fun getTotalCount(): LiveData<Int>

    @Query("SELECT * FROM collection_items WHERE id = :id")
    fun getById(id: Long): LiveData<CollectionItem>

    @Query("SELECT * FROM collection_items WHERE id = :id")
    suspend fun getItemById(id: Long): CollectionItem?

    @Query("SELECT * FROM collection_items WHERE remoteId = :remoteId")
    fun findByRemoteId(remoteId: Int): CollectionItem?

    @Query("SELECT * FROM collection_items WHERE (titre LIKE :query OR editeur LIKE :query OR description LIKE :query OR ocrText LIKE :query) ORDER BY annee DESC, mois DESC LIMIT 50")
    suspend fun search(query: String): List<CollectionItem>

    @Query("SELECT * FROM collection_items WHERE titre LIKE :query")
    suspend fun getAllByTitle(query: String): List<CollectionItem>

    @RawQuery
    suspend fun advancedSearch(query: SupportSQLiteQuery): List<CollectionItem>

    @RawQuery
    suspend fun countAdvancedSearch(query: SupportSQLiteQuery): Int

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

    @Query("""
        SELECT 
            superCategorie as name, 
            SUM(CASE WHEN isPossessed = 1 THEN 1 ELSE 0 END) as possessedCount, 
            COUNT(id) as totalCount 
        FROM collection_items 
        WHERE superCategorie IS NOT NULL AND superCategorie != '' 
        GROUP BY superCategorie
    """)
    suspend fun getSuperCategoryInfoSuspend(): List<CategoryInfoFromDb>

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

    @Query("""
        SELECT 
            categorie as name, 
            SUM(CASE WHEN isPossessed = 1 THEN 1 ELSE 0 END) as possessedCount, 
            COUNT(id) as totalCount 
        FROM collection_items 
        WHERE superCategorie = :superCategory AND categorie IS NOT NULL AND categorie != ''
        GROUP BY categorie
    """)
    suspend fun getCategoryInfoForSuperCategorySuspend(superCategory: String): List<CategoryInfoFromDb>

    @Query("SELECT * FROM collection_items WHERE superCategorie = :superCategory AND categorie = :category AND isPossessed = :isPossessed ORDER BY annee DESC, mois DESC")
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
