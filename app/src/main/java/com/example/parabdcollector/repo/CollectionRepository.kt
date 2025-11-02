package com.example.parabdcollector.repo

import androidx.lifecycle.LiveData
import com.example.parabdcollector.dao.CollectionDao
import com.example.parabdcollector.model.CollectionItem

class CollectionRepository(private val collectionDao: CollectionDao) {

    fun getAll(): LiveData<List<CollectionItem>> {
        return collectionDao.getAll()
    }

    fun getById(id: Long): LiveData<CollectionItem> {
        return collectionDao.getById(id)
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