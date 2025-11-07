package com.example.parabdcollector.ui

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.parabdcollector.model.CollectionItem
import com.example.parabdcollector.repo.CollectionRepository

class MainViewModel(application: Application, private val repository: CollectionRepository) : ViewModel() {

    val possessedItems: LiveData<List<CollectionItem>> = repository.getAllPossessed()
    val soughtItems: LiveData<List<CollectionItem>> = repository.getAllSought()
    val totalItemsCount: LiveData<Int> = repository.getTotalCount()

    fun getById(id: Long): LiveData<CollectionItem> {
        return repository.getById(id)
    }

    fun getDistinctSuperCategories(isPossessed: Boolean): LiveData<List<String>> {
        return repository.getDistinctSuperCategories(isPossessed)
    }

    fun getDistinctCategoriesForSuperCategory(superCategory: String, isPossessed: Boolean): LiveData<List<String>> {
        return repository.getDistinctCategoriesForSuperCategory(superCategory, isPossessed)
    }

    fun getItemsBySuperCategoryAndCategory(superCategory: String, category: String, isPossessed: Boolean): LiveData<List<CollectionItem>> {
        return repository.getItemsBySuperCategoryAndCategory(superCategory, category, isPossessed)
    }

    fun insert(item: CollectionItem) {
        // Pour l'insertion, nous devons utiliser une coroutine.
        // Idéalement, cela devrait être géré dans le viewModelScope, mais pour la simplicité de l'exemple...
    }

    fun update(item: CollectionItem) {
        // Idem pour la mise à jour.
    }
}