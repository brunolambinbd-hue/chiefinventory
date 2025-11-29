package com.example.parabdcollector.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.parabdcollector.model.Location
import com.example.parabdcollector.repo.LocationRepository
import kotlinx.coroutines.launch

class LocationViewModel(private val repository: LocationRepository) : ViewModel() {

    // The original, flat list of all locations from the database.
    private val allLocations: LiveData<List<Location>> = repository.getAll()

    // A set to keep track of which location IDs are currently expanded.
    private val _expandedState = MutableLiveData<Set<Long>>(emptySet())

    // The final, visible list of expandable locations to be displayed in the RecyclerView.
    private val _visibleLocations = MediatorLiveData<List<ExpandableLocation>>()
    val visibleLocations: LiveData<List<ExpandableLocation>> = _visibleLocations

    init {
        // We listen to changes from both the original data and the expansion state.
        _visibleLocations.addSource(allLocations) { locations ->
            // First time we get locations, expand all parents by default as requested.
            if (_expandedState.value.isNullOrEmpty() && locations.isNotEmpty()) {
                val parentIds = locations.filter { loc -> locations.any { it.parentLocationId == loc.id } }
                                         .map { it.id }
                                         .toSet()
                _expandedState.value = parentIds
            } else {
                 _visibleLocations.value = buildVisibleList(locations, _expandedState.value ?: emptySet())
            }
        }
        _visibleLocations.addSource(_expandedState) { expandedIds ->
            _visibleLocations.value = buildVisibleList(allLocations.value ?: emptyList(), expandedIds)
        }
    }

    /**
     * Toggles the expansion state for a given location ID.
     */
    fun toggleExpansion(locationId: Long) {
        val currentExpanded = _expandedState.value ?: emptySet()
        _expandedState.value = if (currentExpanded.contains(locationId)) {
            currentExpanded - locationId
        } else {
            currentExpanded + locationId
        }
    }

    /**
     * Builds the flat list of VISIBLE locations based on the current expansion state.
     */
    private fun buildVisibleList(
        all: List<Location>,
        expandedIds: Set<Long>
    ): List<ExpandableLocation> {
        val visibleList = mutableListOf<ExpandableLocation>()
        val locationsByParent = all.groupBy { it.parentLocationId }
        val allChildrenMap = all.associate { it.id to locationsByParent.containsKey(it.id) }

        fun addChildren(parentId: Long?, depth: Int) {
            val children = locationsByParent[parentId]?.sortedBy { it.name }
            children?.forEach { location ->
                val hasChildren = allChildrenMap[location.id] ?: false
                visibleList.add(
                    ExpandableLocation(
                        location = location,
                        depth = depth,
                        isExpanded = expandedIds.contains(location.id),
                        hasChildren = hasChildren
                    )
                )
                // If the current location is expanded, we add its children recursively.
                if (expandedIds.contains(location.id)) {
                    addChildren(location.id, depth + 1)
                }
            }
        }

        addChildren(null, 0) // Start with root locations (parentId is null)
        return visibleList
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
}
