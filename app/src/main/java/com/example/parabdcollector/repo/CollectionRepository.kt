package com.example.parabdcollector.repo

import androidx.lifecycle.LiveData
import com.example.parabdcollector.dao.CollectionDao
import com.example.parabdcollector.model.CollectionItem

class CollectionRepository(private val collectionDao: CollectionDao) {

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

    fun search(query: String): LiveData<List<CollectionItem>> {
        return collectionDao.search(query)
    }

    fun advancedSearch(titre: String?, univers: String?, editeur: String?, annee: Int?, categorie: String?): LiveData<List<CollectionItem>> {
        return collectionDao.advancedSearch(titre, univers, editeur, annee, categorie)
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