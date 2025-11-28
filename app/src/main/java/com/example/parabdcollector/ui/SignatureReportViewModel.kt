package com.example.parabdcollector.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.example.parabdcollector.model.CollectionItem
import com.example.parabdcollector.model.SignatureStats
import com.example.parabdcollector.repo.CollectionRepository

class SignatureReportViewModel(repository: CollectionRepository) : ViewModel() {

    private val _allItems = repository.getAll()
    val filteredItems = MediatorLiveData<List<CollectionItem>>()

    val signatureStats: LiveData<SignatureStats> = repository.getSignatureStats()

    init {
        filteredItems.addSource(_allItems) { items ->
            val problems = items.filter { it.imageEmbedding == null || it.imageEmbedding.isEmpty() }
                                 .sortedBy { it.remoteId } // On trie pour avoir un ordre stable

            val valids = items.filter { it.imageEmbedding != null && it.imageEmbedding.isNotEmpty() }
                              .take(5) // On ne prend que les 5 premiers valides

            filteredItems.value = problems + valids
        }
    }
}
