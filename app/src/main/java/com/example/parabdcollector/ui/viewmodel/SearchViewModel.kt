package com.example.parabdcollector.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.imagecomparison.ImageEmbedderHelper
import com.example.parabdcollector.model.SearchCriteria
import com.example.parabdcollector.ui.model.SearchResultItem
import com.example.parabdcollector.repo.CollectionRepository
import com.example.parabdcollector.utils.SignatureUtils
import kotlinx.coroutines.launch

/**
 * ViewModel for the search screen ([com.example.parabdcollector.ui.actvity.SearchActivity]).
 *
 * This ViewModel handles the logic for simple text search, advanced criteria search,
 * and image-based similarity search.
 *
 * @param application The application instance, required for the AndroidViewModel and ImageEmbedderHelper.
 * @param repository The [CollectionRepository] from which to get the data.
 */
class SearchViewModel(application: Application, private val repository: CollectionRepository) : AndroidViewModel(application) {

    private val _searchResults = MutableLiveData<List<SearchResultItem>>()
    /** The results of the most recent search, exposed as LiveData. */
    val searchResults: LiveData<List<SearchResultItem>> = _searchResults

    private val _signaturePreview = MutableLiveData<String>()
    /** A formatted string preview of the last computed image signature. */
    val signaturePreview: LiveData<String> = _signaturePreview

    private val imageEmbedderHelper: ImageEmbedderHelper = ImageEmbedderHelper(
        context = application,
        listener = object : ImageEmbedderHelper.EmbedderListener {
            override fun onError(error: String, errorCode: Int) {
                Log.e("SearchViewModel", "ImageEmbedderHelper Error ($errorCode): $error")
            }
        }
    )

    /**
     * Calculates the signature of a given bitmap and updates the [signaturePreview] LiveData.
     * @param bitmap The image for which to compute the signature.
     */
    fun calculateSignatureForPreview(bitmap: Bitmap) {
        viewModelScope.launch {
            val signature = imageEmbedderHelper.computeSignature(bitmap)
            _signaturePreview.value = SignatureUtils.formatSignaturePreview(getApplication(), signature?.floatEmbedding())
        }
    }

    /**
     * Performs a simple text search by delegating to the repository.
     * @param query The search term.
     */
    fun search(query: String) {
        viewModelScope.launch {
            val results = repository.search(query)
            _searchResults.value = results.map { SearchResultItem(it) }
        }
    }

    /**
     * Performs an advanced search using text criteria and an optional image.
     * @param criteria The set of text-based search criteria.
     * @param bitmap The optional image to use for similarity search.
     */
    fun advancedSearch(criteria: SearchCriteria, bitmap: Bitmap?) {
        viewModelScope.launch {
            val queryEmbedding = bitmap?.let { imageEmbedderHelper.computeSignature(it)?.floatEmbedding() }
            val results = repository.advancedSearch(criteria, queryEmbedding)
            _searchResults.value = results
        }
    }

    /**
     * Clears the current search results and signature preview from the UI.
     */
    fun clearSearchResults() {
        _searchResults.value = emptyList()
        _signaturePreview.value = ""
    }

    /**
     * Cleans up the ImageEmbedderHelper when the ViewModel is destroyed.
     */
    override fun onCleared() {
        super.onCleared()
        imageEmbedderHelper.clearImageEmbedder()
    }
}
