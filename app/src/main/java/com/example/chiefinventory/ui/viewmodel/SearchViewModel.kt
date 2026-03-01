package com.example.chiefinventory.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.imagecomparison.ImageEmbedderHelper
import com.example.chiefinventory.R
import com.example.chiefinventory.model.SearchCriteria
import com.example.chiefinventory.ui.model.SearchResultItem
import com.example.chiefinventory.repo.CollectionRepository
import com.example.chiefinventory.utils.SignatureUtils
import com.example.chiefinventory.utils.TextRecognitionHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

/**
 * Represents the state of a search operation.
 */
sealed class SearchResultState {
    /** The screen is waiting for a search to be initiated. */
    object Idle : SearchResultState()
    /** The search is in progress. */
    object Loading : SearchResultState()
    /** The search completed successfully. */
    data class Success(val results: List<SearchResultItem>, val totalCount: Int = results.size) : SearchResultState()
    /** The search failed. */
    data class Error(val message: String) : SearchResultState()
}

/**
 * ViewModel for the search screen ([com.example.chiefinventory.ui.actvity.SearchActivity]).
 *
 * This ViewModel handles the logic for simple text search, advanced criteria search,
 * and image-based similarity search.
 *
 * @param application The application instance, required for the AndroidViewModel and ImageEmbedderHelper.
 * @param repository The [CollectionRepository] from which to get the data.
 */
class SearchViewModel(application: Application, private val repository: CollectionRepository) : AndroidViewModel(application) {

    private val _searchResultState = MutableLiveData<SearchResultState>(SearchResultState.Idle)
    /** The state of the most recent search, exposed as LiveData. */
    val searchResultState: LiveData<SearchResultState> = _searchResultState

    private val _signaturePreview = MutableLiveData<String>()
    /** A formatted string preview of the last computed image signature. */
    val signaturePreview: LiveData<String> = _signaturePreview

    private var searchJob: Job? = null

    private val imageEmbedderHelper: ImageEmbedderHelper = ImageEmbedderHelper(
        context = application,
        listener = object : ImageEmbedderHelper.EmbedderListener {
            override fun onError(error: String, errorCode: Int) {
                Log.e("SearchViewModel", "ImageEmbedderHelper Error ($errorCode): $error")
                _searchResultState.postValue(SearchResultState.Error(getApplication<Application>().getString(R.string.search_error_image_analysis)))
            }
        }
    )

    private val textRecognitionHelper = TextRecognitionHelper(application)

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
        searchJob?.cancel()
        _searchResultState.value = SearchResultState.Loading
        searchJob = viewModelScope.launch {
            try {
                val results = repository.search(query)
                _searchResultState.value = SearchResultState.Success(results.map { SearchResultItem(it) })
            } catch (e: CancellationException) {
                Log.i("SearchViewModel", "Simple search cancelled.")
                // Propagate cancellation to ensure the coroutine stops cleanly
                throw e
            } catch (e: Exception) {
                Log.e("SearchViewModel", "Simple search failed", e)
                _searchResultState.value = SearchResultState.Error(getApplication<Application>().getString(R.string.search_error_simple))
            }
        }
    }

    /**
     * Performs an advanced search using text criteria and an optional image.
     * @param criteria The set of text-based search criteria.
     * @param bitmap The optional image to use for similarity search.
     */
    fun advancedSearch(criteria: SearchCriteria, bitmap: Bitmap?) {
        searchJob?.cancel()
        _searchResultState.value = SearchResultState.Loading
        searchJob = viewModelScope.launch {
            try {
                val queryEmbedding = bitmap?.let { imageEmbedderHelper.computeSignature(it)?.floatEmbedding() }
                val queryOcr = bitmap?.let { textRecognitionHelper.recognizeText(it) }
                
                val searchResult = repository.advancedSearch(criteria, queryEmbedding, queryOcr)
                _searchResultState.value = SearchResultState.Success(searchResult.results, searchResult.totalCount)
            } catch (e: CancellationException) {
                Log.i("SearchViewModel", "Advanced search cancelled.")
                throw e
            } catch (e: Exception) {
                Log.e("SearchViewModel", "Advanced search failed", e)
                _searchResultState.value = SearchResultState.Error(getApplication<Application>().getString(R.string.search_error_advanced))
            }
        }
    }

    /**
     * Clears the current search results and signature preview from the UI.
     */
    fun clearSearchResults() {
        searchJob?.cancel()
        _searchResultState.value = SearchResultState.Idle
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
