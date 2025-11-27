package com.example.parabdcollector.repo

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.example.parabdcollector.dao.CollectionDao
import com.example.parabdcollector.model.CategoryInfo
import com.example.parabdcollector.model.CollectionItem
import com.example.parabdcollector.model.SearchResultItem
import com.example.parabdcollector.model.SignatureStats
import com.example.parabdcollector.ui.SearchCriteria
import java.nio.ByteBuffer
import java.nio.ByteOrder
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
        return collectionDao.search("%${query}%")
    }

    suspend fun advancedSearch(criteria: SearchCriteria, queryEmbedding: FloatArray?): List<SearchResultItem> {
        // 1. On filtre par les critères texte
        val textFilteredItems = collectionDao.advancedSearch(
            titre = criteria.titre?.let { "%it%" },
            editeur = criteria.editeur?.let { "%it%" },
            annee = criteria.annee,
            mois = criteria.mois,
            superCategorie = criteria.superCategorie,
            categorie = criteria.categorie?.let { "%it%" },
            description = criteria.description?.let { "%it%" },
            tirage = criteria.tirage?.let { "%it%" },
            dimensions = criteria.dimensions?.let { "%it%" }
        )

        // 2. Si une image est fournie, on calcule la similarité sur les résultats pré-filtrés
        if (queryEmbedding != null) {
            return textFilteredItems
                .filter { it.imageEmbedding != null && it.imageEmbedding.isNotEmpty() }
                .map { 
                    val similarity = cosineSimilarity(queryEmbedding, it.imageEmbedding!!)
                    SearchResultItem(it, similarity.toDouble())
                }
                .filter { (it.similarity ?: 0.0) >= 0.65 } // On ne garde que les résultats pertinents
                .sortedByDescending { it.similarity }
                .take(5)
        } else {
            // Sinon, on retourne simplement les résultats du texte
            return textFilteredItems.map { SearchResultItem(it) }
        }
    }

    suspend fun findSimilarItems(queryEmbedding: FloatArray): List<SearchResultItem> {
        val allItems = collectionDao.getAllItems()

        return allItems
            .filter { it.imageEmbedding != null && it.imageEmbedding.isNotEmpty() }
            .map {
                val similarity = cosineSimilarity(queryEmbedding, it.imageEmbedding!!)
                SearchResultItem(it, similarity.toDouble())
            }
            .filter { !(it.similarity?.isNaN() ?: true) } // On exclut les NaN
            .filter { (it.similarity ?: 0.0) >= 0.65 } // On ne garde que les résultats pertinents
            .sortedByDescending { it.similarity }
            .take(5)
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
        
        val normA_sqrt = kotlin.math.sqrt(normA)
        val normB_sqrt = kotlin.math.sqrt(normB)

        // On évite la division par zéro
        if (normA_sqrt == 0.0f || normB_sqrt == 0.0f) {
            return 0.0f
        }

        return dotProduct / (normA_sqrt * normB_sqrt)
    }

    private fun toFloatArray(bytes: ByteArray): FloatArray {
        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
        val floatArray = FloatArray(bytes.size / 4)
        buffer.asFloatBuffer().get(floatArray)
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
