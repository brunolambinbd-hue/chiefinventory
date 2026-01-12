package com.example.parabdcollector.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.example.parabdcollector.model.Location
import com.example.parabdcollector.repo.LocationRepository
import com.example.parabdcollector.ui.model.DisplayLocation
import kotlinx.coroutines.launch

/**
 * ViewModel for the location management screen.
 * Simplified to always show all locations in a hierarchical flat list to ensure immediate visibility.
 */
class LocationViewModel(private val repository: LocationRepository) : ViewModel() {

    /** A LiveData list of all locations from the database. */
    val allLocations: LiveData<List<Location>> = repository.getAll()

    private val _expandedStates = MutableLiveData<Set<Long>>(emptySet())

    /** 
     * The complete hierarchical list of locations. 
     * Since the user wants to see ALL locations, we expose this directly.
     */
    val displayLocations: LiveData<List<DisplayLocation>> = allLocations.map { buildDisplayList(it) }

    /** 
     * Visible locations now simply returns all display locations 
     * to ensure immediate visibility of new items, even sub-locations.
     */
    val visibleLocations: LiveData<List<DisplayLocation>> = displayLocations

    fun toggleExpansion(locationId: Long) {
        // Kept for compatibility, but no longer impacts visibility in this simplified mode.
        val currentExpanded = _expandedStates.value ?: emptySet()
        _expandedStates.value = if (locationId in currentExpanded) {
            currentExpanded - locationId
        } else {
            currentExpanded + locationId
        }
    }

    fun expandAll() {
        displayLocations.value?.let { all ->
            _expandedStates.value = all.mapNotNull { it.location.parentId }.toSet()
        }
    }

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

    fun insert(location: Location) = viewModelScope.launch {
        repository.insert(location)
    }

    fun update(location: Location) = viewModelScope.launch {
        repository.update(location)
    }

    fun delete(location: Location) = viewModelScope.launch {
        repository.delete(location)
    }

    fun updateLocationParent(locationId: Long, newParentId: Long?) = viewModelScope.launch {
        repository.updateLocationParent(locationId, newParentId)
    }
}
