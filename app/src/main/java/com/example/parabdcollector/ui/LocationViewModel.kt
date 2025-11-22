package com.example.parabdcollector.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.parabdcollector.model.Location
import com.example.parabdcollector.repo.LocationRepository
import kotlinx.coroutines.launch

class LocationViewModel(private val repository: LocationRepository) : ViewModel() {

    val allLocations: LiveData<List<Location>> = repository.getAll()

    fun getRootLocations(): LiveData<List<Location>> {
        return repository.getRootLocations()
    }

    fun getChildren(parentId: Long): LiveData<List<Location>> {
        return repository.getChildren(parentId)
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
