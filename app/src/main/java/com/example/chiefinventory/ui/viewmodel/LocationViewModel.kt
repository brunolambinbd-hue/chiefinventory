package com.example.chiefinventory.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.chiefinventory.model.Location
import com.example.chiefinventory.model.LocationType
import com.example.chiefinventory.repo.LocationRepository
import com.example.chiefinventory.ui.model.DisplayLocation
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * ViewModel for the location management screen.
 * Supports filtering by [LocationType] to distinguish between Ingredient and Recipe locations.
 */
class LocationViewModel(private val repository: LocationRepository) : ViewModel() {

    private val _currentType = MutableStateFlow(LocationType.COLLECTION)
    val currentType: MutableStateFlow<LocationType> = _currentType

    /** A LiveData list of locations filtered by the current type. */
    @OptIn(ExperimentalCoroutinesApi::class)
    val allLocations: LiveData<List<Location>> = _currentType.flatMapLatest { type ->
        repository.getAllByType(type).asFlow()
    }.asLiveData()

    private val _expandedStates = MutableLiveData<Set<Long>>(emptySet())

    /** 
     * The complete hierarchical list of locations. 
     */
    val displayLocations: LiveData<List<DisplayLocation>> = allLocations.asFlow().map { buildDisplayList(it) }.asLiveData()

    /** 
     * Visible locations now simply returns all display locations.
     */
    val visibleLocations: LiveData<List<DisplayLocation>> = displayLocations

    fun setLocationType(type: LocationType) {
        _currentType.value = type
    }

    fun toggleExpansion(locationId: Long) {
        val currentExpanded = _expandedStates.value
        _expandedStates.value = toggleLocationExpandedState(currentExpanded, locationId)
    }

    fun expandAll() {
        val all = displayLocations.value
        if (all != null) {
            _expandedStates.value = extractParentIds(all)
        }
    }

    fun insert(name: String, parentId: Long?): Job = viewModelScope.launch {
        repository.insert(Location(name = name, parentId = parentId, type = _currentType.value))
    }

    fun update(location: Location): Job = viewModelScope.launch {
        repository.update(location)
    }

    fun delete(location: Location): Job = viewModelScope.launch {
        repository.delete(location)
    }

    fun updateLocationParent(locationId: Long, newParentId: Long?): Job = viewModelScope.launch {
        repository.updateLocationParent(locationId, newParentId)
    }

    companion object {
        fun toggleLocationExpandedState(currentExpanded: Set<Long>?, locationId: Long): Set<Long> {
            val set = if (currentExpanded != null) {
                currentExpanded
            } else {
                emptySet()
            }

            if (locationId in set) {
                return set - locationId
            } else {
                return set + locationId
            }
        }

        fun extractParentIds(displayLocations: List<DisplayLocation>): Set<Long> {
            return displayLocations.mapNotNull { displayLocation ->
                displayLocation.location.parentId
            }.toSet()
        }

        fun buildDisplayList(locations: List<Location>): List<DisplayLocation> {
            val displayList = mutableListOf<DisplayLocation>()
            val locationsByParent = locations.groupBy { it.parentId }

            fun addChildren(parentId: Long?, depth: Int) {
                val children = locationsByParent[parentId]
                if (children != null) {
                    children.sortedBy { it.name }.forEach { location ->
                        displayList.add(DisplayLocation(location, depth))
                        addChildren(location.id, depth + 1)
                    }
                }
            }

            addChildren(null, 0)
            return displayList
        }
    }
}
