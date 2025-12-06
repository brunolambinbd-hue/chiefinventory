package com.example.parabdcollector.repo

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import androidx.sqlite.db.SimpleSQLiteQuery
import com.example.imagecomparison.EmbeddingUtils
import com.example.parabdcollector.dao.CollectionDao
import com.example.parabdcollector.model.CollectionItem
import com.example.parabdcollector.model.SearchCriteria
import com.example.parabdcollector.model.SignatureStats
import com.example.parabdcollector.ui.model.CategoryInfo
import com.example.parabdcollector.ui.model.ItemCountForLocation
import com.example.parabdcollector.ui.model.SearchResultItem
import com.example.parabdcollector.utils.CategoryMapper

/**
 * Repository for managing all data operations for [CollectionItem] entities.
 *
 * This class acts as a single source of truth for all collection data, abstracting the data sources
 * (in this case, the [CollectionDao]) from the ViewModels.
 *
 * @property collectionDao The Data Access Object for collection items, provided via constructor injection.
 */
class CollectionRepository(private val collectionDao: CollectionDao) {

    /**
     * Retrieves all collection items from the database.
     * @return A [LiveData] list of all [CollectionItem]s.
     */
    fun getAll(): LiveData<List<CollectionItem>> {
        return collectionDao.getAll()
    }

    /**
     * Counts the number of items in each location.
     * @return A LiveData list of [ItemCountForLocation] objects.
     */
    fun getItemCountByLocation(): LiveData<List<ItemCountForLocation>> {
        return collectionDao.getItemCountByLocation()
    }

    /**
     * Retrieves all items for a given location ID.
     * @param locationId The ID of the location.
     * @return A [LiveData] list of items in that location.
     */
    fun getItemsByLocationId(locationId: Long): LiveData<List<CollectionItem>> {
        return collectionDao.getItemsByLocationId(locationId)
    }

    /**
     * Computes and returns live statistics about the state of image embeddings in the collection.
     * This is a transformation on the `getAll()` LiveData.
     * @return A [LiveData] object containing the [SignatureStats].
     */
    fun getSignatureStats(): LiveData<SignatureStats> {
        return getAll().map { list ->
            val total = list.size
            var valid = 0
            var empty = 0
            var missing = 0

            for (item in list) {
                when {
                    item.imageEmbedding == null -> {
                        missing++
                    }
                    item.imageEmbedding.isEmpty() -> {
                        empty++
                    }
                    else -> valid++
                }
            }
            Log.i("SignatureStats", "Calcul terminé: Valides=$valid, Vides=$empty, Manquantes=$missing, Total=$total")
            SignatureStats(total, valid, empty, missing)
        }
    }

    /**
     * Retrieves all items that the user possesses.
     * @return A [LiveData] list of possessed [CollectionItem]s.
     */
    fun getAllPossessed(): LiveData<List<CollectionItem>> {
        return collectionDao.getAllPossessed()
    }

    /**
     * Retrieves all items that the user is seeking.
     * @return A [LiveData] list of sought [CollectionItem]s.
     */
    fun getAllSought(): LiveData<List<CollectionItem>> {
        return collectionDao.getAllSought()
    }

    /**
     * Gets the total number of items in the collection.
     * @return A [LiveData] containing the total count.
     */
    fun getTotalCount(): LiveData<Int> {
        return collectionDao.getTotalCount()
    }

    /**
     * Retrieves a single item by its local primary key.
     * @param id The local database ID of the item.
     * @return A [LiveData] object containing the requested [CollectionItem].
     */
    fun getById(id: Long): LiveData<CollectionItem> {
        return collectionDao.getById(id)
    }

    /**
     * Synchronously retrieves a single item by its local primary key.
     * @param id The local database ID of the item.
     * @return The [CollectionItem], or null if not found.
     */
    suspend fun getItemById(id: Long): CollectionItem? {
        return collectionDao.getItemById(id)
    }

    /**
     * Finds a single item by its external (remote) ID. This is a synchronous, blocking operation.
     * @param remoteId The remote ID to search for.
     * @return The matching [CollectionItem], or null if not found.
     */
    fun findByRemoteId(remoteId: Int): CollectionItem? {
        return collectionDao.findByRemoteId(remoteId)
    }

