package com.example.parabdcollector.repo

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import androidx.sqlite.db.SimpleSQLiteQuery
import com.example.imagecomparison.EmbeddingUtils
import com.example.parabdcollector.dao.CollectionDao
import com.example.parabdcollector.model.CategoryInfo
import com.example.parabdcollector.model.CollectionItem
import com.example.parabdcollector.model.SearchCriteria
import com.example.parabdcollector.model.SearchResultItem
import com.example.parabdcollector.model.SignatureStats

class CollectionRepository(private val collectionDao: CollectionDao) {

    fun getAll(): LiveData<List<CollectionItem>> {
        return collectionDao.getAll()
    }

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

    fun findByRemoteId(remoteId: Int): CollectionItem? {
        return collectionDao.findByRemoteId(remoteId)
    }

    suspend fun search(query: String): List<CollectionItem> {
        return collectionDao.search("%${query}%")
    }

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

    fun getSuperCategoryInfo(isPossessed: Boolean): LiveData<List<CategoryInfo>> {
        return collectionDao.getSuperCategoryInfo(isPossessed)
    }

    fun getCategoryInfoForSuperCategory(superCategory: String, isPossessed: Boolean): LiveData<List<CategoryInfo>> {
        return collectionDao.getCategoryInfoForSuperCategory(superCategory, isPossessed)
    }

    fun getItemsBySuperCategoryAndCategory(superCategory: String, category: String, isPossessed: Boolean): LiveData<List<CollectionItem>> {
        return collectionDao.getItemsBySuperCategoryAndCategory(superCategory, category, isPossessed)
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
