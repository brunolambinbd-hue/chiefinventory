package com.example.parabdcollector.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.parabdcollector.model.CollectionItem
import com.example.parabdcollector.repo.CollectionRepository
import kotlinx.coroutines.launch

// Le ViewModel doit hériter de AndroidViewModel pour être testable avec un contexte.
class MainViewModel(application: Application, private val repository: CollectionRepository) : AndroidViewModel(application) {

    val allItems: LiveData<List<CollectionItem>> = repository.getAll()

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