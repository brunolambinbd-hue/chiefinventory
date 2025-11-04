package com.example.parabdcollector.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.parabdcollector.model.CollectionItem
import com.example.parabdcollector.repo.CollectionRepository

class ItemListViewModel(repository: CollectionRepository, listType: String?) : ViewModel() {

    val items: LiveData<List<CollectionItem>> = when (listType) {
        ItemListActivity.TYPE_POSSESSED -> repository.getAllPossessed()
        ItemListActivity.TYPE_SOUGHT -> repository.getAllSought()
        else -> throw IllegalArgumentException("Unknown list type")
    }
}