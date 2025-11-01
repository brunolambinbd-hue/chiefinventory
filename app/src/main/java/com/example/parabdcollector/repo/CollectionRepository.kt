package com.example.parabdcollector.repo

import androidx.lifecycle.LiveData
import com.example.parabdcollector.db.CollectionDao
import com.example.parabdcollector.model.CollectionItem

class CollectionRepository(private val collectionDao: CollectionDao) {

    fun getAll(): LiveData<List<CollectionItem>> {
        return collectionDao.getAll()
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