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
import com.example.parabdcollector.ui.model.CategoryInfo
import com.example.parabdcollector.model.CollectionItem
import com.example.parabdcollector.ui.model.ItemCountForLocation

/**
 * Data Access Object for the [CollectionItem] entity.
 * Defines the SQL queries used to interact with the collection_items table.
 */
@Dao
interface CollectionDao {

    /**
     * Selects all items from the table, ordered by title.
     * @return A [LiveData] list of all [CollectionItem]s.
     */
    @Query("SELECT * FROM collection_items ORDER BY titre ASC")
    fun getAll(): LiveData<List<CollectionItem>>

    /**
     * Synchronously selects all items from the table. Used for non-UI related tasks.
     * @return A simple list of all [CollectionItem]s.
     */
    @Query("SELECT * FROM collection_items")
    suspend fun getAllItems(): List<CollectionItem>

    /**
     * Synchronously selects all items from the table that have a non-null and non-empty image embedding.
     * This is used as the base for similarity searches.
     * @return A list of [CollectionItem]s with valid embeddings.
     */
    @Query("SELECT * FROM collection_items WHERE imageEmbedding IS NOT NULL AND LENGTH(imageEmbedding) > 0")
    suspend fun getAllItemsWithEmbeddings(): List<CollectionItem>

    /**
     * Selects all items marked as possessed.
     * @return A [LiveData] list of possessed [CollectionItem]s.
     */
    @Query("SELECT * FROM collection_items WHERE isPossessed = 1 ORDER BY titre ASC")
    fun getAllPossessed(): LiveData<List<CollectionItem>>

    /**
     * Selects all items marked as sought (not possessed).
     * @return A [LiveData] list of sought [CollectionItem]s.
     */
    @Query("SELECT * FROM collection_items WHERE isPossessed = 0 ORDER BY titre ASC")
    fun getAllSought(): LiveData<List<CollectionItem>>

    /**
     * Gets the total count of items in the table.
     * @return A [LiveData] holding the total count.
     */
    @Query("SELECT COUNT(*) FROM collection_items")
    fun getTotalCount(): LiveData<Int>

    /**
     * Counts the number of items in each location.
     * @return A LiveData list of [ItemCountForLocation] objects.
     */
    @Query("SELECT locationId, COUNT(*) as count FROM collection_items WHERE locationId IS NOT NULL GROUP BY locationId")
    fun getItemCountByLocation(): LiveData<List<ItemCountForLocation>>

    /**
     * Selects all items for a given location ID.
     * @param locationId The ID of the location to filter by.
     * @return A LiveData list of matching [CollectionItem]s.
     */
    @Query("SELECT * FROM collection_items WHERE locationId = :locationId ORDER BY titre ASC")
    fun getItemsByLocationId(locationId: Long): LiveData<List<CollectionItem>>

    /**
     * Selects a single item by its primary key.
     * @param id The local ID of the item.
     * @return A [LiveData] holding the requested [CollectionItem].
     */
    @Query("SELECT * FROM collection_items WHERE id = :id")
    fun getById(id: Long): LiveData<CollectionItem>

    /**
     * Synchronously selects a single item by its primary key.
     * @param id The local ID of the item.
     * @return The [CollectionItem], or null if not found.
     */
    @Query("SELECT * FROM collection_items WHERE id = :id")
    suspend fun getItemById(id: Long): CollectionItem?

    /**
     * Selects a single item by its unique remote ID.
     * @param remoteId The remote ID to search for.
     * @return The matching [CollectionItem], or null if not found.
     */
    @Query("SELECT * FROM collection_items WHERE remoteId = :remoteId")
    fun findByRemoteId(remoteId: Int): CollectionItem?

    /**
     * Performs a simple full-text search across several key fields for items the user does not possess.
     * @param query The search term to find.
     * @return A list of matching, unpossessed [CollectionItem]s.
     */
    @Query("SELECT * FROM collection_items WHERE isPossessed = 0 AND (titre LIKE :query OR editeur LIKE :query OR CAST(annee AS TEXT) LIKE :query OR categorie LIKE :query OR materiau LIKE :query OR tirage LIKE :query OR dimensions LIKE :query) ORDER BY annee DESC, mois DESC")
    suspend fun search(query: String): List<CollectionItem>

    /**
     * Executes a dynamically constructed search query.
     * @param query The [SupportSQLiteQuery] built by the repository.
     * @return A list of matching [CollectionItem]s.
     */
    @RawQuery
    suspend fun advancedSearch(query: SupportSQLiteQuery): List<CollectionItem>

    /**
     * Groups items by super-category and provides counts for both possessed and total items.
     * @return A [LiveData] list of [CategoryInfo] objects for super-categories.
     */
    @Query("""
        SELECT 
            superCategorie as name, 
            SUM(isPossessed) as possessedCount, 
            COUNT(*) as totalCount 
        FROM collection_items 
        WHERE superCategorie IS NOT NULL AND superCategorie != '' 
        GROUP BY superCategorie 
        ORDER BY superCategorie ASC
    """)
    fun getSuperCategoryInfo(): LiveData<List<CategoryInfo>>

    /**
     * Groups items by detailed category within a given super-category, providing possessed and total counts.
     * @param superCategory The super-category to filter by.
     * @return A [LiveData] list of [CategoryInfo] objects for detailed categories.
     */
    @Query("""
        SELECT 
            categorie as name, 
            SUM(isPossessed) as possessedCount, 
            COUNT(*) as totalCount 
        FROM collection_items 
        WHERE superCategorie = :superCategory AND categorie IS NOT NULL AND categorie != '' 
        GROUP BY categorie 
        ORDER BY categorie ASC
    """)
    fun getCategoryInfoForSuperCategory(superCategory: String): LiveData<List<CategoryInfo>>

    /**
     * Selects all items belonging to a specific super-category and detailed category.
     * @param superCategory The super-category to filter by.
     * @param category The detailed category to filter by.
     * @param isPossessed True to select possessed items, false for sought items.
     * @return A [LiveData] list of matching [CollectionItem]s.
     */
    @Query("SELECT * FROM collection_items WHERE superCategorie = :superCategory AND categorie = :category AND isPossessed = :isPossessed ORDER BY titre ASC")
    fun getItemsBySuperCategoryAndCategory(superCategory: String, category: String, isPossessed: Boolean): LiveData<List<CollectionItem>>

    /**
     * Inserts a new item. If the remoteId already exists, the insert is ignored.
     * @param item The [CollectionItem] to insert.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: CollectionItem)

    /**
     * Updates an existing item.
     * @param item The [CollectionItem] to update.
     */
    @Update
    suspend fun update(item: CollectionItem)

    /**
     * Deletes an item.
     * @param item The [CollectionItem] to delete.
     */
    @Delete
    suspend fun delete(item: CollectionItem)
}
