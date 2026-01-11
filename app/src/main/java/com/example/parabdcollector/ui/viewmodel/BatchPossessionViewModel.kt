package com.example.parabdcollector.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.example.parabdcollector.model.CollectionItem
import com.example.parabdcollector.model.Location
import com.example.parabdcollector.repo.CollectionRepository
import com.example.parabdcollector.repo.LocationRepository
import com.example.parabdcollector.ui.model.DisplayLocation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BatchPossessionViewModel(
    private val repository: CollectionRepository,
    private val locationRepository: LocationRepository
) : ViewModel() {

    private val _analysisResult = MutableLiveData<AnalysisResult?>(null)
    val analysisResult: LiveData<AnalysisResult?> = _analysisResult

    private val _updateStatus = MutableLiveData<Int?>(null)
    val updateStatus: LiveData<Int?> = _updateStatus

    /** List of locations for the dropdown. */
    val displayLocations: LiveData<List<DisplayLocation>> = locationRepository.getAll().map {
        buildDisplayList(it)
    }

    private var itemsToUpdate: List<CollectionItem> = emptyList()
    var selectedLocationId: Long? = null

    fun analyzeSeries(seriesName: String, startNum: Int, endNum: Int) {
        viewModelScope.launch {
            val allItems = repository.getAllByTitle(seriesName)
            val numberRegex = Regex("""(?i)$seriesName.*?(?:n°|\s+)(\d+)""")

            itemsToUpdate = allItems.filter { item ->
                if (item.isPossessed) return@filter false
                val match = numberRegex.find(item.titre)
                val num = match?.groupValues?.get(1)?.toIntOrNull()
                num != null && num in startNum..endNum
            }
            _analysisResult.postValue(AnalysisResult(itemsToUpdate.size, endNum - startNum + 1))
        }
    }

    fun applyUpdate() {
        val list = itemsToFix()
        if (list.isEmpty()) return
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                list.forEach { item ->
                    repository.update(item.copy(isPossessed = true, locationId = selectedLocationId))
                }
            }
            _updateStatus.postValue(list.size)
            _analysisResult.postValue(null)
        }
    }

    private fun itemsToFix() = itemsToUpdate

    private fun buildDisplayList(locations: List<Location>): List<DisplayLocation> {
        val displayList = mutableListOf<DisplayLocation>()
        val locationsByParent = locations.groupBy { it.parentId }
        fun addChildren(parentId: Long?, depth: Int) {
            locationsByParent[parentId]?.sortedBy { it.name }?.forEach { location ->
                displayList.add(DisplayLocation(location, depth))
                addChildren(location.id, depth + 1)
            }
        }
        addChildren(null, 0)
        return displayList
    }

    data class AnalysisResult(val foundCount: Int, val rangeSize: Int)
}
