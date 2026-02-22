package com.example.parabdcollector.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.example.parabdcollector.dao.SignatureReportItem
import com.example.parabdcollector.model.SignatureStats
import com.example.parabdcollector.repo.CollectionRepository

/**
 * ViewModel for the signature report screen.
 */
class SignatureReportViewModel(repository: CollectionRepository) : ViewModel() {

    // On utilise maintenant la méthode légère qui ne charge pas les blobs
    private val _reportItems = repository.getSignatureReportItems()

    /**
     * A filtered list of items for the report using lightweight SignatureReportItem.
     */
    val filteredItems: MediatorLiveData<List<SignatureReportItem>> = MediatorLiveData()

    /** Live statistics about the state of image signatures. */
    val signatureStats: LiveData<SignatureStats> = repository.getSignatureStats()

    init {
        filteredItems.addSource(_reportItems) { items ->
            // On sépare les items avec signature et sans signature
            val problems = items.filter { !it.hasEmbedding }
                                 .sortedBy { it.id }

            val valids = items.filter { it.hasEmbedding }
                              .take(20) // Petit échantillon pour vérification

            filteredItems.value = problems + valids
        }
    }
}