    /**
     * Performs a simple, full-text search across multiple fields.
     * @param query The search term.
     * @return A list of matching [CollectionItem]s.
     */
    suspend fun search(query: String): List<CollectionItem> {
        return collectionDao.search("%${query}%")
    }

    /**
     * Performs a complex search based on a set of criteria and an optional image embedding.
     *
     * This method dynamically builds a SQL query based on the provided [SearchCriteria].
     * If an image embedding is provided, it first filters by text criteria and then calculates
     * the cosine similarity to find the most visually similar items.
     *
     * @param criteria The set of text-based search criteria.
     * @param queryEmbedding The float array of the image to search for, or null.
     * @return A list of [SearchResultItem], potentially including similarity scores.
     */
    suspend fun advancedSearch(criteria: SearchCriteria, queryEmbedding: FloatArray?): List<SearchResultItem> {
        val queryBuilder = StringBuilder("SELECT * FROM collection_items WHERE 1=1")
        val args = mutableListOf<Any?>()

        criteria.titre?.takeIf { it.isNotBlank() }?.let {
            queryBuilder.append(" AND titre LIKE ?")
            args.add("%$it%")
        }
        criteria.editeur?.takeIf { it.isNotBlank() }?.let {
            queryBuilder.append(" AND editeur LIKE ?")
            args.add("%$it%")
        }
        criteria.annee?.let {
            queryBuilder.append(" AND annee = ?")
            args.add(it)
        }
        criteria.mois?.let {
            queryBuilder.append(" AND mois = ?")
            args.add(it)
        }
        criteria.superCategorie?.takeIf { it.isNotBlank() }?.let {
            queryBuilder.append(" AND superCategorie = ?")
            args.add(it)
        }
        criteria.categorie?.takeIf { it.isNotBlank() }?.let {
            queryBuilder.append(" AND categorie LIKE ?")
            args.add("%$it%")
        }
        criteria.description?.takeIf { it.isNotBlank() }?.let {
            queryBuilder.append(" AND description LIKE ?")
            args.add("%$it%")
        }
        criteria.tirage?.takeIf { it.isNotBlank() }?.let {
            queryBuilder.append(" AND tirage LIKE ?")
            args.add("%$it%")
        }
        criteria.dimensions?.takeIf { it.isNotBlank() }?.let {
            queryBuilder.append(" AND dimensions LIKE ?")
            args.add("%$it%")
        }

        queryBuilder.append(" ORDER BY annee DESC, mois DESC")

        val sqlQuery = SimpleSQLiteQuery(queryBuilder.toString(), args.toTypedArray())
        val textFilteredItems = collectionDao.advancedSearch(sqlQuery)

        return if (queryEmbedding != null) {
            textFilteredItems
                .filter { it.imageEmbedding != null && it.imageEmbedding.isNotEmpty() }
                .map { 
                    val similarity = cosineSimilarity(queryEmbedding, it.imageEmbedding!!)
                    SearchResultItem(it, similarity.toDouble())
                }
                .filter { !(it.similarity?.isNaN() ?: true) }
                .filter { (it.similarity ?: 0.0) >= 0.65 }
                .sortedByDescending { it.similarity }
                .take(5)
        } else {
            textFilteredItems.map { SearchResultItem(it) }
        }
    }

    /**
     * Finds the most visually similar items to a given image embedding.
     * @param queryEmbedding The float array of the image to search for.
     * @return A list of the top 3 most similar [SearchResultItem]s.
     */
    suspend fun findMostSimilarItems(queryEmbedding: FloatArray): List<SearchResultItem> {
        val allItems = collectionDao.getAllItemsWithEmbeddings()
        return allItems
            .map { 
                val similarity = cosineSimilarity(queryEmbedding, it.imageEmbedding!!)
                SearchResultItem(it, similarity.toDouble())
            }
            .filter { !(it.similarity?.isNaN() ?: true) }
            .sortedByDescending { it.similarity }
            .take(3)
    }

