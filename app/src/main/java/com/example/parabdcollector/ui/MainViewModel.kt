package com.example.parabdcollector.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.parabdcollector.db.AppDatabase
import com.example.parabdcollector.db.CollectionDao
import com.example.parabdcollector.model.CollectionItem
import com.example.parabdcollector.repo.CollectionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel(private val repository: CollectionRepository) : ViewModel() {

    // 1. Les données (allItems) sont directement exposées depuis le repository.
    //    Pas besoin de bloc 'init' pour cela.
    val allItems: LiveData<List<CollectionItem>> = repository.getAll()

    // 2. Les fonctions 'insert', 'update', 'delete' utilisent le repository
    //    reçu dans le constructeur.
    //    On supprime 'Dispatchers.IO' car Room s'en charge déjà pour les fonctions 'suspend'.
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