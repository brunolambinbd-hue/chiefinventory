package com.example.parabdcollector.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.parabdcollector.model.CollectionItem
import com.example.parabdcollector.repo.CollectionRepository

class SearchViewModel(application: Application, private val repository: CollectionRepository) : AndroidViewModel(application) {

    private val _searchResults = MutableLiveData<List<CollectionItem>>()
    val searchResults: LiveData<List<CollectionItem>> = _searchResults

    fun search(query: String): LiveData<List<CollectionItem>> {
        return repository.search("%${query}%")
    }
}