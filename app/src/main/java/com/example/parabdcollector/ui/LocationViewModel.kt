package com.example.parabdcollector.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.example.parabdcollector.model.Location
import com.example.parabdcollector.repo.LocationRepository
import kotlinx.coroutines.launch

// Nouvelle data class pour l'affichage, incluant la profondeur
data class DisplayLocation(val location: Location, val depth: Int)

class LocationViewModel(private val repository: LocationRepository) : ViewModel() {

    // La liste de base, non modifiée
    private val allLocations: LiveData<List<Location>> = repository.getAll()

    // La nouvelle liste transformée pour l'affichage
    val displayLocations: LiveData<List<DisplayLocation>> = allLocations.map {
        buildDisplayList(it)
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

        addChildren(null, 0) // On commence par les éléments racines
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
