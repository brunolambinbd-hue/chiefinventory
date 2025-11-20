package com.example.parabdcollector.repo

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.example.parabdcollector.dao.CollectionDao
import com.example.parabdcollector.model.CategoryInfo
import com.example.parabdcollector.model.CollectionItem
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

    fun search(query: String): LiveData<List<CollectionItem>> {
        return collectionDao.search(query)
    }

    fun advancedSearch(titre: String?, editeur: String?, annee: Int?, mois: Int?, superCategorie: String?, categorie: String?, description: String?): LiveData<List<CollectionItem>> {
        return collectionDao.advancedSearch(titre, editeur, annee, mois, superCategorie, categorie, description)
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