package com.example.parabdcollector.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.parabdcollector.model.CollectionItem
import com.example.parabdcollector.repo.CollectionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel for handling batch possession updates for series of items.
 */
class BatchPossessionViewModel(private val repository: CollectionRepository) : ViewModel() {

    private val _analysisResult = MutableLiveData<AnalysisResult?>()
    val analysisResult: LiveData<AnalysisResult?> = _analysisResult

    private val _updateStatus = MutableLiveData<Int?>()
    val updateStatus: LiveData<Int?> = _updateStatus

    private var itemsToUpdate: List<CollectionItem> = emptyList()

    /**
     * Analyzes the collection to find items belonging to a series within a number range.
     * It uses a Regex to extract numbers from titles like "Spirou n°1500" or "Spirou 1500".
     * It uses a non-limited search to ensure all series items are processed.
     */
    fun analyzeSeries(seriesName: String, startNum: Int, endNumber: Int) {
        viewModelScope.launch {
            // Use the non-limited search function
            val allItemsOfSeries = repository.getAllByTitle(seriesName)
            
            // Regex explanation: matches optional whitespace, followed by "n°" or nothing, then captures one or more digits.
            val numberRegex = Regex("""(?i)$seriesName.*?(?:n°|\s+)(\d+)""")

            itemsToUpdate = allItemsOfSeries.filter { item ->
                // Only consider items the user does NOT already possess
                if (item.isPossessed) return@filter false
                
                val match = numberRegex.find(item.titre)
                val num = match?.groupValues?.get(1)?.toIntOrNull()
                
                num != null && num in startNum..endNumber
            }

            _analysisResult.value = AnalysisResult(
                foundCount = itemsToUpdate.size,
                rangeSize = endNumber - startNum + 1
            )
        }
    }

    /**
     * Performs the batch update, marking all analyzed items as possessed.
     */
    fun applyUpdate() {
        val listToProcess = itemsToUpdate
        if (listToProcess.isEmpty()) return

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                listToProcess.forEach { item ->
                    repository.update(item.copy(isPossessed = true))
                }
            }
            _updateStatus.value = listToProcess.size
            _analysisResult.value = null // Clear analysis after success
        }
    }

    data class AnalysisResult(val foundCount: Int, val rangeSize: Int)
}
