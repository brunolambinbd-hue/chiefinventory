package com.example.parabdcollector.ui

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.imagecomparison.ImageEmbedderHelper
import com.example.parabdcollector.R
import com.example.parabdcollector.model.SearchResultItem
import com.example.parabdcollector.repo.CollectionRepository
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import java.nio.ByteOrder

data class SearchCriteria(
    val titre: String? = null,
    val editeur: String? = null,
    val annee: Int? = null,
    val mois: Int? = null,
    val superCategorie: String? = null,
    val categorie: String? = null,
    val description: String? = null,
    val tirage: String? = null,
    val dimensions: String? = null
)

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

    fun search(criteria: SearchCriteria, bitmap: Bitmap?) {
        viewModelScope.launch {
            // On calcule la signature de l'image de recherche, si elle existe
            val querySignature = bitmap?.let {
                val signature = imageEmbedderHelper.computeSignature(it)
                signature?.floatEmbedding()
            }

            // On met à jour l'aperçu de la signature
            querySignature?.let {
                val preview = it.take(5).joinToString(", ") { "%.2f".format(it) }
                _signaturePreview.value = getApplication<Application>().getString(R.string.signature_preview_format, preview)
            } ?: run { _signaturePreview.value = "" }

            // On lance la recherche unifiée dans le repository
            val results = repository.unifiedSearch(criteria, querySignature)
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