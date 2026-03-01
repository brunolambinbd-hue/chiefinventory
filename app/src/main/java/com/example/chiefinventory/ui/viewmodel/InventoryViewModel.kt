package com.example.chiefinventory.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.chiefinventory.repo.CollectionRepository
import com.example.chiefinventory.ui.model.SearchResultItem
import com.example.imagecomparison.ImageEmbedderHelper
import kotlinx.coroutines.launch

/**
 * ViewModel for the Inventory Scanner feature.
 *
 * This ViewModel handles the logic for capturing an image, finding similar items,
 * and updating an item's status and location.
 */
class InventoryViewModel(
    application: Application,
    private val repository: CollectionRepository
) : AndroidViewModel(application) {

    private val _similarItems = MutableLiveData<Pair<List<SearchResultItem>, Uri>>()
    val similarItems: LiveData<Pair<List<SearchResultItem>, Uri>> = _similarItems

    private val imageEmbedderHelper: ImageEmbedderHelper = ImageEmbedderHelper(context = application, listener = null)

    /**
     * Takes a bitmap, computes its signature, and finds the top 3 most similar items.
     * @param bitmap The image of the item to find.
     * @param imageUri The URI of the scanned image, to pass it forward.
     */
    fun findSimilarItems(bitmap: Bitmap, imageUri: Uri) {
        viewModelScope.launch {
            val queryEmbedding = imageEmbedderHelper.computeSignature(bitmap)?.floatEmbedding()
            if (queryEmbedding != null) {
                val results = repository.findMostSimilarItems(queryEmbedding)
                _similarItems.postValue(Pair(results, imageUri))
            } else {
                _similarItems.postValue(Pair(emptyList(), imageUri)) // Post empty if signature calculation fails
            }
        }
    }

    /**
     * Updates an item's location and marks it as possessed.
     * @param itemId The ID of the item to update.
     * @param locationId The new location ID for the item.
     */
    fun updateItemLocationAndStatus(itemId: Long, locationId: Long) {
        viewModelScope.launch {
            val item = repository.getItemById(itemId)
            if (item != null) {
                val updatedItem = item.copy(locationId = locationId, isPossessed = true)
                repository.update(updatedItem)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        imageEmbedderHelper.clearImageEmbedder()
    }
}
