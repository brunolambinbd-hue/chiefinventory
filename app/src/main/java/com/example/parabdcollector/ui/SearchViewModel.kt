package com.example.parabdcollector.ui

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.imagecomparison.ImageEmbedderHelper
import com.example.parabdcollector.model.SearchCriteria
import com.example.parabdcollector.model.SearchResultItem
import com.example.parabdcollector.repo.CollectionRepository
import com.example.parabdcollector.utils.SignatureUtils
import kotlinx.coroutines.launch

class SearchViewModel(application: Application, private val repository: CollectionRepository) : AndroidViewModel(application) {

    private val _searchResults = MutableLiveData<List<SearchResultItem>>()
    val searchResults: LiveData<List<SearchResultItem>> = _searchResults

    private val _signaturePreview = MutableLiveData<String>()
    val signaturePreview: LiveData<String> = _signaturePreview

    private val imageEmbedderHelper: ImageEmbedderHelper = ImageEmbedderHelper(
        context = application,
        listener = object : ImageEmbedderHelper.EmbedderListener {
            override fun onError(error: String, errorCode: Int) {
                Log.e("SearchViewModel", "ImageEmbedderHelper Error ($errorCode): $error")
            }
        }
    )

    fun calculateSignatureForPreview(bitmap: Bitmap) {
        viewModelScope.launch {
            val signature = imageEmbedderHelper.computeSignature(bitmap)
            // Utiliser .value car nous sommes sur le thread principal grâce à viewModelScope
            _signaturePreview.value = SignatureUtils.formatSignaturePreview(getApplication(), signature?.floatEmbedding())
        }
    }

    fun search(query: String) {
        viewModelScope.launch {
            val results = repository.search(query)
            _searchResults.value = results.map { SearchResultItem(it) }
        }
    }

    fun advancedSearch(criteria: SearchCriteria, bitmap: Bitmap?) {
        viewModelScope.launch {
            val queryEmbedding = bitmap?.let { imageEmbedderHelper.computeSignature(it)?.floatEmbedding() }
            val results = repository.advancedSearch(criteria, queryEmbedding)
            // Utiliser .value pour une mise à jour synchrone dans le scope du test
            _searchResults.value = results
        }
    }

    fun clearSearchResults() {
        _searchResults.value = emptyList()
        _signaturePreview.value = ""
    }

    override fun onCleared() {
        super.onCleared()
        imageEmbedderHelper.clearImageEmbedder()
    }
}
