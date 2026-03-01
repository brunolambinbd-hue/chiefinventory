package com.example.chiefinventory.repo

import androidx.annotation.ColorRes
import androidx.lifecycle.*
import com.example.chiefinventory.R
import com.example.chiefinventory.dao.CollectionDao
import com.example.chiefinventory.model.*
import com.example.chiefinventory.ui.model.CategoryInfo
import com.example.chiefinventory.ui.model.SearchResultItem
import com.example.chiefinventory.utils.CategoryMapper
import kotlinx.coroutines.Dispatchers
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Repository for managing all data operations for [CollectionItem] entities.
 */
open class CollectionRepository(private val collectionDao: CollectionDao) {
    fun getAll(): LiveData<List<CollectionItem>> = collectionDao.getAll()
    fun getUnlocatedItems(): LiveData<List<CollectionItem>> = collectionDao.getUnlocatedItems()
    fun getLocatedNotPossessedItems(): LiveData<List<CollectionItem>> = collectionDao.getLocatedNotPossessedItems()
    
    @Suppress("unused")
    fun getItemCountByLocation(): LiveData<List<com.example.chiefinventory.dao.ItemCountForLocation>> = collectionDao.getItemCountByLocation()

    fun getItemsByLocationId(lId: Long): LiveData<List<CollectionItem>> = collectionDao.getItemsByLocationId(lId)
    
    /** Retrieves the 50 most recently updated possessed items. */
    fun getRecentPossessed(): LiveData<List<CollectionItem>> = collectionDao.getRecentPossessed()

    /** Retrieves the 50 most recently updated items with a location. */
    fun getRecentLocated(): LiveData<List<CollectionItem>> = collectionDao.getRecentLocated()

    suspend fun getAllItemsSuspend(): List<CollectionItem> = collectionDao.getAllSuspend()

    fun getSignatureStats(): LiveData<SignatureStats> = liveData(Dispatchers.IO) {
        val list = collectionDao.getAllSuspend()
        var v = 0; var e = 0; var m = 0
        for (i in list) { when { i.imageEmbedding == null -> m++; i.imageEmbedding.isEmpty() -> e++; else -> v++ } }
        emit(SignatureStats(list.size, v, e, m))
    }
    fun getAllPossessed(): LiveData<List<CollectionItem>> = collectionDao.getAllPossessed()
    fun getAllSought(): LiveData<List<CollectionItem>> = collectionDao.getAllSought()
    fun getTotalCount(): LiveData<Int> = collectionDao.getTotalCount()
    fun getById(id: Long): LiveData<CollectionItem> = collectionDao.getById(id)
    suspend fun getItemById(id: Long): CollectionItem? = collectionDao.getItemById(id)
    fun findByRemoteId(rId: Int): CollectionItem? = collectionDao.findByRemoteId(rId)
    suspend fun search(q: String): List<CollectionItem> = collectionDao.search("%$q%")
    suspend fun getAllByTitle(t: String): List<CollectionItem> = collectionDao.getAllByTitle("%$t%")

    open suspend fun advancedSearch(cr: SearchCriteria, qE: FloatArray?, qOcr: String? = null): AdvancedSearchResult {
        val conditions = mutableListOf<String>(); val args = mutableListOf<Any?>()
        cr.titre?.takeIf { it.isNotBlank() }?.let { conditions.add("titre LIKE ?"); args.add("%$it%") }
        cr.editeur?.takeIf { it.isNotBlank() }?.let { conditions.add("editeur LIKE ?"); args.add("%$it%") }
        cr.annee?.let { conditions.add("annee = ?"); args.add(it) }
        cr.mois?.let { conditions.add("mois = ?"); args.add(it) }
        cr.superCategorie?.takeIf { it.isNotBlank() }?.let { conditions.add("superCategorie = ?"); args.add(it) }
        cr.categorie?.takeIf { it.isNotBlank() }?.let { conditions.add("categorie LIKE ?"); args.add("%$it%") }
        cr.description?.takeIf { it.isNotBlank() }?.let { conditions.add("description LIKE ?"); args.add("%$it%") }
        cr.tirage?.takeIf { it.isNotBlank() }?.let { conditions.add("tirage LIKE ?"); args.add("%$it%") }
        cr.dimensions?.takeIf { it.isNotBlank() }?.let { conditions.add("dimensions LIKE ?"); args.add("%$it%") }
        cr.isPossessed?.let { conditions.add("isPossessed = ?"); args.add(if (it) 1 else 0) }
        
        // Ajout de la recherche OCR dans les critères avancés
        qOcr?.takeIf { it.isNotBlank() }?.let { conditions.add("ocrText LIKE ?"); args.add("%$it%") }
        
        val hasTextCriteria = conditions.isNotEmpty()
        val wh = if (hasTextCriteria) " WHERE ${conditions.joinToString(" AND ")}" else ""
        
        val items = if (qE != null && !hasTextCriteria) {
            collectionDao.getAllItemsWithEmbeddings()
        } else {
            val dataQ = androidx.sqlite.db.SimpleSQLiteQuery("SELECT * FROM collection_items$wh ORDER BY annee DESC, mois DESC LIMIT 200", args.toTypedArray())
            collectionDao.advancedSearch(dataQ)
        }

        val total = if (hasTextCriteria) {
            val countQ = androidx.sqlite.db.SimpleSQLiteQuery("SELECT COUNT(*) FROM collection_items$wh", args.toTypedArray())
            collectionDao.countAdvancedSearch(countQ)
        } else items.size

        val res = if (qE != null) {
            val filtered = items.filter { it.imageEmbedding != null && it.imageEmbedding.isNotEmpty() }
                .map { 
                    val visualSim = cosineSimilarity(qE, it.imageEmbedding!!).toDouble()
                    // Si on a du texte OCR en entrée, on peut booster le score si le texte correspond
                    var finalSim = visualSim
                    if (qOcr != null && it.ocrText?.contains(qOcr, ignoreCase = true) == true) {
                        finalSim += 0.1 // Petit boost de confiance
                    }
                    SearchResultItem(it, finalSim) 
                }
                .filter { it.similarity != null && it.similarity >= 0.65 }
                .sortedByDescending { it.similarity }
            return AdvancedSearchResult(filtered.take(10), filtered.size)
        } else items.map { SearchResultItem(it) }
        
        return AdvancedSearchResult(res, total)
    }

