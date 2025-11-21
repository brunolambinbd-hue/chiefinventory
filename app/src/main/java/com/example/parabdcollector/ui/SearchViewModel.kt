package com.example.parabdcollector.ui

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.imagecomparison.ImageEmbedderHelper
import com.example.parabdcollector.model.SearchResultItem
import com.example.parabdcollector.repo.CollectionRepository
import kotlinx.coroutines.launch

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

    private val imageEmbedderHelper: ImageEmbedderHelper = ImageEmbedderHelper(
        context = application,
        listener = object : ImageEmbedderHelper.EmbedderListener {
            override fun onError(error: String, errorCode: Int) {
                Log.e("SearchViewModel", "ImageEmbedderHelper Error ($errorCode): $error")
            }
        }
    )

    fun search(query: String) {
        viewModelScope.launch {
            val results = repository.search("%${query}%")
            _searchResults.value = results.map { SearchResultItem(it) } 
        }
    }

    fun advancedSearch(criteria: SearchCriteria) {
        viewModelScope.launch {
            val results = repository.advancedSearch(
                titre = criteria.titre?.let { "%$it%" },
                editeur = criteria.editeur?.let { "%$it%" },
                annee = criteria.annee,
                mois = criteria.mois,
                superCategorie = criteria.superCategorie,
                categorie = criteria.categorie?.let { "%$it%" },
                description = criteria.description?.let { "%$it%" },
                tirage = criteria.tirage?.let { "%$it%" },
                dimensions = criteria.dimensions?.let { "%$it%" }
            )
            _searchResults.value = results.map { SearchResultItem(it) }
        }
    }

    fun searchByImage(bitmap: Bitmap) {
        viewModelScope.launch {
            val signature = imageEmbedderHelper.computeSignature(bitmap)
            if (signature != null) {
                val results = repository.findSimilarItems(signature.floatEmbedding())
                _searchResults.value = results
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        imageEmbedderHelper.clearImageEmbedder()
    }
}