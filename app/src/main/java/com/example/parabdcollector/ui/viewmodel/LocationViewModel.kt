package com.example.parabdcollector.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.example.parabdcollector.model.Location
import com.example.parabdcollector.repo.LocationRepository
import com.example.parabdcollector.ui.model.DisplayLocation
import kotlinx.coroutines.launch

class LocationViewModel(private val repository: LocationRepository) : ViewModel() {

    val allLocations: LiveData<List<Location>> = repository.getAll()

    private val _expandedStates = MutableLiveData<Set<Long>>(emptySet())

    /** A flat list of all locations, decorated with their depth for indented display. Used for dialogs. */
    val displayLocations: LiveData<List<DisplayLocation>> = allLocations.map { buildDisplayList(it) }

    /** A filtered list of locations that should be visible based on the current expansion states. */
    val visibleLocations: LiveData<List<DisplayLocation>> = MediatorLiveData<List<DisplayLocation>>().apply {
        var allDisplayLocations: List<DisplayLocation> = emptyList()
        var expandedIds: Set<Long> = emptySet()

        fun update() {
            val visibleList = mutableListOf<DisplayLocation>()
            val locationMap = allDisplayLocations.associateBy { it.location.id }

            for (displayLocation in allDisplayLocations) {
                // A location is visible if it is a root item, or if its direct parent is expanded.
                val parentId = displayLocation.location.parentId
                if (parentId == null) {
                    visibleList.add(displayLocation)
                } else if (expandedIds.contains(parentId)) {
                    // To be visible, all its ancestors must also be expanded
                    var isAncestorPathExpanded = true
                    var currentParentId = locationMap[parentId]?.location?.parentId
                    while (currentParentId != null) {
                        if (!expandedIds.contains(currentParentId)) {
                            isAncestorPathExpanded = false
                            break
                        }
                        currentParentId = locationMap[currentParentId]?.location?.parentId
                    }
                    if (isAncestorPathExpanded) {
                        visibleList.add(displayLocation)
                    }
                }
            }
            value = visibleList
        }

        addSource(displayLocations) {
            allDisplayLocations = it
            update()
        }
        addSource(_expandedStates) {
            expandedIds = it
            update()
        }
    }

    /**
     * Toggles the expansion state for a given location ID.
     */
    fun toggleExpansion(locationId: Long) {
        val currentExpanded = _expandedStates.value ?: emptySet()
        _expandedStates.value = if (locationId in currentExpanded) {
            currentExpanded - locationId
        } else {
            currentExpanded + locationId
        }
    }

    /**
     * Expands all parent locations to show their children.
     */
    fun expandAll() {
        displayLocations.value?.let { allLocations ->
            // Find all unique parent IDs from the list of all locations.
            val parentIds = allLocations.mapNotNull { it.location.parentId }.toSet()
            _expandedStates.value = parentIds
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

        addChildren(null, 0) // Start with root elements
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
