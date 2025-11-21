package com.example.parabdcollector.repo

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.example.parabdcollector.dao.CollectionDao
import com.example.parabdcollector.model.CategoryInfo
import com.example.parabdcollector.model.CollectionItem
import com.example.parabdcollector.model.SearchResultItem
import com.example.parabdcollector.model.SignatureStats
import kotlin.experimental.and

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
        return collectionDao.search(query)
    }

    suspend fun advancedSearch(titre: String?, editeur: String?, annee: Int?, mois: Int?, superCategorie: String?, categorie: String?, description: String?, tirage: String?, dimensions: String?): List<CollectionItem> {
        return collectionDao.advancedSearch(titre, editeur, annee, mois, superCategorie, categorie, description, tirage, dimensions)
    }

    suspend fun findSimilarItems(queryEmbedding: FloatArray): List<SearchResultItem> {
        val allItems = collectionDao.getAll().value ?: return emptyList()

        return allItems
            .filter { it.imageEmbedding != null && it.imageEmbedding.isNotEmpty() }
            .map {
                val similarity = cosineSimilarity(queryEmbedding, it.imageEmbedding!!)
                SearchResultItem(it, similarity.toDouble())
            }
            .filter { it.similarity!! > 0.8 } // Seuil de similarité
            .sortedByDescending { it.similarity }
            .take(10)
    }

    private fun cosineSimilarity(vec1: FloatArray, vec2Bytes: ByteArray): Float {
        val vec2 = toFloatArray(vec2Bytes)
        
        var dotProduct = 0.0f
        var normA = 0.0f
        var normB = 0.0f
        for (i in vec1.indices) {
            dotProduct += vec1[i] * vec2[i]
            normA += vec1[i] * vec1[i]
            normB += vec2[i] * vec2[i]
        }
        return dotProduct / (kotlin.math.sqrt(normA) * kotlin.math.sqrt(normB))
    }

    private fun toFloatArray(bytes: ByteArray): FloatArray {
        val floatArray = FloatArray(bytes.size / 4)
        for (i in floatArray.indices) {
            val intBits = (bytes[i * 4].toInt() and 0xFF) or
                    ((bytes[i * 4 + 1].toInt() and 0xFF) shl 8) or
                    ((bytes[i * 4 + 2].toInt() and 0xFF) shl 16) or
                    ((bytes[i * 4 + 3].toInt() and 0xFF) shl 24)
            floatArray[i] = Float.fromBits(intBits)
        }
        return floatArray
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
}