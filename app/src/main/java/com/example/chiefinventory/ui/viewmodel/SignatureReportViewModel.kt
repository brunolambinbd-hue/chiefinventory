package com.example.chiefinventory.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.example.chiefinventory.model.CollectionItem
import com.example.chiefinventory.model.SignatureStats
import com.example.chiefinventory.repo.CollectionRepository

/**
 * ViewModel for the signature report screen ([com.example.chiefinventory.ui.actvity.SignatureReportActivity]).
 *
 * This ViewModel provides statistics about image signatures and a filtered list of items
 * for the report. It highlights items with missing or empty signatures.
 *
 * @param repository The [CollectionRepository] for accessing collection data.
 */
class SignatureReportViewModel(repository: CollectionRepository) : ViewModel() {

    private val _allItems = repository.getAll()

    /**
     * A filtered and sorted list of items for the report. It includes all items with problematic
     * signatures (null or empty) plus a small sample of valid items for reference.
     */
    val filteredItems: MediatorLiveData<List<CollectionItem>> = MediatorLiveData<List<CollectionItem>>()

    /** Live statistics about the state of image signatures in the collection. */
    val signatureStats: LiveData<SignatureStats> = repository.getSignatureStats()

    init {
        filteredItems.addSource(_allItems) { items ->
            // Filter for items with problematic signatures (null or empty).
            val problems = items.filter { it.imageEmbedding == null || it.imageEmbedding.isEmpty() }
                                 .sortedBy { it.remoteId } // Sort for a stable order.

            // Take a small sample of valid items for context.
            val valids = items.filter { it.imageEmbedding != null && it.imageEmbedding.isNotEmpty() }
                              .take(5)

            // Combine the lists, showing problematic items first.
            filteredItems.value = problems + valids
        }
    }
}