    suspend fun findMostSimilarItems(qE: FloatArray): List<SearchResultItem> {
        return collectionDao.getAllItemsWithEmbeddings()
            .map { SearchResultItem(it, cosineSimilarity(qE, it.imageEmbedding!!).toDouble()) }
            .filter { !(it.similarity?.isNaN() ?: true) }
            .sortedByDescending { it.similarity }
            .take(3)
    }

    private fun cosineSimilarity(v1: FloatArray, v2B: ByteArray): Float {
        // Conversion robuste du ByteArray en FloatArray selon la taille détectée
        val v2 = when (val size = v2B.size) {
            v1.size -> {
                // Format Quantifié (INT8) : 1 octet par dimension
                FloatArray(size) { i -> v2B[i].toFloat() }
            }
            v1.size * 4 -> {
                // Format FLOAT32 : 4 octets par dimension
                val buffer = ByteBuffer.wrap(v2B).order(ByteOrder.LITTLE_ENDIAN)
                FloatArray(v1.size) { buffer.float }
            }
            else -> return 0.0f // Incohérence de taille
        }

        var dotProduct = 0.0f
        var normA = 0.0f
        var normB = 0.0f
        for (i in v1.indices) {
            dotProduct += v1[i] * v2[i]
            normA += v1[i] * v1[i]
            normB += v2[i] * v2[i]
        }
        val denominator = kotlin.math.sqrt(normA) * kotlin.math.sqrt(normB)
        return if (denominator == 0.0f) 0.0f else dotProduct / denominator
    }

    @ColorRes private fun getStatusColor(p: Int, t: Int, sM: Boolean): Int {
        if (t == 0) return R.color.status_error
        val pct = (p * 100) / t
        return if (sM) { if (pct > 75) R.color.status_ok else if (pct > 25) R.color.status_warning else R.color.status_error }
        else { if (pct < 25) R.color.status_error else if (pct < 75) R.color.status_warning else R.color.status_ok }
    }

    fun getSuperCategoryInfo(sM: Boolean): LiveData<List<CategoryInfo>> = liveData(Dispatchers.IO) {
        val all = CategoryMapper.getSuperCategories(); val dbC = collectionDao.getSuperCategoryInfoSuspend().associateBy { it.name }
        emit(all.map { n -> val c = dbC[n]; val p = c?.possessedCount ?: 0; val t = c?.totalCount ?: 0; CategoryInfo(n, p, t, getStatusColor(p, t, sM)) })
    }

    fun getCategoryInfoForSuperCategory(sC: String, sM: Boolean): LiveData<List<CategoryInfo>> = liveData(Dispatchers.IO) {
        val all = CategoryMapper.getCategoriesFor(sC); val dbC = collectionDao.getCategoryInfoForSuperCategorySuspend(sC).associateBy { it.name }
        emit(all.map { n -> val c = dbC[n]; val p = c?.possessedCount ?: 0; val t = c?.totalCount ?: 0; CategoryInfo(n, p, t, getStatusColor(p, t, sM)) })
    }

    fun getItemsBySuperCategoryAndCategory(s: String, c: String, p: Boolean): LiveData<List<CollectionItem>> = collectionDao.getItemsBySuperCategoryAndCategory(s, c, p)
    suspend fun insert(item: CollectionItem): Unit = collectionDao.insert(item.copy(updatedAt = System.currentTimeMillis()))
    suspend fun update(item: CollectionItem): Unit = collectionDao.update(item.copy(updatedAt = System.currentTimeMillis()))
    suspend fun delete(item: CollectionItem): Unit = collectionDao.delete(item)
}