    /**
     * Calculates the cosine similarity between two vectors.
     * @param vec1 The first vector as a [FloatArray].
     * @param vec2Bytes The second vector as a [ByteArray] from the database.
     * @return The cosine similarity score as a [Float].
     */
    private fun cosineSimilarity(vec1: FloatArray, vec2Bytes: ByteArray): Float {
        val vec2 = EmbeddingUtils.byteArrayToMyEmbedding(vec2Bytes).floatValues ?: return 0.0f
        
        var dotProduct = 0.0f
        var normA = 0.0f
        var normB = 0.0f
        for (i in vec1.indices) {
            dotProduct += vec1[i] * vec2[i]
            normA += vec1[i] * vec1[i]
            normB += vec2[i] * vec2[i]
        }
        
        val normaSqrt = kotlin.math.sqrt(normA)
        val normbSqrt = kotlin.math.sqrt(normB)

        if (normaSqrt == 0.0f || normbSqrt == 0.0f) {
            return 0.0f
        }

        return dotProduct / (normaSqrt * normbSqrt)
    }

    /**
     * Returns statistical information about super-categories, including possessed and total counts.
     * This implementation ensures that ALL super-categories from the [CategoryMapper] are displayed,
     * even those with a total count of 0.
     * @return A [LiveData] list of [CategoryInfo] objects.
     */
    fun getSuperCategoryInfo(): LiveData<List<CategoryInfo>> {
        val allSuperCategories = CategoryMapper.getSuperCategories()
        val categoryInfoFromDb = collectionDao.getSuperCategoryInfo()

        return categoryInfoFromDb.map { dbCounts ->
            val dbCountsMap = dbCounts.associateBy { it.name }
            allSuperCategories.map { superCategoryName ->
                val counts = dbCountsMap[superCategoryName]
                CategoryInfo(
                    name = superCategoryName,
                    possessedCount = counts?.possessedCount ?: 0,
                    totalCount = counts?.totalCount ?: 0
                )
            }
        }
    }

    /**
     * Returns statistical information about detailed categories within a given super-category.
     * This implementation ensures that ALL sub-categories from the [CategoryMapper] are displayed,
     * even those with a total count of 0.
     * @param superCategory The name of the super-category to filter by.
     * @return A [LiveData] list of [CategoryInfo] objects.
     */
    fun getCategoryInfoForSuperCategory(superCategory: String): LiveData<List<CategoryInfo>> {
        val allSubCategories = CategoryMapper.getCategoriesFor(superCategory)
        val categoryInfoFromDb = collectionDao.getCategoryInfoForSuperCategory(superCategory)

        return categoryInfoFromDb.map { dbCounts ->
            val dbCountsMap = dbCounts.associateBy { it.name }
            allSubCategories.map { subCategoryName ->
                val counts = dbCountsMap[subCategoryName]
                CategoryInfo(
                    name = subCategoryName,
                    possessedCount = counts?.possessedCount ?: 0,
                    totalCount = counts?.totalCount ?: 0
                )
            }
        }
    }

    /**
     * Returns a list of items belonging to a specific category and super-category.
     * @param superCategory The name of the super-category.
     * @param category The name of the detailed category.
     * @param isPossessed True to get possessed items, false for sought items.
     * @return A [LiveData] list of matching [CollectionItem]s.
     */
    fun getItemsBySuperCategoryAndCategory(superCategory: String, category: String, isPossessed: Boolean): LiveData<List<CollectionItem>> {
        return collectionDao.getItemsBySuperCategoryAndCategory(superCategory, category, isPossessed)
    }

    /**
     * Inserts a new item into the database. This is a suspending function.
     * @param item The [CollectionItem] to insert.
     */
    suspend fun insert(item: CollectionItem) {
        collectionDao.insert(item)
    }

    /**
     * Updates an existing item in the database. This is a suspending function.
     * @param item The [CollectionItem] to update.
     */
    suspend fun update(item: CollectionItem) {
        collectionDao.update(item)
    }

    /**
     * Deletes an item from the database. This is a suspending function.
     * @param item The [CollectionItem] to delete.
     */
    suspend fun delete(item: CollectionItem) {
        collectionDao.delete(item)
    }
}
