package com.example.parabdcollector.repo

import androidx.annotation.ColorRes
import androidx.lifecycle.*
import com.example.parabdcollector.R
import com.example.parabdcollector.dao.CollectionDao
import com.example.parabdcollector.model.*
import com.example.parabdcollector.ui.model.CategoryInfo
import com.example.parabdcollector.ui.model.SearchResultItem
import com.example.parabdcollector.utils.CategoryMapper
import com.example.parabdcollector.utils.DimensionUtils
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
    fun getItemCountByLocation(): LiveData<List<com.example.parabdcollector.dao.ItemCountForLocation>> = collectionDao.getItemCountByLocation()

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

    open suspend fun advancedSearch(
        cr: SearchCriteria, 
        qE: FloatArray?, 
        detectedWords: List<String> = emptyList()
    ): AdvancedSearchResult {
        val conditions = mutableListOf<String>(); val args = mutableListOf<Any?>()
        
        // --- PARSING DES DIMENSIONS (Manuelles ou AR) ---
        val manualDim = DimensionUtils.parseDimensions(cr.dimensions)
        val finalWidth = cr.detectedWidth ?: manualDim?.first
        val finalHeight = cr.detectedHeight ?: manualDim?.second
        val hasPhysicalDimensions = finalWidth != null && finalHeight != null

        cr.titre?.takeIf { it.isNotBlank() }?.let { conditions.add("titre LIKE ?"); args.add("%$it%") }
        cr.editeur?.takeIf { it.isNotBlank() }?.let { conditions.add("editeur LIKE ?"); args.add("%$it%") }
        cr.annee?.let { conditions.add("annee = ?"); args.add(it) }
        cr.mois?.let { conditions.add("mois = ?"); args.add(it) }
        cr.superCategorie?.takeIf { it.isNotBlank() }?.let { conditions.add("superCategorie = ?"); args.add(it) }
        cr.categorie?.takeIf { it.isNotBlank() }?.let { conditions.add("categorie LIKE ?"); args.add("%$it%") }
        cr.description?.takeIf { it.isNotBlank() }?.let { conditions.add("description LIKE ?"); args.add("%$it%") }
        cr.tirage?.takeIf { it.isNotBlank() }?.let { conditions.add("tirage LIKE ?"); args.add("%$it%") }
        
        // On n'ajoute la condition textuelle sur les dimensions QUE si on n'a pas pu parser de chiffres
        if (!hasPhysicalDimensions) {
            cr.dimensions?.takeIf { it.isNotBlank() }?.let { conditions.add("dimensions LIKE ?"); args.add("%$it%") }
        }

        // On assouplit la recherche d'éditeur s'il vient de l'OCR
        val finalEditor = cr.editeur?.trim()
        if (!finalEditor.isNullOrBlank()) {
            // "COLLATE NOCASE" permet d'ignorer les majuscules/minuscules en SQLite
            conditions.add("editeur LIKE ? COLLATE NOCASE")
            args.add("%$finalEditor%")
        }

        cr.isPossessed?.let { conditions.add("isPossessed = ?"); args.add(if (it) 1 else 0) }
        
        val hasTextCriteria = conditions.isNotEmpty()
        val wh = if (hasTextCriteria) " WHERE ${conditions.joinToString(" AND ")}" else ""
        
        val items = if ((qE != null || detectedWords.isNotEmpty() || hasPhysicalDimensions) && !hasTextCriteria) {
            // Mode INTELLIGENT (Photo, OCR ou Mesure AR/Manuelle)
            collectionDao.getAllItemsWithEmbeddings()
        } else {
            // Mode TEXTUEL classique
            val dataQ = androidx.sqlite.db.SimpleSQLiteQuery("SELECT * FROM collection_items$wh ORDER BY annee DESC, mois DESC LIMIT 200", args.toTypedArray())
            collectionDao.advancedSearch(dataQ)
        }

        val total = if (hasTextCriteria) {
            val countQ = androidx.sqlite.db.SimpleSQLiteQuery("SELECT COUNT(*) FROM collection_items$wh", args.toTypedArray())
            collectionDao.countAdvancedSearch(countQ)
        } else items.size

        if (qE != null || detectedWords.isNotEmpty() || hasPhysicalDimensions) {
            val allSimilar = items.filter { it.imageEmbedding != null && it.imageEmbedding.isNotEmpty() }
                .map { item ->
                    var visualScore = if (qE != null) {
                        cosineSimilarity(qE, item.imageEmbedding!!).toDouble()
                    } else {
                        0.5 // Score neutre si on n'a que de l'OCR ou de la mesure
                    }
                    
                    // --- LOGIQUE HYBRIDE : BOOST PAR OCR (Ajustée) ---
                    if (detectedWords.isNotEmpty()) {
                        var boost = 0.0
                        // On vérifie si AU MOINS UN mot correspond à l'éditeur ou au titre (ignoreCase = true)
                        val matchEditor = detectedWords.any { word -> 
                            item.editeur?.contains(word, ignoreCase = true) == true 
                        }
                        val matchTitle = detectedWords.any { word -> 
                            item.titre.contains(word, ignoreCase = true) == true 
                        }

                        if (matchEditor) boost += 0.10 // +10% pour l'éditeur
                        if (matchTitle) boost += 0.05  // +5% pour le titre

                        if (boost > 0) {
                            visualScore += boost
                            // On plafonne à 0.99 pour laisser la place au 100% visuel pur
                            if (visualScore > 0.99) visualScore = 0.99
                        }
                    }

                    // --- LOGIQUE DIMENSIONS : FILTRAGE INTELLIGENT (AR ou Manuel) ---
                    if (hasPhysicalDimensions) {
                        val itemDim = DimensionUtils.parseDimensions(item.dimensions)
                        if (itemDim != null) {
                            val isMatch = DimensionUtils.isWithinTolerance(
                                Pair(finalWidth!!, finalHeight!!),
                                itemDim
                            )
                            if (!isMatch) {
                                // On applique une pénalité sévère si les dimensions ne collent pas
                                visualScore -= 0.50
                                if (visualScore < 0.0) visualScore = 0.0
                            } else {
                                // Petit boost si les dimensions sont parfaites
                                visualScore += 0.05
                                if (visualScore > 1.0) visualScore = 1.0
                            }
                        }
                    } else if (cr.queryAspectRatio != null) {
                        // --- FALLBACK : FILTRAGE PAR RATIO D'IMAGE ---
                        val itemDim = DimensionUtils.parseDimensions(item.dimensions)
                        if (itemDim != null) {
                            val itemRatio = itemDim.first / itemDim.second
                            val isRatioMatch = DimensionUtils.isRatioMatch(cr.queryAspectRatio, itemRatio)
                            if (!isRatioMatch) {
                                // Pénalité légère si le ratio (format) ne correspond pas du tout
                                visualScore -= 0.15
                                if (visualScore < 0.0) visualScore = 0.0
                            }
                        }
                    }

                    SearchResultItem(item, visualScore)
                }
                .sortedByDescending { it.similarity }
            
            val highConfidence = allSimilar.filter { it.similarity != null && it.similarity >= 0.65 }
            
            return if (highConfidence.isNotEmpty()) {
                AdvancedSearchResult(highConfidence.take(15), highConfidence.size, isFallback = false)
            } else {
                AdvancedSearchResult(allSimilar.take(15), allSimilar.size, isFallback = true)
            }
        }
        
        val res = items.map { SearchResultItem(it) }
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
    fun getSignatureReportItems(): LiveData<List<com.example.parabdcollector.dao.SignatureReportItem>> =
        collectionDao.getSignatureReportItems()

    fun getItemsBySession(sessionId: Long): LiveData<List<CollectionItem>> = collectionDao.getItemsBySession(sessionId)
    fun getFullHierarchy(): LiveData<List<com.example.parabdcollector.dao.FullHierarchyItem>> = collectionDao.getFullHierarchy()
    suspend fun getAllPublishers(): List<String> = collectionDao.getAllPublishers()
    suspend fun insert(item: CollectionItem): Unit = collectionDao.insert(item.copy(updatedAt = System.currentTimeMillis()))
    suspend fun insertAll(items: List<CollectionItem>): Unit = collectionDao.insertAll(items.map { it.copy(updatedAt = System.currentTimeMillis()) })
    suspend fun update(item: CollectionItem): Unit = collectionDao.update(item.copy(updatedAt = System.currentTimeMillis()))
    suspend fun updateAll(items: List<CollectionItem>): Unit = collectionDao.updateAll(items.map { it.copy(updatedAt = System.currentTimeMillis()) })
    suspend fun delete(item: CollectionItem): Unit = collectionDao.delete(item)
}
