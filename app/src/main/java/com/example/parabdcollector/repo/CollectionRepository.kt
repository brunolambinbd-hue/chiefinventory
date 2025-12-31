package com.example.parabdcollector.repo

import android.util.Log
import androidx.annotation.ColorRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.example.imagecomparison.EmbeddingUtils
import com.example.parabdcollector.R
import com.example.parabdcollector.dao.CollectionDao
import com.example.parabdcollector.dao.ItemCountForLocation
import com.example.parabdcollector.model.CollectionItem
import com.example.parabdcollector.model.SearchCriteria
import com.example.parabdcollector.model.SignatureStats
import com.example.parabdcollector.ui.model.CategoryInfo
import com.example.parabdcollector.ui.model.SearchResultItem
import com.example.parabdcollector.utils.CategoryMapper
import kotlinx.coroutines.Dispatchers

/**
 * Repository for managing all data operations for [CollectionItem] entities.
 *
 * This class acts as a single source of truth for all collection data, abstracting the data sources
 * (in this case, the [CollectionDao]) from the ViewModels.
 *
 * @property collectionDao The Data Access Object for collection items, provided via constructor injection.
 */
open class CollectionRepository(private val collectionDao: CollectionDao) {

    fun getAll(): LiveData<List<CollectionItem>> {
        return collectionDao.getAll()
    }

    fun getItemCountByLocation(): LiveData<List<ItemCountForLocation>> {
        return collectionDao.getItemCountByLocation()
    }

    fun getItemsByLocationId(locationId: Long): LiveData<List<CollectionItem>> {
        return collectionDao.getItemsByLocationId(locationId)
    }

    fun getSignatureStats(): LiveData<SignatureStats> = liveData(Dispatchers.IO) {
        val list = collectionDao.getAllSuspend()
        val total = list.size
        var valid = 0
        var empty = 0
        var missing = 0

        for (item in list) {
            when {
                item.imageEmbedding == null -> missing++
                item.imageEmbedding.isEmpty() -> empty++
                else -> valid++
            }
        }
        Log.i("SignatureStats", "Calculation complete: Valid=$valid, Empty=$empty, Missing=$missing, Total=$total")
        emit(SignatureStats(total, valid, empty, missing))
    }

    fun getAllPossessed(): LiveData<List<CollectionItem>> {
        return collectionDao.getAllPossessed()
    }

    fun getAllSought(): LiveData<List<CollectionItem>> {
        return collectionDao.getAllSought()
    }

    fun getTotalCount(): LiveData<Int> {
        return collectionDao.getTotalCount()
    }

    fun getById(id: Long): LiveData<CollectionItem> {
        return collectionDao.getById(id)
    }

    suspend fun getItemById(id: Long): CollectionItem? {
        return collectionDao.getItemById(id)
    }

    fun findByRemoteId(remoteId: Int): CollectionItem? {
        return collectionDao.findByRemoteId(remoteId)
    }

    open suspend fun search(query: String): List<CollectionItem> {
        return collectionDao.search("%${query}%")
    }

    open suspend fun advancedSearch(criteria: SearchCriteria, queryEmbedding: FloatArray?): List<SearchResultItem> {
        val queryBuilder = StringBuilder("SELECT * FROM collection_items")
        val args = mutableListOf<Any?>()
        var conditions = mutableListOf<String>()

        criteria.titre?.takeIf { it.isNotBlank() }?.let {
            conditions.add("titre LIKE ?")
            args.add("%$it%")
        }
        criteria.editeur?.takeIf { it.isNotBlank() }?.let {
            conditions.add("editeur LIKE ?")
            args.add("%$it%")
        }
        criteria.annee?.let {
            conditions.add("annee = ?")
            args.add(it)
        }
        criteria.mois?.let {
            conditions.add("mois = ?")
            args.add(it)
        }
        criteria.superCategorie?.takeIf { it.isNotBlank() }?.let {
            conditions.add("superCategorie = ?")
            args.add(it)
        }
        criteria.categorie?.takeIf { it.isNotBlank() }?.let {
            conditions.add("categorie LIKE ?")
            args.add("%$it%")
        }
        criteria.description?.takeIf { it.isNotBlank() }?.let {
            conditions.add("description LIKE ?")
            args.add("%$it%")
        }
        criteria.tirage?.takeIf { it.isNotBlank() }?.let {
            conditions.add("tirage LIKE ?")
            args.add("%$it%")
        }
        criteria.dimensions?.takeIf { it.isNotBlank() }?.let {
            conditions.add("dimensions LIKE ?")
            args.add("%$it%")
        }
        
        if (conditions.isNotEmpty()) {
            queryBuilder.append(" WHERE ").append(conditions.joinToString(" AND "))
        }

        queryBuilder.append(" ORDER BY annee DESC, mois DESC")

        val sqlQuery = androidx.sqlite.db.SimpleSQLiteQuery(queryBuilder.toString(), args.toTypedArray())
        val textFilteredItems = collectionDao.advancedSearch(sqlQuery)

        return if (queryEmbedding != null) {
            textFilteredItems
                .filter { it.imageEmbedding != null && it.imageEmbedding.isNotEmpty() }
                .map { 
                    val similarity = cosineSimilarity(queryEmbedding, it.imageEmbedding!!)
                    SearchResultItem(it, similarity.toDouble())
                }
                .filter { it.similarity != null && it.similarity >= 0.65 }
                .sortedByDescending { it.similarity }
                .take(5)
        } else {
            textFilteredItems.map { SearchResultItem(it) }
        }
    }

