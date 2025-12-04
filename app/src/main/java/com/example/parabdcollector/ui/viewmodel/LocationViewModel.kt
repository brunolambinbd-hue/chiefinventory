package com.example.parabdcollector.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.example.parabdcollector.model.Location
import com.example.parabdcollector.repo.CollectionRepository
import com.example.parabdcollector.repo.LocationRepository
import com.example.parabdcollector.ui.model.DisplayLocation
import com.example.parabdcollector.ui.model.ExpandableLocation
import com.example.parabdcollector.ui.model.ItemCountForLocation
import kotlinx.coroutines.launch

/**
 * ViewModel for the location management screen ([LocationManagementActivity]).
 *
 * This class is responsible for managing the hierarchical state of locations, including which
 * items are expanded or collapsed, and for providing a flattened list for the UI to display.
 *
 * @property locationRepository The [LocationRepository] for accessing location data.
 * @property collectionRepository The [CollectionRepository] for accessing item data, used here to get item counts.
 */
class LocationViewModel(
    private val locationRepository: LocationRepository,
    private val collectionRepository: CollectionRepository
) : ViewModel() {

    private val allLocations: LiveData<List<Location>> = locationRepository.getAll()
    private val itemCountByLocation: LiveData<List<ItemCountForLocation>> = collectionRepository.getItemCountByLocation()
    private val _expandedState = MutableLiveData<Set<Long>>(emptySet())

    private val _visibleLocations = MediatorLiveData<List<ExpandableLocation>>()
    /** The final, flattened list of locations to be displayed in the RecyclerView, reflecting the current expanded/collapsed state. */
    val visibleLocations: LiveData<List<ExpandableLocation>> = _visibleLocations

    val displayLocations: LiveData<List<DisplayLocation>> = allLocations.map {
        buildDisplayList(it)
    }

    init {
        // This function will be called whenever any of the source LiveData changes.
        fun updateVisibleList() {
            val locations = allLocations.value ?: return
            val counts = itemCountByLocation.value ?: emptyList()
            val expandedIds = _expandedState.value ?: emptySet()
            _visibleLocations.value = buildVisibleList(locations, counts, expandedIds)
        }

        _visibleLocations.addSource(allLocations) { updateVisibleList() }
        _visibleLocations.addSource(itemCountByLocation) { updateVisibleList() }
        _visibleLocations.addSource(_expandedState) { updateVisibleList() }
    }

    /**
     * Toggles the expanded/collapsed state of a given location.
     * @param locationId The ID of the location to toggle.
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
     * Constructs the visible list of [ExpandableLocation]s based on the full list and the current expanded state.
     * @param all The complete list of all locations.
     * @param counts The list of item counts per location.
     * @param expandedIds The set of IDs for locations that are currently expanded.
     * @return A flattened list representing the visible portion of the location tree.
     */
    private fun buildVisibleList(
        all: List<Location>,
        counts: List<ItemCountForLocation>,
        expandedIds: Set<Long>
    ): List<ExpandableLocation> {
        val visibleList = mutableListOf<ExpandableLocation>()
        val locationsByParent = all.groupBy { it.parentLocationId }
        val allChildrenMap = all.associate { it.id to locationsByParent.containsKey(it.id) }
        val countsMap = counts.associateBy { it.locationId }

        fun addChildren(parentId: Long?, depth: Int) {
            val children = locationsByParent[parentId]?.sortedBy { it.name }
            children?.forEach { location ->
                val hasChildren = allChildrenMap[location.id] ?: false
                val itemCount = countsMap[location.id]?.count ?: 0
                visibleList.add(
                    ExpandableLocation(
                        location = location,
                        depth = depth,
                        isExpanded = expandedIds.contains(location.id),
                        hasChildren = hasChildren,
                        itemCount = itemCount
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

    /**
     * Recursively builds a flat list of [DisplayLocation]s from a hierarchical list of [Location]s for dropdown display.
     * @param locations The complete list of locations from the database.
     * @return A list of [DisplayLocation]s, ordered and with depth information.
     */
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

    /**
     * Inserts a new location into the database.
     * @param location The [Location] to insert.
     */
    fun insert(location: Location) = viewModelScope.launch {
        locationRepository.insert(location)
    }

    /**
     * Updates an existing location in the database.
     * @param location The [Location] to update.
     */
    fun update(location: Location) = viewModelScope.launch {
        locationRepository.update(location)
    }

    /**
     * Deletes a location from the database.
     * @param location The [Location] to delete.
     */
    fun delete(location: Location) = viewModelScope.launch {
        locationRepository.delete(location)
    }
}
