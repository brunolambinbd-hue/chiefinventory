package com.example.parabdcollector.ui

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.parabdcollector.model.CategoryInfo
import com.example.parabdcollector.model.CollectionItem
import com.example.parabdcollector.repo.CollectionRepository
import kotlinx.coroutines.launch

class MainViewModel(application: Application, private val repository: CollectionRepository) : ViewModel() {

    val possessedItems: LiveData<List<CollectionItem>> = repository.getAllPossessed()
    val soughtItems: LiveData<List<CollectionItem>> = repository.getAllSought()
    val totalItemsCount: LiveData<Int> = repository.getTotalCount()

    fun getById(id: Long): LiveData<CollectionItem> {
        return repository.getById(id)
    }

    fun getSuperCategoryInfo(isPossessed: Boolean): LiveData<List<CategoryInfo>> {
        return repository.getSuperCategoryInfo(isPossessed)
    }

    fun getCategoryInfoForSuperCategory(superCategory: String, isPossessed: Boolean): LiveData<List<CategoryInfo>> {
        return repository.getCategoryInfoForSuperCategory(superCategory, isPossessed)
    }

    fun getItemsBySuperCategoryAndCategory(superCategory: String, category: String, isPossessed: Boolean): LiveData<List<CollectionItem>> {
        return repository.getItemsBySuperCategoryAndCategory(superCategory, category, isPossessed)
    }

    fun insert(item: CollectionItem) = viewModelScope.launch {
        repository.insert(item)
    }

    fun update(item: CollectionItem) = viewModelScope.launch {
        repository.update(item)
    }
}