    /**
     * Finds the most visually similar items to a given image embedding, searching through ALL items.
     * @param queryEmbedding The float array of the image to search for.
     * @return A list of the top 3 most similar [SearchResultItem]s, including their ownership status.
     */
    suspend fun findMostSimilarItems(queryEmbedding: FloatArray): List<SearchResultItem> {
        val allItems = collectionDao.getAllItemsWithEmbeddings() // This fetches all items with a signature
        return allItems
            .map { 
                val similarity = cosineSimilarity(queryEmbedding, it.imageEmbedding!!)
                SearchResultItem(it, similarity.toDouble())
            }
            .filter { !(it.similarity?.isNaN() ?: true) }
            .sortedByDescending { it.similarity }
            .take(3)
    }

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

    @ColorRes
    private fun getStatusColor(possessedCount: Int, totalCount: Int, isSoughtMode: Boolean): Int {
        if (totalCount == 0) return R.color.status_error

        val percentage = (possessedCount * 100) / totalCount
        return if (isSoughtMode) {
            when {
                percentage > 75 -> R.color.status_ok // Almost complete, few sought
                percentage > 25 -> R.color.status_warning
                else -> R.color.status_error // Not complete at all, many sought
            }
        } else {
            when {
                percentage < 25 -> R.color.status_error
                percentage < 75 -> R.color.status_warning
                else -> R.color.status_ok
            }
        }
    }

    fun getSuperCategoryInfo(isSoughtMode: Boolean): LiveData<List<CategoryInfo>> = liveData(Dispatchers.IO) {
        val allSuperCategories = CategoryMapper.getSuperCategories()
        val dbCounts = collectionDao.getSuperCategoryInfoSuspend()
        val dbCountsMap = dbCounts.associateBy { it.name }

        val categoryInfos = allSuperCategories.map { superCategoryName ->
            val counts = dbCountsMap[superCategoryName]
            val possessed = counts?.possessedCount ?: 0
            val total = counts?.totalCount ?: 0
            CategoryInfo(
                name = superCategoryName,
                possessedCount = possessed,
                totalCount = total,
                statusColorRes = getStatusColor(possessed, total, isSoughtMode)
            )
        }
        emit(categoryInfos)
    }

    fun getCategoryInfoForSuperCategory(superCategory: String, isSoughtMode: Boolean): LiveData<List<CategoryInfo>> = liveData(Dispatchers.IO) {
        val allSubCategories = CategoryMapper.getCategoriesFor(superCategory)
        val dbCounts = collectionDao.getCategoryInfoForSuperCategorySuspend(superCategory)
        val dbCountsMap = dbCounts.associateBy { it.name }

        val categoryInfos = allSubCategories.map { subCategoryName ->
            val counts = dbCountsMap[subCategoryName]
            val possessed = counts?.possessedCount ?: 0
            val total = counts?.totalCount ?: 0
            CategoryInfo(
                name = subCategoryName,
                possessedCount = possessed,
                totalCount = total,
                statusColorRes = getStatusColor(possessed, total, isSoughtMode)
            )
        }
        emit(categoryInfos)
    }

    fun getItemsBySuperCategoryAndCategory(superCategory: String, category: String, isSoughtMode: Boolean): LiveData<List<CollectionItem>> {
        return collectionDao.getItemsBySuperCategoryAndCategory(superCategory, category, isSoughtMode)
    }

    suspend fun insert(item: CollectionItem) {
        collectionDao.insert(item)
    }

    suspend fun update(item: CollectionItem) {
        collectionDao.update(item)
    }

    suspend fun delete(item: CollectionItem) {
        collectionDao.delete(item)
    }
}
