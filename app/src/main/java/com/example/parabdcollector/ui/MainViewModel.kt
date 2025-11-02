package com.example.parabdcollector.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.parabdcollector.model.CollectionItem
import com.example.parabdcollector.repo.CollectionRepository
import kotlinx.coroutines.launch

class MainViewModel(application: Application, private val repository: CollectionRepository) : AndroidViewModel(application) {

    val possessedItems: LiveData<List<CollectionItem>> = repository.getAllPossessed()
    val soughtItems: LiveData<List<CollectionItem>> = repository.getAllSought()
    val totalItemsCount: LiveData<Int> = repository.getTotalCount()

    fun getById(id: Long): LiveData<CollectionItem> {
        return repository.getById(id)
    }

    fun insert(item: CollectionItem) = viewModelScope.launch {
        repository.insert(item)
    }

    fun update(item: CollectionItem) = viewModelScope.launch {
        repository.update(item)
    }

    fun delete(item: CollectionItem) = viewModelScope.launch {
        repository.delete(item)
    }
}