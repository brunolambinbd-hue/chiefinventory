package com.example.parabdcollector.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.example.parabdcollector.model.Location
import com.example.parabdcollector.repo.LocationRepository
import kotlinx.coroutines.launch

class LocationViewModel(private val repository: LocationRepository) : ViewModel() {

    private val allLocations: LiveData<List<Location>> = repository.getAll()
    private val _expandedState = MutableLiveData<Set<Long>>(emptySet())

    private val _visibleLocations = MediatorLiveData<List<ExpandableLocation>>()
    val visibleLocations: LiveData<List<ExpandableLocation>> = _visibleLocations

    val displayLocations: LiveData<List<DisplayLocation>> = allLocations.map {
        buildDisplayList(it)
    }

    init {
        _visibleLocations.addSource(allLocations) { locations ->
            val currentExpanded = _expandedState.value ?: emptySet()
            if (currentExpanded.isEmpty() && locations.isNotEmpty()) {
                val parentIds = locations.filter { loc -> locations.any { it.parentLocationId == loc.id } }
                                         .map { it.id }
                                         .toSet()
                _expandedState.value = parentIds
                // On force la reconstruction avec le nouvel état étendu
                _visibleLocations.value = buildVisibleList(locations, parentIds)
            } else {
                _visibleLocations.value = buildVisibleList(locations, currentExpanded)
            }
        }
        _visibleLocations.addSource(_expandedState) { expandedIds ->
            _visibleLocations.value = buildVisibleList(allLocations.value ?: emptyList(), expandedIds)
        }
    }

    fun toggleExpansion(locationId: Long) {
        val currentExpanded = _expandedState.value ?: emptySet()
        _expandedState.value = if (currentExpanded.contains(locationId)) {
            currentExpanded - locationId
        } else {
            currentExpanded + locationId
        }
    }

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
                if (expandedIds.contains(location.id)) {
                    addChildren(location.id, depth + 1)
                }
            }
        }

        addChildren(null, 0)
        return visibleList
    }

    private fun buildDisplayList(locations: List<Location>): List<DisplayLocation> {
        val displayList = mutableListOf<DisplayLocation>()
        val locationsByParent = locations.groupBy { it.parentLocationId }

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
}
