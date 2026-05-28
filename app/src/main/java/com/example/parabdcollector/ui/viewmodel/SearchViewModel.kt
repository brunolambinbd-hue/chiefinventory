package com.example.parabdcollector.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.imagecomparison.ImageEmbedderHelper
import com.example.parabdcollector.R
import com.example.parabdcollector.model.SearchCriteria
import com.example.parabdcollector.ui.model.SearchResultItem
import com.example.parabdcollector.repo.CollectionRepository
import com.example.parabdcollector.utils.ImageProcessingUtils
import com.example.parabdcollector.utils.SignatureUtils
import com.example.parabdcollector.utils.TextRecognitionHelper
import kotlinx.coroutines.async
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
    data class Success(
        val results: List<SearchResultItem>, 
        val totalCount: Int = results.size,
        val isFallback: Boolean = false
    ) : SearchResultState()
    /** The search failed. */
    data class Error(val message: String) : SearchResultState()
}

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

    private val _searchResultState = MutableLiveData<SearchResultState>(SearchResultState.Idle)
    /** The state of the most recent search, exposed as LiveData. */
    val searchResultState: LiveData<SearchResultState> = _searchResultState

    private val _signaturePreview = MutableLiveData<String>()
    /** A formatted string preview of the last computed image signature. */
    val signaturePreview: LiveData<String> = _signaturePreview

    private val _detectedWords = MutableLiveData<List<String>>()
    /** Individual words detected in the image for background boosting. */
    val detectedWords: LiveData<List<String>> = _detectedWords

    private val _matchedPublisher = MutableLiveData<String?>()
    /** The official publisher name if a match was found in the database. */
    val matchedPublisher: LiveData<String?> = _matchedPublisher

    private var knownPublishers: List<String> = emptyList()

    init {
        viewModelScope.launch {
            try {
                knownPublishers = repository.getAllPublishers()
            } catch (e: Exception) {
                Log.e("SearchViewModel", "Failed to load publishers", e)
            }
        }
    }

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

    /**
     * Calculates the signature of a given bitmap and updates the [signaturePreview] LiveData.
     * @param bitmap The image for which to compute the signature.
     */
    fun calculateSignatureForPreview(bitmap: Bitmap) {
        viewModelScope.launch {
            val signature = imageEmbedderHelper.computeSignature(bitmap)
            _signaturePreview.value = SignatureUtils.formatSignaturePreview(getApplication(), signature?.floatEmbedding())

            // 1. Premier essai OCR avec l'image brute
            var words = TextRecognitionHelper.extractText(bitmap)
            Log.d("SearchViewModel", "OCR Essai 1 (Brut) : $words")
            
            // 2. Si le résultat est pauvre, on tente une "seconde chance" avec l'image boostée
            if (words.size < 2 || words.all { it.length < 5 }) {
                val enhanced = ImageProcessingUtils.enhanceContrast(bitmap)
                val newWords = TextRecognitionHelper.extractText(enhanced)
                Log.d("SearchViewModel", "OCR Essai 2 (Boosté) : $newWords")
                
                if (newWords.size > words.size) {
                    words = newWords
                }
            }

            if (words.isNotEmpty()) {
                // 3. Fusion et corrections OCR classiques
                var fullDetectedText = words.joinToString(" ").lowercase()

                // On corrige les erreurs courantes d'OCR pour faciliter le match (ex: 'dc' au lieu de 'de')
                fullDetectedText = fullDetectedText
                    .replace(" dc ", " de ")
                    .replace(" dc-", " de-")
                    .replace("-dc ", "-de ")
                    .replace(" mcr", " mer")
                    .replace(" flestival", " festival")

                Log.d("SearchViewModel", "Texte après corrections OCR : '$fullDetectedText'")

                val officialPublisher = if (knownPublishers.isNotEmpty()) {
                    knownPublishers.find { publisher ->
                        val cleanPublisher = publisher.lowercase().trim()

                        // Stratégie A : Inclusion directe (la plus sûre)
                        val directMatch = fullDetectedText.contains(cleanPublisher) || cleanPublisher.contains(fullDetectedText)

                        // Stratégie B : Match par mots significatifs (si A échoue)
                        // On vérifie si tous les mots longs de l'éditeur officiel sont présents
                        val wordsMatch = if (!directMatch) {
                            val pubWords = cleanPublisher.split(" ", "-", "/").filter { it.length > 3 }
                            pubWords.isNotEmpty() && pubWords.all { fullDetectedText.contains(it) }
                        } else false

                        val match = directMatch || wordsMatch
                        if (match) Log.d("SearchViewModel", "Match trouvé ! Base: '$cleanPublisher' vs Image: '$fullDetectedText'")
                        match
                    }
                } else null
                
                _matchedPublisher.value = officialPublisher
                _detectedWords.value = words
            }
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
                // 1. Premier essai avec l'image brute
                val embeddingDeferred = async { bitmap?.let { imageEmbedderHelper.computeSignature(it)?.floatEmbedding() } }
                val wordsDeferred = async { bitmap?.let { TextRecognitionHelper.extractText(it) } ?: emptyList() }
                
                val queryEmbedding = embeddingDeferred.await()
                val detectedWords = wordsDeferred.await()
                
                var searchResult = repository.advancedSearch(criteria, queryEmbedding, detectedWords)

                // 2. Si le résultat est incertain (Fallback) et qu'on a une image, on tente le DEUXIÈME ESSAI
                if (searchResult.isFallback && bitmap != null) {
                    Log.i("SearchViewModel", "Résultat incertain. Tentative de traitement d'image (2ème essai)...")
                    
                    val enhancedBitmap = ImageProcessingUtils.enhanceContrast(bitmap)
                    val enhancedEmbedding = imageEmbedderHelper.computeSignature(enhancedBitmap)?.floatEmbedding()
                    
                    if (enhancedEmbedding != null) {
                        val secondResult = repository.advancedSearch(criteria, enhancedEmbedding, detectedWords)
                        
                        // Si le deuxième essai donne un résultat de haute confiance, on le prend
                        if (!secondResult.isFallback) {
                            Log.i("SearchViewModel", "Succès au 2ème essai !")
                            searchResult = secondResult
                        }
                    }
                }

                _searchResultState.value = SearchResultState.Success(
                    results = searchResult.results, 
                    totalCount = searchResult.totalCount,
                    isFallback = searchResult.isFallback
                )
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
